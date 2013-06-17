
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * This is the Player class. It represents the user of the game, and the main
 * character of the game.
 *
 * June 17, 2013
 *
 * @author Braden Watling
 */
public class Player extends Actor implements KeyListener {

    /**
     * The sprite sheet of the Player. Source for Player:
     * http://charas-project.net/charas2/index.php
     *
     * Old Player:
     * http://www.spriters-resource.com/community/printthread.php?tid=19817
     */
    public static BufferedImage playerImage;
    /**
     * The number of frames in the sprite sheet
     */
    public static final int NUM_FRAMES = 3;
    /**
     * The frame rate to display each frame from the sprite sheet at.
     */
    public static final int FPS = 15;
    /**
     * Default time between shots
     */
    public static final int DEFAULT_SHOOT_INTERVAL = 1000;
    /**
     * A reference to an ArrayList storing all of the Projectiles
     */
    private ArrayList<Projectile> projectiles;
    /**
     * An ArrayList to store all of the keys that are currently being pressed on
     * the keyboard.
     */
    private ArrayList<Character> keys;
    /**
     * The time between shooting Projectiles. This was made a member variable in
     * order to provide infrastructure for a PowerUp that could increase the
     * rate of fire of Projectiles.
     */
    private int shootInterval;
    /**
     * The time that the last Projectile was fired.
     */
    private long lastProjectileTime;

    /**
     * Create a Player based on the parameters below.
     *
     * @param position The position Tile of the Player.
     * @param currentLevel A reference to the Level.
     * @param projectiles A reference to the ArrayList of Projectiles.
     */
    public Player(Tile position, Level currentLevel,
            ArrayList<Projectile> projectiles) {
        super(position, currentLevel, playerImage, NUM_FRAMES, FPS);
        this.projectiles = projectiles;

        //Initialize default values
        keys = new ArrayList<Character>();
        shootInterval = DEFAULT_SHOOT_INTERVAL;
        lastProjectileTime = 0;

        //This frame has the character just standing in one place.
        xDefaultFrame = 1;
        // Dont change the direction of the player when it
        // stops moving
        yDefaultFrame = -1;
    }

    @Override
    /**
     * This is the update method of the Player class. It is responsible for
     * updating the position and target of the Player based on the requested
     * direction of movement (from keys).
     */
    void update() {
        //Level and position must exist
        if (currentLevel == null || position == null) {
            return;
        }

        //Handle projectile creation
        if (keys.contains(' ')) {
            Tile projectileDirection = null;

            //Use the target as a parent if it exists and isn't a wall, otherwise use the position
            Tile parent = (target != null && !target.getIsWall()) ? target
                    : position;

            //Set the direction of the projectile based on the direction last moved in
            if (moveLeft) {
                projectileDirection = currentLevel.getLeft(parent);
            } else if (moveRight) {
                projectileDirection = currentLevel.getRight(parent);
            } else if (moveUp) {
                projectileDirection = currentLevel.getUp(parent);
            } else if (moveDown) {
                projectileDirection = currentLevel.getDown(parent);
            }

            //Make sure the projectileDirection is correct and it's been long enough since the last Projectile
            long now = System.currentTimeMillis();
            if (projectileDirection != null && !projectileDirection.getIsWall()
                    && now - lastProjectileTime > shootInterval) {

                //Create a new Projectile
                projectiles.add(new Projectile(parent, projectileDirection,
                        currentLevel));

                //Record the time it was shot at
                lastProjectileTime = now;
            }
        }

        //If we're not halfway between Tiles
        if (transitionComplete) {
            //If we have any keys
            if (keys.size() > 0) {
                //Start at the end of the ArrayList and go backwards
                //The reason for this is to get the most recently pressed key
                for (int i = keys.size() - 1; i >= 0; i--) {
                    char c = keys.get(i);

                    //If the key is any of the desired keys
                    if (c == 'a' || c == 'd' || c == 'w' || c == 's') {
                        //Check which key it is and take appropriate action
                        switch (c) {
                            case 'a':
                                target = currentLevel.getLeft(position);
                                break;
                            case 'd':
                                target = currentLevel.getRight(position);
                                break;
                            case 'w':
                                target = currentLevel.getUp(position);
                                break;
                            case 's':
                                target = currentLevel.getDown(position);
                                break;
                        }
                        //Start movement towards target
                        moveToTarget();
                        //Found a desired key, stop looking
                        break;
                    }
                }
            }
        }

        //Update the current frame
        updateAnimation();
    }

    @Override
    /**
     * This method returns an Area object representing the Area around the
     * Player that is not shown as darkness in the Level. Essentially, this
     * means that the Area defined by this function is "cut out" from the black
     * that covers the Level.
     */
    protected Area getLight() {
        int x = position.getXPixels() + xMove + Tile.WIDTH / 2;
        int y = position.getYPixels() + yMove + Tile.HEIGHT / 2;

        return new Area(new Ellipse2D.Float(x - lightRadius, y
                - lightRadius, lightRadius * 2, lightRadius * 2));
    }

    /**
     * This method is responsible for resetting the Player to its default
     * starting position.
     *
     * @param newLevel The new Level of the Player.
     * @param newPosition The new position Tile of the Player.
     */
    public void reset(Level newLevel, Tile newPosition) {
        super.reset(newLevel, newPosition);

        clearKeys();
    }

    /**
     * This method is responsible for emptying the pressed keys that have been
     * stored in the ArrayList of Characters.
     */
    public void clearKeys() {
        keys.clear();
    }

    /**
     * Unused.
     *
     * @param e Unused.
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * This method is called when a key is pressed. When this occurs, the
     * Character representing the key is converted to lower case, and added to
     * the ArrayList of Characters.
     *
     * @param e The KeyEvent
     */
    public void keyPressed(KeyEvent e) {
        char c = Character.toLowerCase(e.getKeyChar());

        if (!keys.contains(c)) {
            keys.add(c);
        }
    }

    /**
     * This method is called when a key is released. When this occurs, the
     * Character representing the key is converted to lower case, and removed
     * from the ArrayList of Characters.
     *
     * @param e The KeyEvent
     */
    public void keyReleased(KeyEvent e) {
        Character c = Character.toLowerCase(e.getKeyChar());

        keys.remove(c);
    }
}
