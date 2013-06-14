
import java.awt.image.BufferedImage;

/**
 *
 * @author Braden Watling
 */
public class HealthPowerUp extends PowerUp {

    /**
     * The image for the HealthPowerUp
     */
    public static BufferedImage healthPowerUpImage;
    /**
     * This represents how much health the Player gains when he interacts with
     * this HealthPowerUp
     */
    private int healAmount;

    public HealthPowerUp(Tile position, int healAmount) {
        super(position, healthPowerUpImage);

        this.healAmount = healAmount;
    }

    @Override
    public void doPowerUp(Actor actor) {
        //Only Players can use this powerup
        if (actor instanceof Player) {
            actor.addHealth(healAmount);
            destroyed = true;
        }
    }
}
