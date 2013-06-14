import java.awt.image.BufferedImage;

public class HealthPowerUp extends PowerUp {

	public static BufferedImage healthPowerUpImage;
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
