
import java.awt.image.BufferedImage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Braden
 */
public class RandomEnemy extends Enemy {

    public static final double SPEED = 1.5;
    public static final int DAMAGE = 3, RANDOM_CHOICE = 5, RETARGET_TIME = 0;
    public static BufferedImage randomEnemyImage;

    public RandomEnemy(Tile position, Level currentLevel, int health, Player player) {
        super(position, currentLevel, health, player);

        this.drawImage = randomEnemyImage;
        this.speed = SPEED;
        this.damage = DAMAGE;
        this.randomChoice = RANDOM_CHOICE;
        this.retargetTime = RETARGET_TIME;
    }
}
