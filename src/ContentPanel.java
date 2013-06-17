
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JPanel;

/**
 * This class represents the JPanel on which the bulk of the game takes place.
 *
 * @author Braden Watling
 */
public class ContentPanel extends JPanel {

    /**
     * This is the delay between one level ending and the next starting.
     */
    public static final int END_LEVEL_DELAY = 1000;
    /**
     * A reference to the Applet.
     */
    private MainApplet mainApplet;
    /**
     * A reference to the HUDPanel.
     */
    private HUDPanel hudPanel;
    /**
     * A reference to the current Level. This must be renewed with new Levels.
     */
    private Level currentLevel;
    /**
     * A reference to the Player, this should never be renewed.
     */
    private Player player;
    /**
     * A reference to the Enemy ArrayList, this should never be renewed.
     */
    private ArrayList<Enemy> enemies;
    /**
     * A reference to the Projectile ArrayList, this should never be renewed.
     */
    private ArrayList<Projectile> projectiles;
    /**
     * A reference to the current Task. This essentially represents the current
     * thread.
     */
    private Task currentTask;
    /**
     * Whether or not the game is running vs paused. (running = true, paused =
     * false)
     */
    private boolean runState;
    /**
     * Whether or not the Level win screen should be displayed.
     */
    private boolean showLevelWinScreen;
    /**
     * Whether or not the Level lose screen should be displayed.
     */
    private boolean showLevelLoseScreen;

    /**
     * This class represents the update thread for the ContentPanel.
     */
    class Task extends TimerTask {

        @Override
        /**
         * This method is called MainApplet.FPS times per second and is
         * responsible for updating all elements of the game, including the
         * Player, the Enemies, the Projectiles, and doing any collision or
         * other logic between them.
         */
        public void run() {
            if (runState) {
                //Keep focus on the ContentPanel for the keyboard
                requestFocus();

                //All code in this if statement required player to not be null.
                if (player != null) {
                    //Update the player
                    player.update();

                    //If the player's dead, end the level
                    if (player.isDead()) {
                        mainApplet.endLevel(false);
                    }

                    //Do the following for all PowerUps in the Level
                    ArrayList<PowerUp> powerUps = currentLevel.getPowerUps();
                    for (int i = 0; i < powerUps.size(); i++) {
                        PowerUp powerUp = powerUps.get(i);
                        if (powerUp != null) {
                            //If the player collides with a PowerUp
                            if (powerUp.getPosition().equals(
                                    player.getPosition())) {
                                powerUp.doPowerUp(player);
                            }

                            //If the PowerUp must be destroyed
                            if (powerUp.getDestroyed()) {
                                powerUps.remove(i--);
                            }
                        }
                    }

                    //Do the following for all Enemies
                    for (int i = 0; i < enemies.size(); i++) {
                        Enemy enemy = enemies.get(i);
                        if (enemy != null) {
                            //Update the Enemy
                            enemy.update();

                            Tile enemyPosition = enemy.getPosition();

                            //Check collision between Enemy and Player
                            if (enemyPosition != null) {
                                if (enemyPosition.equals(player.position)) {
                                    player.doDamage(enemy.getDamageAmount());
                                }
                            }

                            //Do PowerUps for all Enemies (although there is no
                            //PowerUp for Enemies, this provides infrastructure
                            //for there to be).
                            for (int a = 0; a < powerUps.size(); a++) {
                                PowerUp powerUp = powerUps.get(a);
                                if (powerUp != null) {
                                    if (powerUp.getPosition().equals(
                                            enemy.getPosition())) {
                                        powerUp.doPowerUp(enemy);
                                    }
                                }
                            }

                            //Check collisions with Projectiles
                            for (int a = 0; a < projectiles.size(); a++) {
                                Projectile projectile = projectiles.get(a);

                                //Check both Projectile position and Enemy position
                                //as well as Projectile target and Enemy position
                                boolean didCollide = projectile.getPosition().equals(enemyPosition)
                                        || (projectile.getTarget() != null && projectile.getTarget().equals(enemyPosition));

                                //If they collided
                                if (didCollide) {
                                    // If the projectile hasn't already been
                                    // destroyed
                                    if (!projectile.getDestroyProjectile()) {
                                        //Damage Enemy and destroy the Projectile
                                        enemy.doDamage(Projectile.DAMAGE_TO_ENEMY);
                                        projectile.destroyProjectile();
                                    }
                                }
                            }

                            //Remove any dead Enemies
                            if (enemy.isDead()) {
                                enemies.remove(enemy);
                            }
                        }
                    }

                    //If all Enemies are dead
                    if (enemies.size() <= 0) {
                        // Win
                        mainApplet.endLevel(true);
                    }
                }

                //Do the following for all Projectiles
                for (int i = 0; i < projectiles.size(); i++) {
                    Projectile projectile = projectiles.get(i);
                    if (projectile != null) {
                        //If the projectile isn't null, update it
                        projectile.update();

                        //Remove any destroyed Projectiles
                        if (projectile.getDestroyProjectile()) {
                            projectiles.remove(i--);
                        }
                    }
                }
            }

            //Keep the area that is lit up to date
            updateLights();

            //Request for the Panels to be repainted
            repaint();
            hudPanel.repaint();
        }
    }

    /**
     * Create a new ContentPanel based on the following parameters.
     *
     * @param mainApplet A reference the the Applet.
     * @param currentLevel A reference to the current Level.
     * @param player A reference to the Player.
     * @param enemies A reference to the ArrayList of Enemies.
     * @param projectiles A reference to the ArrayList of Projectiles.
     */
    public ContentPanel(MainApplet mainApplet, HUDPanel hudPanel, Level currentLevel,
            Player player, ArrayList<Enemy> enemies,
            ArrayList<Projectile> projectiles) {
        super();

        this.mainApplet = mainApplet;
        this.hudPanel = hudPanel;
        this.currentLevel = currentLevel;
        this.player = player;
        this.enemies = enemies;
        this.projectiles = projectiles;

        //Send keyboard information directly to the Player
        addKeyListener(player);
    }

    /**
     * This method is responsible for displaying the win/lose screen and
     * delaying for a period of time between Levels.
     *
     * @param win Whether or not the last Level was won.
     */
    public void doEndLevel(boolean win) {
        try {
            showLevelWinScreen = win;
            showLevelLoseScreen = !win;
            repaint();
            Thread.sleep(END_LEVEL_DELAY);
        } catch (Exception e) {
        }
        showLevelWinScreen = false;
        showLevelLoseScreen = false;
    }

    /**
     * This method cancels any existing tasks associated with the ContentPanel
     * and starts a new one at the proper FPS on the designated Timer.
     *
     * @param timer The Timer object to start a new Task on.
     */
    public void startThread(Timer timer) {
        if (currentTask != null) {
            currentTask.cancel();
        }

        currentTask = new Task();
        //Schedule the new Task at the FPS defined in MainApplet
        timer.scheduleAtFixedRate(currentTask, 0, 1000 / MainApplet.FPS);
    }

    /**
     * This method toggles the ContentPanel thread updating most things or not.
     * NOTE: by pausing during a game, any timer that is being kept track of in
     * terms of System.currentTimeMillis() will most likely be triggered the
     * instant the game is resumed. An example of this is the SearchEnemy. If
     * the game is paused with him paused, he will immediately chase after the
     * game is un-paused. This is the way it should be, and can be considered a
     * penalty for pausing the game.
     */
    public void toggleStartStop() {
        runState = !runState;

        if (player != null) {
            player.clearKeys();
        }
    }

    /**
     * Set the state of the ContentPanel - either running or stopped.
     *
     * @param runState Whether or not the ContentPanel should start running or
     * be stopped.
     */
    public void setRunState(boolean runState) {
        this.runState = runState;

        if (player != null) {
            player.clearKeys();
        }
    }

    /**
     * Gets the state of the ContentPanel - either running or stopped.
     *
     * @return Whether or not the ContentPanel is running or stopped.
     */
    public boolean getRunState() {
        return runState;
    }

    /**
     * This method is responsible for drawing everything that must be drawn on
     * the ContentPanel, including the Level, the Player, the Enemies, the
     * Projectiles, the PowerUps etc.
     *
     * @param g The Graphics object to draw to
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Cast to Graphics2D for more functionality
        Graphics2D g2d = ((Graphics2D) g);

        if (currentLevel != null) {
            //Translate the drawing area to the middle of the Panel
            int levelWidth = currentLevel.getWidthPixels(), levelHeight = currentLevel.getHeightPixels();
            g2d.translate((getWidth() - levelWidth) / 2, (getHeight() - levelHeight) / 2);
            g2d.setClip(0, 0, levelWidth, levelHeight);
        }

        //Turn on antialiasing. This makes the light circles look non-pixelated.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (showLevelWinScreen) {
            //If the win screen must be shown
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, currentLevel.getWidthPixels(), currentLevel.getHeightPixels());
        } else if (showLevelLoseScreen) {
            //If the lose screen must be shown
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0, currentLevel.getWidthPixels(), currentLevel.getHeightPixels());
        } else {
            //Make sure currentLevel and player are not null
            if (currentLevel == null || player == null) {
                return;
            }

            //Draw the Level first
            currentLevel.draw(g2d);

            //Draw the PowerUps
            ArrayList<PowerUp> powerUps = currentLevel.getPowerUps();
            for (int i = 0; i < powerUps.size(); i++) {
                PowerUp powerUp = powerUps.get(i);
                if (powerUp != null) {
                    powerUp.draw(g2d);
                }
            }

            //Draw the Player
            player.draw(g2d);

            //Draw the Enemies
            for (int i = 0; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);
                if (enemy != null) {
                    enemy.draw(g2d);
                }
            }

            //Draw the Projectiles
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

    /**
     * This method is responsible for collecting all light area that is lit up
     * by Enemies and Projectiles and the Player and sending it to the Level.
     */
    public void updateLights() {
        //Level and Player must not be null
        if (currentLevel == null || player == null) {
            return;
        }

        //Represents the portion of the Level that is lit for this iteration
        Area tempLightArea = new Area();

        if (player != null) {
            Area playerLight = player.getLight();
            currentLevel.setCurrentLightArea(playerLight);

            //Add the playerLight to the temp area so that there is no "flicker"
            //when the map is first displayed. This "flicker" is do to the
            //permLightArea not being update until the Level thread executes.
            tempLightArea.add(playerLight);
        }

        //Add the light for all Enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy != null) {
                tempLightArea.add(enemy.getLight());
            }
        }

        //Add the light for all Projectiles
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile projectile = projectiles.get(i);
            if (projectile != null) {
                tempLightArea.add(projectile.getLight());
            }
        }

        //The temp lit area is the area that does not expand over time.
        currentLevel.setTempLitArea(tempLightArea);
    }

    /**
     * This method is responsible for changing the Level that is currently being
     * displayed.
     *
     * @param newLevel The new Level to display and update.
     */
    void changeLevel(Level newLevel) {
        this.currentLevel = newLevel;
    }
}
