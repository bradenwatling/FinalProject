package finalproject;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * The MainApplet class represent the Applet where the game occurs. It contains
 * all of the components associated with the game. It is also responsible for
 * resetting the Level when it must be reset.
 *
 * June 17, 2013
 *
 * @author Braden Watling
 */
public class MainApplet extends JApplet {

    /**
     * This represents whether debugging mode is enabled or not. By enabling
     * debugging mode, buttons are displayed to generate a new Level at any
     * time, as well as toggle the lights on and off in the Level.
     */
    public static final boolean DEBUGGING_MODE = false;
    /**
     * These represent constants relating the the Applet and different
     * Components.
     */
    public static final int FPS = 30, APPLET_WIDTH = 1024, APPLET_HEIGHT = 768, HUD_HEIGHT = 100;
    /**
     * This represents the amount that the difficulty increases when a Level is
     * completed.
     */
    public static final double DIFFICULTY_INCREMENT = 0.5;
    /**
     * This represents the Timer object used to schedule the Level and Content
     * threads.
     */
    private Timer timer;
    /**
     * This represents the heads up display JPanel.
     */
    private HUDPanel HUD;
    /**
     * This represents the JPanel containing the actual game.
     */
    private ContentPanel content;
    /**
     * This represents the Level that is current being played.
     */
    private Level currentLevel;
    /**
     * This is the Player object, which represents the user.
     */
    private Player player;
    /**
     * This is the ArrayList of Enemy objects in the Level.
     */
    private ArrayList<Enemy> enemies;
    /**
     * This is the ArrayList of Projectile objects in the Level.
     */
    private ArrayList<Projectile> projectiles;
    /**
     * This represents the difficulty of the Level. This is used to store
     * difficulties between one Level ending and the next starting. Difficulty
     * is increased or decreased by DIFFICULTY_INCREMENT when a Level ends.
     */
    private double difficulty;

    /**
     * This method is responsible for creating a new Level and resetting all
     * aspects of the game. This includes resetting the Player, the Enemy
     * ArrayList, the Projectile ArrayList etc.
     */
    public void createNewLevel() {
        // Can't make a new level without a content panel and a player
        if (content == null || player == null) {
            return;
        }

        //Empty projectiles and enemies. Only clear them because if they were set
        //to new ArrayLists, any references to the old ArrayLists would be destroyed
        if (projectiles != null) {
            projectiles.clear();
        }
        if (enemies != null) {
            enemies.clear();
        }

        if (timer != null) {
            //Cancel all tasks that are currently running
            timer.cancel();
        }
        //Since the timer either does not exist or is cancelled, create a new one
        timer = new Timer();

        //Make the width of the level based on the difficulty - it gets bigger the
        //closer it gets to Level.MAX_DIFFICULTY
        int newLevelWidth = Level.MIN_WIDTH + (int) ((difficulty / Level.MAX_DIFFICULTY)
                * (Level.MAX_WIDTH - Level.MIN_WIDTH));

        //Generate a new Level
        currentLevel = new Level(this, newLevelWidth,
                Level.HEIGHT, difficulty);

        //Update the panels with the currentLevel
        content.changeLevel(currentLevel);
        HUD.setCurrentLevel(currentLevel);

        /**
         * Reset the player to its default conditions Make sure the Player can
         * get to (0, 0) because that is where the Level generation algorithm
         * starts, and almost all Tiles are connected to that Tile Specify a
         * min. distance of 0 from (0, 0). This means the starting Tile is
         * random.
         */
        player.reset(currentLevel, currentLevel.getRandomTile(currentLevel.getTile(0, 0), 0));

        //The Level determines how many Enemies and PowerUps to make based on its difficulty
        currentLevel.addEnemies(enemies, player);
        currentLevel.addPowerUps(player.getPosition());

        //Let the user see the level before having to start right away
        content.setRunState(false);

        //Start threads
        currentLevel.startThread(timer);
        content.startThread(timer);
    }

    /**
     * This method is responsible for ending the Level, increasing or decreasing
     * the difficulty, and moving onto the next Level.
     *
     * @param win Whether or not the previous Level was won.
     */
    public void endLevel(boolean win) {
        //Show the end Level screen
        content.doEndLevel(win);
        //Update the difficulty, and if the game has not been won
        if (!changeDifficulty(win)) {
            //Create a new Level
            createNewLevel();
        } else {
            //Otherwise display the win screen
            content.winGame();
        }

    }

    /**
     * This method is responsible for increasing and decreasing the difficulty
     * of the Level based on whether the previous Level was won or lost. This
     * method is also responsible for determining if the game has been won or
     * not.
     *
     * @param won Whether or not the previous Level was won.
     * @return Whether or not the game has been won.
     */
    private boolean changeDifficulty(boolean won) {
        if (won) {
            difficulty += DIFFICULTY_INCREMENT;
        } else {
            difficulty -= DIFFICULTY_INCREMENT;
        }

        //Restrict difficulty
        if (difficulty > Level.MAX_DIFFICULTY) {
            //The game is won
            return true;
        } else if (difficulty < 1.0) {
            difficulty = 1.0;
        }

        //The game has not yet been won
        return false;
    }

    /**
     * This class represents what happens when the play/pause button is pressed.
     */
    class PlayPauseListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            content.toggleStartStop();
        }
    }

    /**
     * This class represents what happens when the generate button is pressed.
     * Note that this is only used for debugging. To enable debugging, set
     * DEBUGGING_MODE to true.
     */
    class GenerateListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            createNewLevel();
        }
    }

    /**
     * This class represents what happens when the light switch button is
     * pressed. Note that this is only used for debugging. To enable debugging,
     * set DEBUGGING_MODE to true.
     */
    class LightSwitchListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Level.LIGHT_ENABLED = !Level.LIGHT_ENABLED;
        }
    }

    /**
     * This class represents what happens when the instructions button is
     * pressed. It should pause the game (if its not already paused), show the
     * instructions dialog, and when that closes, should return the game to its
     * previous running state.
     */
    class InstructionsListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            //Record the old state of the game (paused or running
            boolean oldContentRunState = content.getRunState();
            content.setRunState(false);

            //Show the instructions pop up
            HUDPanel.showInstructions();

            //Return to the state it was in
            content.setRunState(oldContentRunState);
        }
    }

    @Override
    /**
     * This method is responsible for initializing the Applet. This includes
     * loading all images for the game, and setting up the different Components
     * of the Applet.
     */
    public void init() {
        try {
            //Load all files for each separate class
            String graphicsFolder = System.getProperty("user.dir")
                    + File.separatorChar + "src" + File.separatorChar
                    + "graphics" + File.separatorChar;
            //System.out.println(graphicsFolder);
            Tile.loadImages(ImageIO.read(new File(graphicsFolder + "wall.png")), ImageIO.read(new File(graphicsFolder + "empty.png")));
            Player.playerImage = ImageIO.read(new File(graphicsFolder
                    + "player.png"));
            Projectile.projectileImage = ImageIO.read(new File(graphicsFolder
                    + "fireball.png"));
            RandomEnemy.randomEnemyImage = ImageIO.read(new File(graphicsFolder
                    + "gengar.png"));
            SimpleEnemy.simpleEnemyImage = ImageIO.read(new File(graphicsFolder + "bat.png"));
            SearchEnemy.searchEnemyImage = ImageIO.read(new File(graphicsFolder + "dog.png"));
            HUDPanel.ratingImage = ImageIO.read(new File(graphicsFolder + "rating.png"));
            HealthPowerUp.healthPowerUpImage = ImageIO.read(new File(graphicsFolder + "health.png"));
            SpeedPowerUp.speedPowerUpImage = ImageIO.read(new File(graphicsFolder + "speed.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Set the size of the applet
        setSize(APPLET_WIDTH, APPLET_HEIGHT);

        //Initialize the difficulty to 1.0, create empty enemies and projectile ArrayLists
        //Create the Player object
        difficulty = 1.0;
        enemies = new ArrayList<Enemy>();
        projectiles = new ArrayList<Projectile>();
        player = new Player(null, null, projectiles);

        //Construct the GUI components of the applet
        makeGUI();
        createNewLevel();
    }

    /**
     * This function is responsible for putting the GUI components on the applet
     */
    private void makeGUI() {
        JPanel contentPane = new JPanel(new BorderLayout(), true);
        setContentPane(contentPane);

        //Create these panels with references to the enemy, projectile, and player objects
        HUD = new HUDPanel(player, enemies);
        content = new ContentPanel(this, HUD, currentLevel, player, enemies, projectiles);

        //Create a button to start the level
        Button playPauseButton = new Button("Play/Pause");
        playPauseButton.addActionListener(new PlayPauseListener());

        //Create a button to generate a new map
        Button generateButton = new Button("Generate Level");
        generateButton.addActionListener(new GenerateListener());

        //Create a button to toggle the light
        Button lightSwitch = new Button("Toggle Light");
        lightSwitch.addActionListener(new LightSwitchListener());

        //The generate button and light switch buttons are for debugging only.
        //Uncomment these lines to use their functionality.
        if (!DEBUGGING_MODE) {
            generateButton.setVisible(false);
            lightSwitch.setVisible(false);
        }

        Button instructionsButton = new Button("Instructions");
        instructionsButton.addActionListener(new InstructionsListener());

        //Setup the HUD component
        //Uncomment the generateButton and lightSwitch button 
        HUD.setPreferredSize(new Dimension(this.getWidth(), HUD_HEIGHT));
        HUD.add(playPauseButton);
        HUD.add(generateButton);
        HUD.add(lightSwitch);
        HUD.add(instructionsButton);

        //Setup the ContentPanel component
        content.setPreferredSize(new Dimension(this.getWidth(), this.getHeight() - HUD_HEIGHT));

        //Add the HUD and ContentPanel to the applet
        contentPane.add(HUD, BorderLayout.NORTH);
        contentPane.add(content, BorderLayout.CENTER);
    }
}
