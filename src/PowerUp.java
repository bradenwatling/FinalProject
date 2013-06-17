
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * This class represents a PowerUp that gives an Actor a bonus that is defined
 * by a subclass of this class.
 * 
 * @author Braden Watling
 */

public abstract class PowerUp {

    /**
     * The position of the PowerUp in the Level.
     */
    Tile position;
    /**
     * Whether or not the PowerUp is destroyed.
     */
    boolean destroyed;
    /**
     * The image for the PowerUp.
     */
    BufferedImage powerUpImage;

    /**
     * Create a new PowerUp based on the following parameters.
     * 
     * @param position The position Tile of the PowerUp.
     * @param powerUpImage The image to display for the PowerUp.
     */
    public PowerUp(Tile position, BufferedImage powerUpImage) {
        this.position = position;
        this.destroyed = false;
        this.powerUpImage = powerUpImage;
    }

    /**
     * This method is responsible for drawing the PowerUp to the screen.
     * 
     * @param g The Graphics2D object to draw to.
     */
    public void draw(Graphics2D g) {
        //If it's not null, draw it.
        if (powerUpImage != null) {
            g.drawImage(powerUpImage, position.getXPixels(),
                    position.getYPixels(), null);
        }
    }

    /**
     * This method is responsible for handling interaction with an Actor.
     */
    public abstract void doPowerUp(Actor actor);

    /**
     * Gets the position of the PowerUp
     * @return The position Tile of the PowerUp.
     */
    public Tile getPosition() {
        return position;
    }

    /**
     * Determines whether or not the PowerUp is destroyed.
     * 
     * @return Whether or not the PowerUp is destroyed.
     */
    public boolean getDestroyed() {
        return destroyed;
    }
}
