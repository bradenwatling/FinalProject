
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.*;

/**
 * The HUDPanel class acts as a heads-up-display for the user. It shows the
 * current difficulty of the Level, as well as health bars for the Player and
 * each Enemy.
 *
 * June 17, 2013
 *
 * @author Braden Watling
 */
public class HUDPanel extends JPanel {

    /**
     * The dimensions of a health bar. OFFSET represents the spacing between
     * health bars.
     */
    private static final int HEALTH_WIDTH = 300, HEALTH_HEIGHT = 20, OFFSET = 3;
    /**
     * These are the instructions that are displayed in the message box when the
     * instructions button is pressed.
     */
    private static final String instructionsText = "<html>Welcome to the game!<br>"
            + "Here's how to play:<br>"
            + "Use 'w', 'a', 's', and 'd' to move around.<br>"
            + "Press spacebar to shoot a projectile.<br>"
            + "In order to advance to the next difficulty, destroy all enemies on the map, or clear the map of all darkness.<br />"
            + "If you lose all your health, you will be sent back a difficulty.<br>"
            + "Press 'Play/Pause' to begin.<br>"
            + "Good luck!</html>";
    /**
     * The sprite sheet for the difficulty rating system.
     *
     * Source: http://shmector.com/medium/other/rating-stars_500x500.png
     */
    public static BufferedImage ratingImage;
    /**
     * The color of the health (foreground color).
     */
    public static final Color HEALTH_COLOR = new Color(0, 175, 0);
    /**
     * A reference to the current Level.
     */
    Level currentLevel;
    /**
     * A reference to the Player.
     */
    Player player;
    /**
     * A reference to the ArrayList of Enemies.
     */
    ArrayList<Enemy> enemies;
    /**
     * The separate images stored in the sprite sheet of ratingImage.
     */
    BufferedImage fullStar, halfStar, emptyStar;

    /**
     * Create a HUDPanel based on the following parameters.
     *
     * @param player A reference to the Player.
     * @param enemies A reference to the ArrayList of Enemies.
     */
    public HUDPanel(Player player, ArrayList<Enemy> enemies) {
        this.player = player;
        this.enemies = enemies;

        //There are 3 images within ratingImage, all side by side horizontally
        int width = ratingImage.getWidth() / 3, height = ratingImage.getHeight();

        //Get each one, in the order full, half, empty
        fullStar = ratingImage.getSubimage(0, 0, width, height);
        halfStar = ratingImage.getSubimage(width, 0, width, height);
        emptyStar = ratingImage.getSubimage(width * 2, 0, width, height);
    }

    /**
     * This method is responsible for drawing all aspects of the HUD, including
     * health bars and difficulty rating.
     *
     * @param g The Graphics object to draw to.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //Cast to Graphics2D for more functionality
        Graphics2D g2d = (Graphics2D) g;

        int x = OFFSET, y = OFFSET;

        drawHealthBar(g2d, x, y, player.getHealth());
        x = getWidth() - HEALTH_WIDTH - OFFSET;

        //This represents the number of enemy health bars we are going to draw
        int numDrawEnemy = enemies.size();
        //Limit it to the max number of enemies
        if (numDrawEnemy > Level.MAX_ENEMIES) {
            numDrawEnemy = Level.MAX_ENEMIES;
        }

        //For each health bar
        for (int i = 0; i < numDrawEnemy; i++) {
            Enemy enemy = enemies.get(i);
            //Draw it
            drawHealthBar(g2d, x, y, enemy.getHealth());
            //Increment y by the height of the health and leave OFFSET between each bar
            y += HEALTH_HEIGHT + OFFSET;
        }

        //Draw the difficulty rating
        drawDifficulty(g2d, 25, 25);
    }

    /**
     * This method is responsible for drawing the difficulty rating of the
     * Level.
     *
     * @param g The Graphics2D object to draw to.
     * @param x The x-position of the difficulty rating.
     * @param y The y-position of the difficulty rating.
     */
    private void drawDifficulty(Graphics2D g, int x, int y) {
        //Image and Level must not be null.
        if (ratingImage == null || currentLevel == null) {
            return;
        }

        //Get the difficulty
        double difficulty = currentLevel.getDifficulty();

        //Determine how many full stars to draw by truncating the decimal
        int numFull = (int) difficulty;

        //Determine whether or not this is a half difficulty by subtracting the
        //trucated integer from the original double
        boolean isHalf = (difficulty - numFull) != 0.0;

        //Determine the difference, meaning the number of empty stars to draw
        int numEmpty = Level.MAX_DIFFICULTY - numFull;

        //Decrement numEmpty if it's a half level
        if (isHalf) {
            numEmpty--;
        }

        int width = fullStar.getWidth();

        //This represents the number of stars that have been drawn so far
        int n = 0;

        //Draw all the stars
        //As more stars are drawn, the x-position increases
        for (int i = 0; i < numFull; i++) {
            g.drawImage(fullStar, x + n++ * width, y, null);
        }

        if (isHalf) {
            g.drawImage(halfStar, x + n++ * width, y, null);
        }

        for (int i = 0; i < numEmpty; i++) {
            g.drawImage(emptyStar, x + n++ * width, y, null);
        }
    }

    /**
     * This method is responsible for drawing a health bar at the specified
     * location with the specified amount of health out of Actor.MAX_HEALTH.
     *
     * @param g The Graphics2D object to draw to.
     * @param x The x-position of the health bar.
     * @param y The y-position of the health bar.
     * @param health The amount of health that should be shown on the health
     * bar.
     */
    private void drawHealthBar(Graphics2D g, int x, int y, int health) {

        //Determine the width of the actual health
        int healthWidth = health * HEALTH_WIDTH / Actor.MAX_HEALTH;

        //Put limits so we don't draw something non-sensical
        if (health < 0) {
            healthWidth = 0;
        } else if (health > Actor.MAX_HEALTH) {
            healthWidth = HEALTH_WIDTH;
        }

        //Draw the health bar in red first
        g.setColor(Color.RED);
        g.fillRoundRect(x, y, HEALTH_WIDTH, HEALTH_HEIGHT, 10, 10);

        //Draw the actual amount of health in HEALTH_COLOR
        g.setColor(HEALTH_COLOR);
        g.fillRoundRect(x, y, healthWidth, HEALTH_HEIGHT, 10, 10);

        //Create a String for the health fraction
        String healthString = health + "/" + Actor.MAX_HEALTH;

        //Setup something to measure the width and height of the text
        FontMetrics metrics = g.getFontMetrics(g.getFont());

        int stringWidth = metrics.stringWidth(healthString), stringHeight = metrics.getHeight();

        //Draw the String
        g.setColor(Color.BLACK);
        //OFFSET must be subtracted because drawString()'s y value represents the bottom of the text
        g.drawString(healthString, x + HEALTH_WIDTH / 2 - stringWidth / 2, y + HEALTH_HEIGHT / 2 + stringHeight / 2 - OFFSET);
    }

    /**
     * This method is responsible for displaying a popup window that shows
     * instructions on how to play the game.
     */
    public static void showInstructions() {
        JOptionPane.showMessageDialog(null, instructionsText, "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Sets the current Level of the HUDPanel.
     *
     * @param currentLevel The new Level for the Panel
     */
    public void setCurrentLevel(Level newLevel) {
        this.currentLevel = newLevel;
    }
}
