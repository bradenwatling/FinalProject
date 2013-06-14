
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class Enemy extends Actor {

    /**
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
     * This Image is defined by each Enemy subclass, and stores each frame of
     * the Enemy's movement animation is the following order from top to bottom:
     * down, left, right, up. The number of frames in each row should be
     * identical to the number specified by numFrames (from Actor)
     */
    protected BufferedImage spriteSheetImage;
    /**
     * This represents the position the Player was in the last time the Enemy was
     * updated. If this changes, then pathToPlayer should be updated. If it doesn't change,
     * however, then the path does not need to be recalculated.
     */
    protected Tile lastPlayerPosition;
    /**
     * This is the shortest path to the Player, determined by getPath() in
     * Level. The first Tile in the path is on the top of the stack, while the
     * last Tile in the path is on the bottom of the stack.
     */
    protected ArrayList<Tile> pathToPlayer;
    /**
     * This represents the number of times out of 10 that the enemy will choose
     * a random direction rather than following the path.
     */
    protected int randomChoice;
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

    public Enemy(Tile position, Level currentLevel, Player player, int numFrames, BufferedImage spriteSheetImage, double speed, int damage, int health, int randomChoice, int retargetTime) {
        super(position, currentLevel, numFrames, FPS);

        this.player = player;
        
        this.spriteSheetImage = spriteSheetImage;
        this.speed = speed;
        this.damage = damage;
        this.health = health;
        this.randomChoice = randomChoice;
        this.retargetTime = retargetTime;

        pathToPlayer = new ArrayList<Tile>();

        // Dont go to a default frame when the projectile stops
        xDefaultFrame = -1;
        yDefaultFrame = -1;
    }
    long timeSinceRetarget = 0;

    @Override
    void update() {
        if (currentLevel == null || position == null || player == null) {
            return;
        }

        boolean chooseRandom;
        Tile curPlayerPosition = player.getPosition();

        if (animationComplete) {
            chooseRandom = Math.random() * 10 < randomChoice;

            long now = System.currentTimeMillis();
            if (updatePath()) {
                if (pathToPlayer != null) {
                    pathToPlayer.clear();
                }
                pathToPlayer = currentLevel.getPath(pathToPlayer, position,
                        player.getPosition());

                timeSinceRetarget = now;
            }
            lastPlayerPosition = curPlayerPosition;

            if (now - timeSinceRetarget > retargetTime) {
                if (chooseRandom) {
                    target = getRandomAdjacent();
                    if (target != null && pathToPlayer != null) {
                        pathToPlayer.add(position);
                        // pathToPlayer = currentLevel.getPath(target,
                        // curPlayerPosition);
                    }
                } else if (pathToPlayer != null) {
                    int pathSize = pathToPlayer.size();
                    if (pathSize > 0) {
                        target = pathToPlayer.get(pathSize - 1);
                        pathToPlayer.remove(pathSize - 1);
                    }
                }
            }

            updateAnimation();

            if (!moveToTarget()) {
                pathToPlayer = currentLevel.getPath(pathToPlayer, position,
                        player.getPosition());
            }
        }
    }

    protected boolean updatePath() {
        return lastPlayerPosition == null
                || !lastPlayerPosition.equals(player.getPosition());
    }

    private Tile getRandomAdjacent() {
        int direction = (int) (Math.random() * 4);
        direction = direction >= 4 ? 3 : direction;

        Tile ret;

        switch (direction) {
            case 0:
                ret = currentLevel.getLeft(position);
                break;
            case 1:
                ret = currentLevel.getRight(position);
                break;
            case 2:
                ret = currentLevel.getUp(position);
                break;
            case 3:
            default:
                ret = currentLevel.getDown(position);
                break;
        }

        return ret;
    }

    @Override
    void draw(Graphics2D g) {
        if (spriteSheetImage != null) {
            BufferedImage frame = getCurrentFrame(spriteSheetImage);
            if (frame != null && doMove(g, frame) && position != null) {
                drawImage(g, frame, position.getXPixels(),
                        position.getYPixels());
            }
        }

        /*
         * if (pathToPlayer != null) { g.setColor(new Color(255, 0, 0, 50)); for
         * (int i = 0; i < pathToPlayer.size(); i++) { Tile t =
         * pathToPlayer.get(i); g.fillRect(t.getXPixels(), t.getYPixels(),
         * Tile.TILE_WIDTH, Tile.TILE_HEIGHT); } }
         */
    }

    @Override
    protected Area getLight() {
        if (position == null) {
            return new Area();
        }

        int x = position.getXPixels() + xMove + Tile.WIDTH / 2;
        int y = position.getYPixels() + yMove + Tile.HEIGHT / 2;

        /*
         * float smallRadius = lightRadius * 1.5f, bigRadius = lightRadius * 2;
         * if (moveLeft) { return new Area(new Ellipse2D.Float(x - bigRadius, y
         * - smallRadius / 2, bigRadius, smallRadius)); } else if (moveRight) {
         * return new Area(new Ellipse2D.Float(x, y - smallRadius / 2,
         * bigRadius, smallRadius)); } else if (moveUp) { return new Area(new
         * Ellipse2D.Float(x - smallRadius / 2, y - bigRadius, smallRadius,
         * bigRadius)); } else if (moveDown) { return new Area(new
         * Ellipse2D.Float(x - smallRadius / 2, y, smallRadius, bigRadius)); }
         * else {
         */
        return new Area(new Ellipse2D.Float(x - lightRadius / 2, y
                - lightRadius / 2, lightRadius, lightRadius));
        // }
    }

    public int getDamageAmount() {
        return damage;
    }
}
