
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

public class ContentPanel extends JPanel {

    /**
     * This is the delay between one level ending and the next starting
     */
    public static final int END_LEVEL_DELAY = 1000;
    private MainApplet mainApplet;
    private Level currentLevel;
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Projectile> projectiles;
    private Task currentTask;
    private boolean requestStart;
    private boolean showWinScreen;
    private boolean showLoseScreen;

    class Task extends TimerTask {

        @Override
        public void run() {
            if (requestStart) {
                requestFocus();

                if (player != null) {
                    player.update();

                    if (player.getHealth() <= 0) {
                        mainApplet.endLevel(false);
                    }

                    ArrayList<PowerUp> powerUps = currentLevel.getPowerUps();
                    for (int i = 0; i < powerUps.size(); i++) {
                        PowerUp powerUp = powerUps.get(i);
                        if (powerUp != null) {
                            if (powerUp.getPosition().equals(
                                    player.getPosition())) {
                                powerUp.doPowerUp(player);
                            }

                            if (powerUp.getDestroyed()) {
                                powerUps.remove(i--);
                            }
                        }
                    }

                    for (int i = 0; i < enemies.size(); i++) {
                        Enemy enemy = enemies.get(i);
                        if (enemy != null) {
                            enemy.update();

                            Tile enemyPosition = enemy.getPosition();

                            if (enemyPosition != null) {
                                if (enemyPosition.equals(player.position)) {
                                    player.doDamage(enemy.getDamageAmount());
                                }
                            }

                            for (int a = 0; a < powerUps.size(); a++) {
                                PowerUp powerUp = powerUps.get(a);
                                if (powerUp != null) {
                                    if (powerUp.getPosition().equals(
                                            enemy.getPosition())) {
                                        powerUp.doPowerUp(enemy);
                                    }
                                }
                            }

                            for (int a = 0; a < projectiles.size(); a++) {
                                Projectile projectile = projectiles.get(a);

                                if (projectile.getPosition().equals(
                                        enemyPosition)
                                        || (projectile.getTarget() != null && projectile
                                        .getTarget().equals(
                                        enemyPosition))) {
                                    // If the projectile hasn't already damaged
                                    // an enemy
                                    if (!projectile.getDestroyProjectile()) {
                                        enemy.doDamage(Projectile.DAMAGE_TO_ENEMY);
                                        projectile.destroyProjectile();
                                    }
                                }
                            }

                            if (enemy.isDead()) {
                                enemies.remove(enemy);
                            }
                        }
                    }

                    if (enemies.size() <= 0) {
                        // Win
                        mainApplet.endLevel(true);
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
            }

            updateLevel();

            repaint();
        }
    }

    public ContentPanel(MainApplet mainApplet, Level currentLevel,
            Player player, ArrayList<Enemy> enemies,
            ArrayList<Projectile> projectiles) {
        this.mainApplet = mainApplet;
        this.currentLevel = currentLevel;
        this.player = player;
        this.enemies = enemies;
        this.projectiles = projectiles;

        addKeyListener(player);
    }

    public void doEndLevel(boolean win) {
        try {
            showWinScreen = win;
            showLoseScreen = !win;
            repaint();
            Thread.sleep(END_LEVEL_DELAY);
        } catch (Exception e) {
        }
        showWinScreen = false;
        showLoseScreen = false;
    }

    public void startThread(Timer timer) {
        if (currentTask != null) {
            currentTask.cancel();
        }

        currentTask = new Task();
        timer.scheduleAtFixedRate(currentTask, 0, 1000 / MainApplet.FPS);
    }
    
    /**
     * This method toggles the ContentPanel thread updating most things or not.
     * NOTE: by pausing during a game, any timer that is being kept track of in
     * terms of System.currentTimeMillis() will most likely be triggered the
     * instant the game is resumed. An example of this is the SearchEnemy. If the
     * game is paused with him paused, he will immediately chase after the game
     * is un-paused. This is the way it should be, and can be considered a penalty
     * for pausing the game.
     */
    public void toggleStartStop() {
        requestStart = !requestStart;
    }

    /**
     * Sets requestStart to true, thereby allowing the ContentPanel thread to
     * operate normally. This acts like a "play" button.
     */
    public void start() {
        requestStart = true;
    }

    /**
     * Sets requestStart to false, thereby preventing the ContentPanel thread
     * from operating normally. This acts like a "pause" button.
     */
    public void stop() {
        requestStart = false;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = ((Graphics2D) g);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (showWinScreen) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, currentLevel.getWidthPixels(), currentLevel.getHeightPixels());
        } else if (showLoseScreen) {
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0, currentLevel.getWidthPixels(), currentLevel.getHeightPixels());
        } else {
            if (currentLevel == null || player == null) {
                return;
            }

            currentLevel.draw(g2d);

            ArrayList<PowerUp> powerUps = currentLevel.getPowerUps();

            for (int i = 0; i < powerUps.size(); i++) {
                PowerUp powerUp = powerUps.get(i);
                if (powerUp != null) {
                    powerUp.draw(g2d);
                }
            }

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

            //Draw the darkness and the light area on top of everything else
            currentLevel.drawLight(g2d);
        }
    }

    public void updateLevel() {
        if (currentLevel == null || player == null) {
            return;
        }

        Area currentLightArea = new Area();
        Area tempLightArea = new Area();

        if (player != null) {
            Area playerLight = player.getLight();
            currentLightArea.add(playerLight);
            //Add the playerLight to the temp area so that there is no "flicker"
            //when the map is first displayed. This "flicker" is do to the
            //permLightArea not being update until the Level thread executes.
            tempLightArea.add(playerLight);
        }

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
