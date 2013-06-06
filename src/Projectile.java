
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Braden
 */
public class Projectile extends Actor {

    public static final int NUM_FRAMES = 3;
    public static final int DAMAGE_TO_ENEMY = 25;
    /**
     * http://www.rpgmakervx.net/lofiversion/index.php/t8973.html
     */
    public static BufferedImage projectileImage;
    private boolean destroyProjectile;

    public Projectile(Tile position, Tile target, Level currentLevel) {
        super(position, currentLevel, 0, NUM_FRAMES, 50);
        this.target = target;
        destroyProjectile = false;
        moveToTarget();
        
        speed = 2.0;

        // Dont go to a default frame when the projectile stops
        xDefaultFrame = -1;
        yDefaultFrame = -1;
    }

    @Override
    void update() {
        if (currentLevel == null || position == null) {
            return;
        }
        if (animationComplete) {

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

        updateAnimation();
    }

    @Override
    void draw(Graphics2D g) {
        if (projectileImage != null && position != null) {
            BufferedImage frame = getCurrentFrame(projectileImage);
            if (frame != null && doMove(g, frame)) {
                drawImage(g, frame, position.getXPixels(),
                        position.getYPixels());
            }
        }
    }

    @Override
    protected Area getLight() {
        int x = position.getXPixels() + xMove + Tile.TILE_WIDTH / 2;
        int y = position.getYPixels() + yMove + Tile.TILE_HEIGHT / 2;

        return new Area(new Ellipse2D.Float(x - lightRadius / 2, y
                - lightRadius / 2, lightRadius, lightRadius));
    }

    public boolean getDestroyProjectile() {
        return destroyProjectile;
    }
    
    public void destroyProjectile() {
        destroyProjectile = true;
    }
}
