
import java.awt.image.BufferedImage;

/**
 * This class is a subclass of Enemy that moves quite a bit faster than the
 * Player. The SearchEnemy chooses a path to the Player, follows it, waits for a
 * certain amount of time, and then picks another path to the Player.
 *
 * June 17, 2013
 *
 * @author Braden Watling
 */
public class SearchEnemy extends Enemy {

    /**
     * This represents the Enemies' sprite sheet.
     */
    public static BufferedImage searchEnemyImage;
    /**
     * This represents the number of frames in the Enemy's sprite sheet.
     */
    public static final int NUM_FRAMES = 2;
    /**
     * The speed, compared to the Player, at which this Enemy moves at.
     */
    public static final double SPEED = 2.0;
    /**
     * The damage, health, and retarget time of this Enemy.
     *
     * The retarget time represents the amount of time that the Enemy must wait
     * before choosing a new target.
     */
    public static final int DAMAGE = 4, HEALTH = 100, RETARGET_TIME = 750;

    /**
     * Create a new SearchEnemy based on the following parameters.
     *
     * @param position The position Tile of the SearchEnemy
     * @param currentLevel A reference to the current Level
     * @param player A reference to the Player
     */
    public SearchEnemy(Tile position, Level currentLevel, Player player) {
        super(position, currentLevel, player, searchEnemyImage, NUM_FRAMES, SPEED, DAMAGE, HEALTH, RETARGET_TIME);
    }

    /**
     * This method overrides Enemy's updatePath() method. It provides a separate
     * method of moving towards the Player. The SearchEnemy only allows the path
     * to player to be updated when the path has been completely executed. This
     * means that the SearchEnemy chooses its path, follows it, waits the
     * RETARGET_TIME, and then picks another path.
     *
     * @return Whether or not the path to the Player should be updated.
     */
    protected boolean updatePath() {
        return pathToPlayer == null || pathToPlayer.isEmpty();
    }
}
