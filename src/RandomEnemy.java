
import java.awt.image.BufferedImage;

/**
 *
 * @author Braden
 */
public class RandomEnemy extends Enemy {

    public static final double SPEED = 1.5;
    public static final int DAMAGE = 3, HEALTH = 100, RANDOM_CHOICE = 4, RETARGET_TIME = 0;
    public static BufferedImage randomEnemyImage;

    public RandomEnemy(Tile position, Level currentLevel, Player player) {
        super(position, currentLevel, player);

        this.drawImage = randomEnemyImage;
        this.speed = SPEED;
        this.damage = DAMAGE;
        this.health = HEALTH;
        this.randomChoice = RANDOM_CHOICE;
        this.retargetTime = RETARGET_TIME;
    }
}
