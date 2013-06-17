
import java.awt.image.BufferedImage;

/**
 * This class is a subclass of Enemy that moves a fair amount faster than the
 * Player. The RandomEnemy, however, has a certain probability of choosing a
 * random direction to move in, rather than sticking to the path to the Player.
 *
 * June 17, 2013
 *
 * @author Braden Watling
 */
public class RandomEnemy extends Enemy {

    /**
     * This represents the Enemies' sprite sheet.
     */
    public static BufferedImage randomEnemyImage;
    /**
     * This represents the number of frames in the Enemy's sprite sheet.
     */
    public static final int NUM_FRAMES = 2;
    /**
     * The speed, compared to the Player, at which this Enemy moves at.
     */
    public static final double SPEED = 1.5;
    /**
     * The damage, health, random factor, and retarget time of this Enemy.
     *
     * The random factor represents the number of times out of 10 that the Enemy
     * will choose a random direction to move in.
     *
     * The retarget time represents the amount of time that the Enemy must wait
     * before choosing a new target.
     */
    public static final int DAMAGE = 3, HEALTH = 100, RANDOM_CHOICE = 4, RETARGET_TIME = 0;

    /**
     * Create a new RandomEnemy based on the following parameters.
     *
     * @param position The position Tile of the RandomEnemy
     * @param currentLevel A reference to the current Level
     * @param player A reference to the Player
     */
    public RandomEnemy(Tile position, Level currentLevel, Player player) {
        super(position, currentLevel, player, randomEnemyImage, NUM_FRAMES, SPEED, DAMAGE, HEALTH, RETARGET_TIME);
    }

    @Override
    /**
     * This function determines whether or not the RandomEnemy should choose a
     * random direction or not.
     *
     * @return Whether or not a random Tile should be chosen.
     */
    protected boolean chooseRandom() {
        //The RandomEnemy will choose a random Tile RANDOM_CHOICE times out of 10
        return Math.random() * 10 < RANDOM_CHOICE;
    }
}
