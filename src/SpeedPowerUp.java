import java.awt.image.BufferedImage;

public class SpeedPowerUp extends PowerUp {
	
	public static BufferedImage speedPowerUpImage;
	private static final int SPEED_POWER_UP_DURATION = 2000;

	public SpeedPowerUp(Tile position) {
		super(position, speedPowerUpImage);
	}

	@Override
	public void doPowerUp(Actor actor) {
		if (actor instanceof Player) {
			actor.addSpeedBonus(SPEED_POWER_UP_DURATION);
			destroyed = true;
		}
	}

}
