
import java.awt.image.BufferedImage;

/**
 *
 * @author Braden
 */
public class RandomEnemy extends Enemy {

    public static final double SPEED = 1.5;
    public static final int DAMAGE = 3, HEALTH = 100, RANDOM_CHOICE = 4, RETARGET_TIME = 0;
    public static BufferedImage randomEnemyImage;
    public static final int NUM_FRAMES = 2;

    public RandomEnemy(Tile position, Level currentLevel, Player player) {
        super(position, currentLevel, player, NUM_FRAMES, randomEnemyImage, SPEED, DAMAGE, HEALTH, RANDOM_CHOICE, RETARGET_TIME);
    }
}
