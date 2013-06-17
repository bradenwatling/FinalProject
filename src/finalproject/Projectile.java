package finalproject;


import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/**
 * This is the Projectile class. It is a subclass of Actor. When created, a
 * target Tile must be defined, and the sole purpose of the Projectile is to
 * move in the same direction as its first target until it is destroyed by
 * colliding with a wall, or until it is destroyed externally by colliding with
 * an Enemy.
 *
 * June 17, 2013
 *
 * @author Braden Watling
 */
public class Projectile extends Actor {

    /**
     * The sprite sheet for the projectile. Source:
     * http://www.rpgmakervx.net/lofiversion/index.php/t8973.html
     */
    public static BufferedImage projectileImage;
    /**
     * The number of frames in the sprite sheet
     */
    public static final int NUM_FRAMES = 3;
    /**
     * The amount of damage that is done when a Projectile collides with an
     * Enemy.
     */
    public static final int DAMAGE_TO_ENEMY = 25;
    /**
     * Whether or not the Projectile is destroyed.
     */
    private boolean destroyProjectile;

    /**
     * Create a Projectile based on the parameters below.
     *
     * @param position The position Tile.
     * @param target The initial target Tile. This represents the direction that
     * the Projectile will continue to travel in until it must be destroyed.
     * @param currentLevel A reference to the current Level.
     */
    public Projectile(Tile position, Tile target, Level currentLevel) {
        super(position, currentLevel, projectileImage, NUM_FRAMES, 50);
        this.health = 0;
        this.target = target;
        destroyProjectile = false;
        moveToTarget();

        speed = 2.0;

        // Dont go to a default frame when the projectile stops
        xDefaultFrame = -1;
        yDefaultFrame = -1;
    }

    @Override
    /**
     * This method is responsible for updating the position of the Projectile.
     * When the Projectile finishes moving from one Tile to the next, it must
     * immediately start moving towards the next Tile in the same direction, and
     * destroy itself if that Tile is a wall or is null.
     */
    void update() {
        //Level and position must not be null.
        if (currentLevel == null || position == null) {
            return;
        }

        //If we're not halfway between Tiles
        if (transitionComplete) {
            //Continue to move in whichever direction we were previously moving.
            if (moveLeft) {
                target = currentLevel.getLeft(position);
            } else if (moveRight) {
                target = currentLevel.getRight(position);
            } else if (moveUp) {
                target = currentLevel.getUp(position);
            } else if (moveDown) {
                target = currentLevel.getDown(position);
            }

            // If unable to moveToTarget(), request to destroy the projectile
            if (!moveToTarget()) {
                destroyProjectile = true;
            }
        }

        //Update the current frame
        updateAnimation();
    }

    @Override
    /**
     * This method returns an Area object representing the Area around the
     * Projectile that is not shown as darkness in the Level. Essentially, this
     * means that the Area defined by this function is "cut out" from the black
     * that covers the Level.
     */
    protected Area getLight() {
        int x = position.getXPixels() + xMove + Tile.WIDTH / 2;
        int y = position.getYPixels() + yMove + Tile.HEIGHT / 2;

        return new Area(new Ellipse2D.Float(x - lightRadius / 2, y
                - lightRadius / 2, lightRadius, lightRadius));
    }

    /**
     * Gets whether or not the Projectile is destroyed.
     *
     * @return Whether or not the Projectile is destroyed.
     */
    public boolean getDestroyProjectile() {
        return destroyProjectile;
    }

    /**
     * This method destroys the Projectile.s
     */
    public void destroyProjectile() {
        destroyProjectile = true;
    }
}
