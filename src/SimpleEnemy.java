
import java.awt.image.BufferedImage;

/**
 * This class is a subclass of Enemy that simply follows a direct path to the
 * Player, but moves much slower than the Player.
 *
 * @author Braden Watling
 */
public class SimpleEnemy extends Enemy {

    /**
     * This represents the Enemies' sprite sheet.
     */
    public static BufferedImage simpleEnemyImage;
    /**
     * This represents the number of frames in the Enemy's sprite sheet.
     */
    public static final int NUM_FRAMES = 2;
    /**
     * The speed, compared to the Player, at which this Enemy moves at.
     */
    public static final double SPEED = 0.75;
    /**
     * The damage, health, random factor, and retarget time of this Enemy.
     *
     * The random factor represents the number of times out of 10 that the Enemy
     * will choose a random direction to move in.
     *
     * The retarget time represents the amount of time that the Enemy must wait
     * before choosing a new target.
     */
    public static final int DAMAGE = 6, HEALTH = 100, RETARGET_TIME = 0;

    /**
     * Create a new SimpleEnemy based on the following parameters.
     *
     * @param position The position Tile of the SimpleEnemy
     * @param currentLevel A reference to the current Level
     * @param player A reference to the Player
     */
    public SimpleEnemy(Tile position, Level currentLevel, Player player) {
        super(position, currentLevel, player, simpleEnemyImage, NUM_FRAMES, SPEED, DAMAGE, HEALTH, RETARGET_TIME);
    }
}
