
import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

public class HUDPanel extends JPanel {

    private static final int HEALTH_WIDTH = 300, HEALTH_HEIGHT = 20;
    Player player;
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
    }

    private void drawHealthBar(Graphics2D g, int x, int y, int health) {
        
        int healthWidth = health * HEALTH_WIDTH / Actor.MAX_HEALTH;
        if(health < 0) {
            healthWidth = 0;
        } else if(health > Actor.MAX_HEALTH) {
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
