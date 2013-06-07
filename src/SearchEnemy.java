
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
    public static final int DAMAGE = 4, RANDOM_CHOICE = 0, RETARGET_TIME = 750;
    public static BufferedImage searchEnemyImage;

    public SearchEnemy(Tile position, Level currentLevel, int health, Player player) {
        super(position, currentLevel, health, player);

        this.drawImage = searchEnemyImage;
        this.speed = SPEED;
        this.damage = DAMAGE;
        this.randomChoice = RANDOM_CHOICE;
        this.retargetTime = RETARGET_TIME;
    }

    protected boolean updatePath() {
        return pathToPlayer == null || pathToPlayer.isEmpty();
    }
}
