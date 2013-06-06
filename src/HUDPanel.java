
import java.awt.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

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
    
    public void start(Timer timer) {
        if (currentTask != null) {
            currentTask.cancel();
        }
        currentTask = new HUDPanel.Task();
        timer.scheduleAtFixedRate(currentTask, 0, 1000 / MainApplet.FPS);
    }
    
    public void cancel() {
        if(currentTask != null) {
            currentTask.cancel();
        }
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;

        int x = 3, y = 3;
        
        drawHealthBar(g2d, x, y, player.getHealth());
        y += HEALTH_HEIGHT + 3;
        
        for(int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            drawHealthBar(g2d, x, y, enemy.getHealth());
            y += HEALTH_HEIGHT + 3;
        }
    }
    
    private void drawHealthBar(Graphics2D g, int x, int y, int health) {
        g.setColor(new Color(255, 0, 0));
        g.fillRoundRect(x, y, HEALTH_WIDTH, HEALTH_HEIGHT, 10, 10);
        g.setColor(new Color(0, 175, 0));
        g.fillRoundRect(x, y, health * HEALTH_WIDTH / Actor.MAX_HEALTH, HEALTH_HEIGHT, 10, 10);
    }
}
