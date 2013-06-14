
import java.awt.image.BufferedImage;

/**
 *
 * @author Braden
 */
public class SimpleEnemy extends Enemy {
    
    public static final double SPEED = 0.75;
    public static final int DAMAGE = 6, HEALTH = 100, RANDOM_CHOICE = 0, RETARGET_TIME = 0;
    
    public static BufferedImage simpleEnemyImage;
    public static final int NUM_FRAMES = 2;
    
    public SimpleEnemy(Tile position, Level currentLevel, Player player) {
        super(position, currentLevel, player, NUM_FRAMES);
        
        this.spriteSheetImage = simpleEnemyImage;
        this.speed = SPEED;
        this.damage = DAMAGE;
        this.health = HEALTH;
        this.randomChoice = RANDOM_CHOICE;
        this.retargetTime = RETARGET_TIME;
    }
}
