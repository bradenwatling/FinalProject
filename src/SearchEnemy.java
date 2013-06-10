
import java.awt.image.BufferedImage;

/**
 *
 * @author Braden
 */
public class SearchEnemy extends Enemy {

    public static final double SPEED = 2.0;
    public static final int DAMAGE = 4, HEALTH = 100, RANDOM_CHOICE = 0, RETARGET_TIME = 750;
    public static BufferedImage searchEnemyImage;

    public SearchEnemy(Tile position, Level currentLevel, Player player) {
        super(position, currentLevel, player);

        this.drawImage = searchEnemyImage;
        this.speed = SPEED;
        this.damage = DAMAGE;
        this.health = HEALTH;
        this.randomChoice = RANDOM_CHOICE;
        this.retargetTime = RETARGET_TIME;
    }

    protected boolean updatePath() {
        return pathToPlayer == null || pathToPlayer.isEmpty();
    }
}
