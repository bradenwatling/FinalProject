package finalproject;


import java.awt.image.BufferedImage;

/**
 * This class is a PowerUp that gives the Player a speed boost when the Player
 * interacts with it.
 *
 * June 17, 2013
 *
 * @author Braden Watling
 */
public class SpeedPowerUp extends PowerUp {

    /**
     * The image for the SpeedPowerUp.
     */
    public static BufferedImage speedPowerUpImage;
    /**
     * This represents the duration of the speed power up.
     */
    private static final int SPEED_POWER_UP_DURATION = 2000;

    /**
     * Create a new SpeedPowerUp on the specified Tile.
     *
     * @param position The Tile to place the SpeedPowerUp on.
     */
    public SpeedPowerUp(Tile position) {
        super(position, speedPowerUpImage);
    }

    @Override
    /**
     * This method is responsible for handling interaction with an Actor.
     */
    public void doPowerUp(Actor actor) {
        if (actor instanceof Player) {
            actor.addSpeedBonus(SPEED_POWER_UP_DURATION);
            destroyed = true;
        }
    }
}
