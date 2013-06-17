
import java.awt.image.BufferedImage;

/**
 * This class is a PowerUp that heals the Player when the Player interacts with
 * it.
 *
 * June 17, 2013
 *
 * @author Braden Watling
 */
public class HealthPowerUp extends PowerUp {

    /**
     * The image for the HealthPowerUp.
     */
    public static BufferedImage healthPowerUpImage;
    /**
     * This represents how much health the Player gains when he interacts with
     * this HealthPowerUp.
     */
    private int healAmount;

    /**
     * Create a new HealthPowerUp based on the following parameters.
     *
     * @param position The position Tile of the HealthPowerUp.
     * @param healAmount The amount of health that the HealthPowerUp heals.
     */
    public HealthPowerUp(Tile position, int healAmount) {
        super(position, healthPowerUpImage);

        this.healAmount = healAmount;
    }

    @Override
    /**
     * This method is responsible for handling interaction with an Actor.
     */
    public void doPowerUp(Actor actor) {
        //Only Players can use this powerup
        if (actor instanceof Player) {
            actor.addHealth(healAmount);
            this.destroyed = true;
        }
    }
}
