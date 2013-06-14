
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * The MainApplet class represent the Applet where the game occurs. It contains
 * all of the components associated with the game. It is also responsible for
 * resetting the Level when it must be reset.
 *
 * @author Braden Watling
 */
public class MainApplet extends JApplet {

    public static int FPS = 30, APPLET_WIDTH = 1024, APPLET_HEIGHT = 768;
    public static double DIFFICULTY_INCREMENT = 0.5;
    private Timer timer;
    private HUDPanel HUD;
    private ContentPanel content;
    private Level currentLevel;
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Projectile> projectiles;
    private int score;
    private double difficulty;

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

        //Get the Level size in pixels
        int levelWidth = currentLevel.getWidthPixels();
        int levelHeight = currentLevel.getHeightPixels();
        //Setsize and center content
        content.setSize(levelWidth, levelHeight);
        content.setLocation((getWidth() - levelWidth) / 2, HUD.getHeight());
        
        //Update the panels with the currentLevel
        content.setCurrentLevel(currentLevel);
        HUD.setCurrentLevel(currentLevel);

        /**
         * Reset the player to its default conditions Make sure the Player can
         * get to (0, 0) because that is where the Level generation algorithm
         * starts, and almost all Tiles are connected to that Tile Specify a
         * min. distance of 0 from (0, 0). This means the starting Tile is
         * random.
         */
        player.reset(currentLevel, currentLevel.getRandomTile(currentLevel.getTile(0, 0), 0));

        int numSimple = 1;
        int numSearch = 1;
        int numRandom = difficulty < 3.0 ? 1 : 2;
        currentLevel.addEnemies(numSimple, numSearch, numRandom, enemies, player);

        int numHealth = 2;
        int numSpeed = 2;
        currentLevel.addPowerUps(numHealth, numSpeed, player);

        //Let the user see the level before having to start right away
        content.stop();

        //Start threads
        currentLevel.startThread(timer);
        content.startThread(timer);
        HUD.startThread(timer);
    }

    public void endLevel(boolean win) {
        content.doEndLevel(win);
        changeDifficulty(win);
        createNewLevel();
    }

    private void changeDifficulty(boolean increase) {
        if (increase) {
            difficulty += DIFFICULTY_INCREMENT;
        } else {
            difficulty -= DIFFICULTY_INCREMENT;
        }

        //Restrict difficulty
        if (difficulty < 1.0) {
            difficulty = 1.0;
        } else if (difficulty > Level.MAX_DIFFICULTY) {
            difficulty = Level.MAX_DIFFICULTY;
        }
    }

    class LevelGeneratorListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            createNewLevel();
        }
    }

    class LevelPlayPauseListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            content.toggleStartStop();
        }
    }

    class LightSwitchListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Level.LIGHT_ENABLED = !Level.LIGHT_ENABLED;
        }
    }

    @Override
    public void init() {
        try {
            //Load all files for each separate class
            String graphicsFolder = System.getProperty("user.dir")
                    + File.separatorChar + "src" + File.separatorChar
                    + "graphics" + File.separatorChar;
            System.out.println(graphicsFolder);
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
        player = new Player(null, null, projectiles, 100);

        //Construct the GUI components of the applet
        makeGUI();
    }

    /**
     * This function is responsible for putting the GUI components on the applet
     */
    private void makeGUI() {
        JPanel contentPane = new JPanel(new BorderLayout(), true);
        setContentPane(contentPane);

        //Create these panels with references to the enemy, projectile, and player objects
        HUD = new HUDPanel(player, enemies);
        content = new ContentPanel(this, currentLevel, player, enemies, projectiles);

        //Create a button to generate a level
        Button mapGenerator = new Button("Generate Map");
        mapGenerator.addActionListener(new LevelGeneratorListener());

        //Create a button to start the level
        Button levelToggler = new Button("Start / Pause");
        levelToggler.addActionListener(new LevelPlayPauseListener());

        //Create a button to toggle the light
        Button lightSwitch = new Button("Toggle Light");
        lightSwitch.addActionListener(new LightSwitchListener());

        //Setup the HUD component
        HUD.setPreferredSize(new Dimension(this.getWidth(), 100));
        HUD.add(mapGenerator);
        HUD.add(levelToggler);
        HUD.add(lightSwitch);

        //Setup the ContentPanel component
        content.setBorder(new LineBorder(Color.BLACK));
        content.setSize(new Dimension(this.getWidth(), this.getHeight() - 100));
        content.setAlignmentY(Component.CENTER_ALIGNMENT);

        //Add the HUD and ContentPanel to the applet
        contentPane.add(HUD, BorderLayout.NORTH);
        contentPane.add(content, BorderLayout.CENTER);
    }
}
