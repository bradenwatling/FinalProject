
import java.awt.image.BufferedImage;

/**
 *
 * @author Braden
 */
public class SearchEnemy extends Enemy {

    public static final double SPEED = 2.0;
    public static final int DAMAGE = 4, HEALTH = 100, RANDOM_CHOICE = 0, RETARGET_TIME = 750;
    public static BufferedImage searchEnemyImage;
    public static final int NUM_FRAMES = 2;

    public SearchEnemy(Tile position, Level currentLevel, Player player) {
        super(position, currentLevel, player, NUM_FRAMES, searchEnemyImage, SPEED, DAMAGE, HEALTH, RANDOM_CHOICE, RETARGET_TIME);
    }

    protected boolean updatePath() {
        return pathToPlayer == null || pathToPlayer.isEmpty();
    }
}
