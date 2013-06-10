
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class HUDPanel extends JPanel {

    private static final int HEALTH_WIDTH = 300, HEALTH_HEIGHT = 20;
    /**
     * http://shmector.com/medium/other/rating-stars_500x500.png
     */
    public static BufferedImage ratingImage;
    Player player;
    Level currentLevel;
    ArrayList<Enemy> enemies;
    Task currentTask;

    class Task extends TimerTask {

        @Override
        public void run() {
            repaint();
        }
    }

    public HUDPanel(Player player, ArrayList<Enemy> enemies) {
        this.player = player;
        this.enemies = enemies;
    }

    public void startThread(Timer timer) {
        if (currentTask != null) {
            currentTask.cancel();
        }

        currentTask = new Task();
        timer.scheduleAtFixedRate(currentTask, 0, 1000 / MainApplet.FPS);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        int x = 3, y = 3;

        drawHealthBar(g2d, x, y, player.getHealth());
        x = getWidth() - HEALTH_WIDTH - 3;

        //This represents the number of enemy health bars we are going to draw
        int numDrawEnemy = enemies.size();
        //Limit it to the max number of enemies
        if (numDrawEnemy > Level.MAX_ENEMIES) {
            numDrawEnemy = Level.MAX_ENEMIES;
        }

        for (int i = 0; i < numDrawEnemy; i++) {
            Enemy enemy = enemies.get(i);
            drawHealthBar(g2d, x, y, enemy.getHealth());
            y += HEALTH_HEIGHT + 3;
        }
        
        drawDifficulty(g2d, 25, 25);
    }
    
    public void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
    }

    private void drawDifficulty(Graphics2D g, int x, int y) {
        if(ratingImage == null || currentLevel == null) {
            return;
        }
        
        double difficulty = currentLevel.getDifficulty();

        int truncated = (int) difficulty;
        double decimal = difficulty - truncated;
        int difference = Level.MAX_DIFFICULTY - truncated - (decimal != 0.0 ? 1 : 0);

        int width = ratingImage.getWidth() / 3, height = ratingImage.getHeight();

        BufferedImage fullStar = ratingImage.getSubimage(0, 0, width, height);
        BufferedImage halfStar = ratingImage.getSubimage(width, 0, width, height);
        BufferedImage noStar = ratingImage.getSubimage(width * 2, 0, width, height);

        int n = 0;
        
        for (int i = 0; i < truncated; i++) {
            g.drawImage(fullStar, x + n++ * width, y, null);
        }
        
        if(decimal != 0.0) {
            g.drawImage(halfStar, x + n++ * width, y, null);
        }

        for (int i = 0; i < difference; i++) {
            g.drawImage(noStar, x + n++ * width, y, null);
        }
    }

    private void drawHealthBar(Graphics2D g, int x, int y, int health) {

        int healthWidth = health * HEALTH_WIDTH / Actor.MAX_HEALTH;
        if (health < 0) {
            healthWidth = 0;
        } else if (health > Actor.MAX_HEALTH) {
            healthWidth = HEALTH_WIDTH;
        }

        g.setColor(new Color(255, 0, 0));
        g.fillRoundRect(x, y, HEALTH_WIDTH, HEALTH_HEIGHT, 10, 10);

        g.setColor(new Color(0, 175, 0));
        g.fillRoundRect(x, y, healthWidth, HEALTH_HEIGHT, 10, 10);

        String healthString = health + "/" + Actor.MAX_HEALTH;
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        g.setColor(Color.BLACK);
        g.drawString(healthString, x + HEALTH_WIDTH / 2 - metrics.stringWidth(healthString) / 2, y + HEALTH_HEIGHT / 2 + metrics.getHeight() / 2 - 3);
    }
}
