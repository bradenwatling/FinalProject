
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class ContentPanel extends JPanel {

    private Level currentLevel;
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Projectile> projectiles;
    private Task currentTask;

    class Task extends TimerTask {

        @Override
        public void run() {
            requestFocus();

            if (player != null) {
                player.update();
            }

            for (int i = 0; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);
                if (enemy != null) {
                    enemy.update();

                    if (enemy.getPosition().equals(player.position)) {
                        player.doDamage(enemy.getDamageAmount());
                    }
                    
                    for (int a = 0; a < projectiles.size(); a++) {
                        Projectile projectile = projectiles.get(a);
                        
                        if(projectile.getPosition().equals(enemy.position) || (projectile.getTarget() != null && projectile.getTarget().equals(enemy.position))) {
                            //If the projectile hasn't already damaged an enemy
                        	if(!projectile.getDestroyProjectile()) {
                            	enemy.doDamage(Projectile.DAMAGE_TO_ENEMY);
                                projectile.destroyProjectile();
                            }
                        }
                    }
                    
                    if(enemy.isDead()) {
                        enemies.remove(enemy);
                    }
                }
            }

            for (int i = 0; i < projectiles.size(); i++) {
                Projectile projectile = projectiles.get(i);
                if (projectile != null) {
                    projectile.update();

                    if (projectile.getDestroyProjectile()) {
                        projectiles.remove(i--);
                        continue;
                    }
                }
            }

            updateLevel();

            repaint();
        }
    }

    public ContentPanel(Level currentLevel, Player player,
            ArrayList<Enemy> enemies, ArrayList<Projectile> projectiles) {
        this.currentLevel = currentLevel;
        this.player = player;
        this.enemies = enemies;
        this.projectiles = projectiles;

        addKeyListener(player);
    }

    public void start(Timer timer) {
        if (currentTask != null) {
            currentTask.cancel();
        }
        currentTask = new Task();
        timer.scheduleAtFixedRate(currentTask, 0, 1000 / MainApplet.FPS);
    }

    public void cancel() {
        if (currentTask != null) {
            currentTask.cancel();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = ((Graphics2D) g);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (currentLevel == null || player == null) {
            return;
        }

        currentLevel.draw(g2d);

        player.draw(g2d);

        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy != null) {
                enemy.draw(g2d);
            }
        }

        for (int i = 0; i < projectiles.size(); i++) {
            Projectile projectile = projectiles.get(i);
            if (projectile != null) {
                projectile.draw(g2d);
            }
        }

        g2d.setColor(Level.LIGHT_COLOR);
        Area totalLitArea = new Area();

        Area litArea = currentLevel.getLitArea();
        Area tempLitArea = currentLevel.getTempLitArea();

        if (litArea != null) {
            totalLitArea.add(litArea);
        }

        if (tempLitArea != null) {
            totalLitArea.add(tempLitArea);
        }

        g2d.fill(totalLitArea);
    }

    public void updateLevel() {
        if (currentLevel == null || player == null) {
            return;
        }


        Area currentLightArea = new Area();
        Area tempLightArea = new Area();

        currentLightArea.add(player.getLight());

        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy != null) {
                tempLightArea.add(enemy.getLight());
            }
        }
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile projectile = projectiles.get(i);
            if (projectile != null) {
                tempLightArea.add(projectile.getLight());
            }
        }

        currentLevel.setCurrentLightArea(currentLightArea);
        currentLevel.setTempLitArea(tempLightArea);
    }

    void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
    }
}
