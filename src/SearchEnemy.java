
import java.awt.image.BufferedImage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Braden
 */
public class SearchEnemy extends Enemy {

    public static final double SPEED = 2.0;
    public static final int DAMAGE = 7, RANDOM_CHOICE = 0;
    public static BufferedImage searchEnemyImage;

    public SearchEnemy(Tile position, Level currentLevel, int health, Player player) {
        super(position, currentLevel, health, player);

        this.drawImage = searchEnemyImage;
        this.speed = SPEED;
        this.damage = DAMAGE;
        this.randomChoice = RANDOM_CHOICE;
    }

    protected void updatePath() {
        if (pathToPlayer == null || pathToPlayer.isEmpty()) {
            pathToPlayer = currentLevel.getPath(position,
                    player.getPosition());
            long now = System.currentTimeMillis();
            timeSinceRetarget = now;
        }
    }
}
