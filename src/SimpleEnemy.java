
import java.awt.image.BufferedImage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Braden
 */
public class SimpleEnemy extends Enemy {
    
    public static final double SPEED = 0.75;
    public static final int DAMAGE = 6, RANDOM_CHOICE = 0;
    
    public static BufferedImage simpleEnemyImage;
    
    public SimpleEnemy(Tile position, Level currentLevel, int health, Player player) {
        super(position, currentLevel, health, player);
        
        this.drawImage = simpleEnemyImage;
        this.speed = SPEED;
        this.damage = DAMAGE;
        this.randomChoice = RANDOM_CHOICE;
    }
}
