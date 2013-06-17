
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * This class is the superclass of all Enemy objects, and is a subclass of
 * Actor. It does the bulk of the logic for the different types of Enemies,
 * while still providing the subclasses with ways to customize the tracking
 * logic.
 *
 * @author Braden Watling
 */
public abstract class Enemy extends Actor {

    /**
     * These are the sources for all of the graphics for the different Enemy
     * types:
     *
     * http://www.spriters-resource.com/ds/pokeheartgoldsoulsilver/sheet/26794
     * http://www.spriters-resource.com/fullview/26795/
     * http://www.spriters-resource.com/fullview/26794/
     */
    public static int FPS = 15;
    /**
     * Keep a reference to the Player. This lets the Enemy know where the Player
     * is in order to track it.
     */
    protected Player player;
    /**
     * This represents the position the Player was in the last time the Enemy
     * was updated. If this changes, then pathToPlayer should be updated. If it
     * doesn't change, however, then the path does not need to be recalculated.
     */
    protected Tile lastPlayerPosition;
    /**
     * This is the shortest path to the Player, determined by getPath() in
     * Level. The first Tile in the path is on the top of the stack, while the
     * last Tile in the path is on the bottom of the stack.
     */
    protected ArrayList<Tile> pathToPlayer;
    /**
     * This represents the amount of health that the player loses when it
     * collides with this Enemy.
     */
    protected int damage;
    /**
     * This represents the amount of time that the Enemy should wait after
     * reaching its destination (emptying the pathToPlayer ArrayList).
     */
    protected int retargetTime;

    /**
     * Create an Enemy based on the parameters below.
     *
     * @param position The position Tile of the Enemy.
     * @param currentLevel A reference to the Level.
     * @param player A reference to the Player.
     * @param spriteSheetImage The image representing this Enemy's sprite sheet.
     * @param numFrames The number of frames in the sprite sheet.
     * @param speed The speed, in relation to the Player, of the Enemy.
     * @param damage The damage that the Enemy does to the Player.
     * @param health The amount of health that the Enemy has.
     * @param retargetTime The amount of time the Enemy must wait between
     * choosing targets.
     */
    public Enemy(Tile position, Level currentLevel, Player player, BufferedImage spriteSheetImage, int numFrames, double speed, int damage, int health, int retargetTime) {
        super(position, currentLevel, spriteSheetImage, numFrames, FPS);

        this.player = player;
        this.speed = speed;
        this.damage = damage;
        this.health = health;
        this.retargetTime = retargetTime;

        //Enemies have a light radius half that of the default.
        lightRadius /= 2;
        
        pathToPlayer = new ArrayList<Tile>();

        // Dont go to a default frame when the projectile stops
        xDefaultFrame = -1;
        yDefaultFrame = -1;
    }
    long timeSinceRetarget = 0;

    @Override
    /**
     * This is the update method of the Enemy class. It is responsible for
     * updating everything relating to the Enemy, including the path to the
     * Player, and other aspects.
     */
    void update() {
        if (currentLevel == null || position == null || player == null) {
            return;
        }

        //If we're not halfway between Tiles
        if (transitionComplete) {

            //Record the position of the Player
            Tile curPlayerPosition = player.getPosition();

            //Record the time
            long now = System.currentTimeMillis();
            if (updatePath()) {
                //If the path isn't null, clear it
                if (pathToPlayer != null) {
                    pathToPlayer.clear();
                }
                //Get a new path
                currentLevel.getPath(pathToPlayer, position,
                        curPlayerPosition);

                //Record the time that we last retargeted
                timeSinceRetarget = now;
            }

            //Update where we last saw the Player
            lastPlayerPosition = curPlayerPosition;

            //If we've waited long enough since we last built our path
            if (now - timeSinceRetarget > retargetTime) {
                if (chooseRandom()) {
                    //If we want to choose a random Tile
                    target = getRandomAdjacent();
                    if (target != null && pathToPlayer != null) {
                        //Choose the random Tile and build a new Path from
                        //where we're going to be, to the Player
                        currentLevel.getPath(pathToPlayer, target,
                                curPlayerPosition);
                    }
                } else if (pathToPlayer != null) {
                    //If we don't want to choose random, and we have a path
                    int pathSize = pathToPlayer.size();
                    if (pathSize > 0) {
                        //Take elements off the top of the path
                        target = pathToPlayer.get(pathSize - 1);
                        pathToPlayer.remove(pathSize - 1);
                    }
                }
            }

            //Let the Actor class update information
            updateAnimation();

            //If for some reason, the path cannot be followed,
            if (!moveToTarget()) {
                //Regenerate the path
                currentLevel.getPath(pathToPlayer, position,
                        curPlayerPosition);
            }
        }
    }

    /**
     * This function determines whether or not the Enemy should choose a random
     * direction or not. The way this should be done is by checking if
     * Math.random() * 10 is less than some number representing the number of
     * times out of 10 that a random Tile should be chosen.
     *
     * @return Whether or not a random Tile should be chosen.
     */
    protected boolean chooseRandom() {
        //For a basic Enemy, never choose a random Tile.
        return false;
    }

    /**
     * This method determines when a new path to the Player must be determined.
     * For a basic Enemy, this occurs when the Player has moved from where the
     * Enemy last thought it was.
     *
     * @return Whether or not the path to the Player should be updated.
     */
    protected boolean updatePath() {
        return lastPlayerPosition == null
                || !lastPlayerPosition.equals(player.getPosition());
    }

    /**
     * This method returns a random Tile that is adjacent to the Enemy's
     * position. This means that there are a maximum of 4 possibilities for
     * which direction the returned Tile will be in.
     *
     * @return A random Tile that is adjacent to the Enemy's position.
     */
    private Tile getRandomAdjacent() {
        int direction = (int) (Math.random() * 4);
        //Limit the random direction to be from 0 - 3
        //The only time direction will be 4 is if Math.random() returns 1.0
        direction = direction >= 4 ? 3 : direction;

        //Get a different direction based on each outcome
        switch (direction) {
            case 0:
                return currentLevel.getLeft(position);
            case 1:
                return currentLevel.getRight(position);
            case 2:
                return currentLevel.getUp(position);
            case 3:
            default:
                return currentLevel.getDown(position);
        }
    }

    @Override
    /**
     * This method returns an Area object representing the Area around the Enemy
     * that is not shown as darkness in the Level. Essentially, this means that
     * the Area defined by this function is "cut out" from the black that covers
     * the Level.
     */
    protected Area getLight() {
        if (position == null) {
            return new Area();
        }

        int x = position.getXPixels() + xMove + Tile.WIDTH / 2;
        int y = position.getYPixels() + yMove + Tile.HEIGHT / 2;

        return new Area(new Ellipse2D.Float(x - lightRadius, y
                - lightRadius, lightRadius * 2, lightRadius * 2));
    }

    /**
     * Gets the amount of damage that this Enemy does to the Player.
     *
     * @return The amount of damage that this Enemy does to the Player.
     */
    public int getDamageAmount() {
        return damage;
    }
}
