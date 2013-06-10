
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class MainApplet extends JApplet {

    public static int FPS = 30;
    public static double DIFFICULTY_INCREMENT = 0.5;
    Timer timer;
    HUDPanel HUD;
    ContentPanel content;
    Level currentLevel;
    Player player;
    ArrayList<Enemy> enemies;
    ArrayList<Projectile> projectiles;
    int score;
    double difficulty;

    public void createNewLevel() {
        // Can't make a new level without a content panel and a player
        if (content == null || player == null) {
            return;
        }
        projectiles.clear();
        enemies.clear();

        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();

        // Difficulty between 1 and 4
        currentLevel = new Level(this, Level.MIN_WIDTH
                + (int) (Math.random() * (Level.MAX_WIDTH - Level.MIN_WIDTH)),
                Level.HEIGHT, difficulty);

        int levelWidth = currentLevel.getWidth() * Tile.TILE_WIDTH;
        int levelHeight = currentLevel.getHeight() * Tile.TILE_HEIGHT;
        content.setSize(levelWidth, levelHeight);
        content.setLocation((getWidth() - levelWidth) / 2, HUD.getHeight());
        content.setCurrentLevel(currentLevel);

        player.reset(currentLevel, currentLevel.getTile(0, 0));

        int numSimple = 1;
        int numSearch = 1;
        int numRandom = difficulty < 3.0 ? 1 : 2;
        currentLevel.addEnemies(numSimple, numSearch, numRandom, enemies, player);

        //Let the user see the level before having to start right away
        content.stop();

        //Start threads
        currentLevel.startThread(timer);
        content.startThread(timer);
        HUD.startThread(timer);
    }

    public void endLevel(boolean win) {
        content.doEndLevel();
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
        } else if (difficulty > 4.0) {
            difficulty = 4.0;
        }
    }

    class LevelStarterListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            content.start();
        }
    }

    class LevelGeneratorListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            createNewLevel();
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
            String graphicsFolder = System.getProperty("user.dir")
                    + File.separatorChar + "src" + File.separatorChar
                    + "graphics" + File.separatorChar;
            System.out.println(graphicsFolder);
            Tile.loadImage(ImageIO.read(new File(graphicsFolder + "wall.png")));
            Player.playerImage = ImageIO.read(new File(graphicsFolder
                    + "player.png"));
            Projectile.projectileImage = ImageIO.read(new File(graphicsFolder
                    + "fireball.png"));
            RandomEnemy.randomEnemyImage = ImageIO.read(new File(graphicsFolder
                    + "gengar.png"));
            SimpleEnemy.simpleEnemyImage = ImageIO.read(new File(graphicsFolder + "bat.png"));
            SearchEnemy.searchEnemyImage = ImageIO.read(new File(graphicsFolder + "dog.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setSize(1000, 768);

        difficulty = 1.0;
        enemies = new ArrayList<Enemy>();
        projectiles = new ArrayList<Projectile>();
        player = new Player(null, null, projectiles, 100);

        makeGUI();
    }

    private void makeGUI() {
        JPanel contentPane = new JPanel(new BorderLayout(), true);
        setContentPane(contentPane);

        HUD = new HUDPanel(player, enemies);
        content = new ContentPanel(this, currentLevel, player, enemies, projectiles);

        Button mapGenerator = new Button("Generate");
        mapGenerator.addActionListener(new LevelGeneratorListener());

        Button levelStarter = new Button("Start");
        levelStarter.addActionListener(new LevelStarterListener());

        Button lightSwitch = new Button("Toggle");
        lightSwitch.addActionListener(new LightSwitchListener());

        HUD.setBorder(new LineBorder(Color.RED));
        HUD.setPreferredSize(new Dimension(this.getWidth(), 100));
        HUD.add(mapGenerator);
        HUD.add(levelStarter);
        HUD.add(lightSwitch);

        content.setBorder(new LineBorder(Color.BLACK));
        content.setSize(new Dimension(this.getWidth(), this.getHeight() - 100));
        content.setAlignmentY(Component.CENTER_ALIGNMENT);

        contentPane.add(HUD, BorderLayout.NORTH);
        contentPane.add(content, BorderLayout.CENTER);
    }
}
