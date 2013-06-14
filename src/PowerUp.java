import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public abstract class PowerUp {
	Tile position;
	boolean destroyed;
	BufferedImage powerUpImage;

	public PowerUp(Tile position, BufferedImage powerUpImage) {
		this.position = position;
		this.destroyed = false;
		this.powerUpImage = powerUpImage;
	}

	public void draw(Graphics2D g) {
		if (powerUpImage != null) {
			g.drawImage(powerUpImage, position.getXPixels(),
					position.getYPixels(), null);
		}
	}

	public abstract void doPowerUp(Actor actor);

	public Tile getPosition() {
		return position;
	}

	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}

	public boolean getDestroyed() {
		return destroyed;
	}
}
