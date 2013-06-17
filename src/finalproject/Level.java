package finalproject;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class represents the Level that the Player, Enemy, Projectile and
 * PowerUp objects must play through. It is responsible for randomly generating
 * a Level, and determining a path from one Tile to another.
 *
 * June 17, 2013
 *
 * @author Braden Watling
 */
public class Level {

    /**
     * These represent the min and maximum width, and the height of the Level
     */
    public static final int MIN_WIDTH = 20, MAX_WIDTH = 30, HEIGHT = 20;
    /**
     * This represents the Color that is drawn over top of the Area that is
     * being lit up.
     */
    public static final Color LIGHT_COLOR = new Color(255, 255, 200, 150);
    /**
     * This represents whether or not the darkness/light of the Level is drawn.
     * This is useful in case you would like to look at the entire Level during
     * debugging.
     */
    public static boolean LIGHT_ENABLED = true;
    /**
     * This represents the maximum difficulty a map can have. This value should
     * not be increased above 4. If it is, the map generation takes a much
     * longer time to complete.
     */
    public static final int MAX_DIFFICULTY = 4;
    /**
     * This represents the difficulty at which another enemy is added and more
     * PowerUps are added
     */
    public static final double HARD_DIFFICULTY = 3.0;
    /**
     * These represent the maximum number of enemies and power ups that can
     * exist in a Level at any one time.
     */
    public static final int MAX_ENEMIES = 4, MAX_POWER_UPS = 5;
    /**
     * This represents the minimum starting distance that Enemies and PowerUps
     * must be from the Player.
     */
    public static final int MIN_STARTING_DISTANCE_FROM_PLAYER = 10;
    /**
     * This represents how many times slower the Level thread runs compared to
     * the other threads. The Level thread must run slower because otherwise it
     * uses a large amount of memory and causes the entire game to lag.
     */
    public static final int LEVEL_THREAD_DELAY_FACTOR = 2;
    /**
     * A reference to the Applet.
     */
    private MainApplet mainApplet;
    /**
     * The array of Tiles representing the map.
     */
    private Tile[][] map;
    /**
     * The width and height of the Level.
     */
    private int width, height;
    /**
     * The difficulty of the map to be generated.
     */
    private double difficulty;
    /**
     * An ArrayList of PowerUp representing the PowerUps on this Level.
     */
    private ArrayList<PowerUp> powerUps;
    /**
     * This represents the Area of the map that is permanently lit up.
     */
    private Area permLitArea;
    /**
     * This represents the Area of the map that is currently lit, and should be
     * added to the permanently lit Area.
     */
    private Area currentLightArea;
    /**
     * This represents the Area of the map that is currently lit but should not
     * be added to the permanently lit area.
     */
    private Area tempLitArea;
    /**
     * This represents the Task that is currently running.
     */
    private Task currentTask;
    /**
     * This is the image that represents the map. It is useful because it is
     * drawn when the Level is created, and subsequent calls to the paint method
     * do not need to draw every Tile separately.
     */
    private BufferedImage mapImage;
    /**
     * openList and closedList are required as member variables because
     * instantiating them on every iteration of getPath() hinders performance by
     * a substantial amount. By making them member variables, a performance
     * boost is given, but it introduces the restriction that the getPath()
     * method can only ever be called by a single thread at any given time. This
     * is fine though, because all pathfinding is done on the ContentPanel
     * thread during the game, and it's done on the Level thread during Level
     * initialization.
     */
    private ArrayList<PathTile> openList, closedList;

    /**
     * Create a Level based on the parameters below.
     *
     * @param width The width of the Level, in Tiles
     * @param height The height of the Level, in Tiles
     * @param difficulty Difficulty of the level. Can range from
     * 1-MAX_DIFFICULTY
     */
    Level(MainApplet mainApplet, int width, int height, double difficulty) {
        this.mainApplet = mainApplet;
        this.width = width;
        this.height = height;

        //Limit the difficulty of the Level.
        if (difficulty < 1.0) {
            difficulty = 1.0;
        } else if (difficulty > MAX_DIFFICULTY) {
            difficulty = MAX_DIFFICULTY;
        }
        this.difficulty = difficulty;

        //Initialize the PowerUps and the permanent lit area.
        powerUps = new ArrayList<PowerUp>();
        permLitArea = new Area();

        //Generate the map
        generateMap();

        //Draw the map image once, so that the paint method does not need to draw every Tile in each iteration.
        mapImage = new BufferedImage(getWidthPixels(), getHeightPixels(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = mapImage.createGraphics();

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidthPixels(), getHeightPixels());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //Draw the Tile if it exists
                if (map[x][y] != null) {
                    map[x][y].draw(g);
                }
            }
        }
    }

    /**
     * This class represents the update thread for the Level.
     */
    class Task extends TimerTask {

        /**
         * Level needs to have its own thread so that it can handle lighting the
         * screen as the player explores the level. It requires a thread so that
         * it does not interrupt the other threads of the program with
         * potentially costly operations.
         */
        @Override
        public void run() {
            //If we have a currentLightArea that we havent worked with
            if (currentLightArea != null) {
                Rectangle screen = new Rectangle(getWidthPixels(), getHeightPixels());
                if (!permLitArea.contains(screen)) {
                    if (!permLitArea.contains(currentLightArea.getBounds2D())) {
                        permLitArea.add(currentLightArea);

                        //Get rid of currentLightArea so we don't try to add it next iteration
                        currentLightArea = null;
                    }
                } else {
                    // Win
                    mainApplet.endLevel(true);
                    //Save memory by ending this thread now that the screen is lit up
                    cancel();
                }
            }
        }
    }

    /**
     * This method is used to start the Level thread, which is responsible for
     * keeping track of what area of the screen is lit up.
     *
     * @param timer The Timer object to start a new Task on.
     */
    public void startThread(Timer timer) {
        if (currentTask != null) {
            currentTask.cancel();
        }

        currentTask = new Task();
        // Make the frequency of the thread LEVEL_THREAD_DELAY_FACTOR times slower
        //than that of the other threads to keep it from being laggy
        timer.scheduleAtFixedRate(currentTask, 0, LEVEL_THREAD_DELAY_FACTOR * 1000 / MainApplet.FPS);
    }

    /**
     * This method is responsible for determining the number of each type of
     * Enemy, and adding them to the Level. It is also responsible for making
     * sure that the starting position of the Enemy is reachable by the Player.
     *
     * @param enemies The ArrayList<Enemy> that the created Enemies will be
     * added to.
     * @param player The position of the Player as well as a reference to the
     * Player is needed so that a path is guaranteed to the Player when the
     * Enemy is created.
     */
    public void addEnemies(ArrayList<Enemy> enemies, Player player) {
        int numSimple = 1;
        int numSearch = 1;
        //Increase the number of RandomEnemies if the difficulty is high enough
        int numRandom = difficulty < HARD_DIFFICULTY ? 1 : 2;

        int totalEnemies = numSimple + numSearch + numRandom;

        //For each Enemy that we're going to add
        for (int i = 0; i < totalEnemies && i < MAX_ENEMIES; i++) {
            //Get a random position for the Enemy, guaranteeing a path to the Player
            Tile t = getRandomTile(player.getPosition(), MIN_STARTING_DISTANCE_FROM_PLAYER);

            Enemy newEnemy = null;

            //If we still need to add SimpleEnemies
            if (numSimple > 0) {
                newEnemy = new SimpleEnemy(t, this, player);
                numSimple--;
            } else if (numSearch > 0) {
                //If we still need to add SearchEnemies
                newEnemy = new SearchEnemy(t, this, player);
                numSearch--;
            } else if (numRandom > 0) {
                //If we still need to add RandomEnemies
                newEnemy = new RandomEnemy(t, this, player);
                numRandom--;
            }

            //Add the Enemy
            if (newEnemy != null) {
                enemies.add(newEnemy);
            } else {
                break;
            }
        }
    }

    /**
     * This method is responsible for determining the number of each type of
     * PowerUp, and adding them to the Level. It is also responsible for making
     * sure that the starting position of the PowerUp is reachable by the
     * Player.
     *
     * @param playerPosition The position of the Player must be known so that a
     * path is guaranteed to each PowerUp
     */
    public void addPowerUps(Tile playerPosition) {
        //Only make one of each type of PowerUp if difficulty < 3.0, otherwise
        //make a random number of each
        boolean multiplePowerUps = difficulty >= HARD_DIFFICULTY;
        int numHealth = 1;
        int numSpeed = 1;

        //If the difficulty is high enough to have extra PowerUps
        if (multiplePowerUps) {
            numHealth += (int) (Math.random() * 2);
            numSpeed += (int) (Math.random() * 2);
        }

        int totalPowerUps = numHealth + numSpeed;

        //For each PowerUp
        for (int i = 0; i < totalPowerUps && i < MAX_POWER_UPS; i++) {
            //Get a random position for the PowerUp, guaranteeing a path to the Player
            Tile t = getRandomTile(playerPosition, MIN_STARTING_DISTANCE_FROM_PLAYER);

            PowerUp newPowerUp = null;

            //If we still have to make HealthPowerUps
            if (numHealth > 0) {
                newPowerUp = new HealthPowerUp(t, 50);
                numHealth--;
            } else if (numSpeed > 0) {
                //If we still have to make SpeedPowerUps
                newPowerUp = new SpeedPowerUp(t);
                numSpeed--;
            }

            //Add the PowerUp
            if (newPowerUp != null) {
                powerUps.add(newPowerUp);
            } else {
                break;
            }
        }
    }

    /**
     * This function returns a random, non-wall Tile in the Level. If a
     * targetPosition is specified, then the chosen Tile is guaranteed to have a
     * path to the targetPosition.
     *
     * @param targetPosition If this parameter is not null, then the returned
     * Tile is guaranteed to have a path to the this Tile.
     * @param minDistance The minimum distance that the returned Tile must be
     * from the targetPosition. If targetPosition is null, then this parameter
     * has no effect. If set to 0, then the returned Tile has no restriction on
     * distance from the target.
     * @return A random, non-wall Tile in the level.
     */
    public Tile getRandomTile(Tile targetPosition, int minDistance) {
        //The tile to be returned
        Tile t = null;

        //Keep looking for a Tile until we get one that isn't null and isn't a wall
        while (t == null || t.getIsWall()) {
            //Choose a random Tile on the map
            t = getTile((int) (Math.random() * (width - 1)),
                    (int) (Math.random() * (height - 1)));

            //If we have a position and Tile to guarantee a path to
            if (targetPosition != null && t != null) {
                //Attempt the path
                ArrayList<Tile> path = getPath(null, t, targetPosition);

                //If we didn't get a good enough path
                if (path == null || path.size() < minDistance) {
                    //Don't use this Tile
                    t = null;
                }
            }
        }
        return t;
    }

    /**
     * This function is responsible for starting the generation of the map using
     * the recursive method makeTile(int, int). It makes as many maps necessary
     * until it generates a map that meets all of the conditions specified in
     * mapSatisfactory().
     */
    private void generateMap() {
        do {
            //Make a new Tile[][]
            map = new Tile[width][height];
            //Do the recursive process and start in the top left corner
            makeTile(0, 0);

            //Don't stop until we're happy with the map
        } while (!mapSatisfactory());

        //When we have a map that we like, fill any Tiles that were missed with
        //non-wall Tiles
        fillEmpty();
    }

    /**
     * This is a recursive method that is responsible for generating a random
     * map based on the difficulty specified by the member variable difficulty.
     * The starting position of this algorithm must be at (0, 0).
     *
     * @param x The x-coordinate to start the generation at.
     * @param y The y-coordinate to start the generation at.
     */
    private void makeTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height || map[x][y] != null) {
            return;
        }

        // Make the current tile empty
        map[x][y] = new Tile(x, y, false);

        /**
         * Pre-calculate the different coordinates of left, right, up, and down
         * Tiles
         */
        int left = x - 1;
        int right = x + 1;
        int up = y - 1;
        int down = y + 1;

        //Assume that we must force an empty adjacent Tile
        boolean forceOneEmpty = true;

        //While we need one Tile to be empty
        while (forceOneEmpty) {
            boolean[] nullAdjacent = getNullAdjacent(x, y);

            // Dont enforce at least one being chosen if there is already an
            // adjacent empty tile
            //Determine if the Tile in each direction has already been created and is a wall
            boolean tileEmptyLeft = left > 0 && !nullAdjacent[0]
                    && !map[left][y].getIsWall();
            boolean tileEmptyRight = right < width && !nullAdjacent[1]
                    && !map[right][y].getIsWall();
            boolean tileEmptyUp = up > 0 && !nullAdjacent[2]
                    && !map[x][up].getIsWall();
            boolean tileEmptyDown = down < height && !nullAdjacent[3]
                    && !map[x][down].getIsWall();

            //If there is an empty Tile in any direction, then we dont need to force an empty Tile
            forceOneEmpty = !(tileEmptyLeft || tileEmptyRight || tileEmptyUp
                    || tileEmptyDown);

            //If all Tiles around this one are already created
            if (!nullAdjacent[0] && !nullAdjacent[1] && !nullAdjacent[2]
                    && !nullAdjacent[3]) {
                // Base case
                // No more to create
                break;
            }

            //For each null Tile surrounding the current one
            for (int i = 0; i < nullAdjacent.length; i++) {
                if (nullAdjacent[i]) {
                    //The probability of a Tile being empty is based on the difficulty.
                    //The higher the difficulty, the less chance of empty Tile
                    boolean isEmpty = Math.random() * 10 < 10 - difficulty;

                    //If we've created an empty Tile, then we no longer need to force an empty one
                    forceOneEmpty = isEmpty ? false : forceOneEmpty;

                    //Make an empty Tile in the correct direction
                    if (isEmpty) {
                        //0 is left, 1 is right, 2 is up, 3 is down
                        switch (i) {
                            case 0:
                                makeTile(left, y);
                                break;
                            case 1:
                                makeTile(right, y);
                                break;
                            case 2:
                                makeTile(x, up);
                                break;
                            case 3:
                                makeTile(x, down);
                                break;
                        }
                    }
                }
            }
        }

        //For all null Tiles that we decided aren't empty Tiles
        boolean[] emptyAdjacent = getNullAdjacent(x, y);

        //Make them wall Tiles
        if (emptyAdjacent[0]) {
            map[left][y] = new Tile(left, y, true);
        }
        if (emptyAdjacent[1]) {
            map[right][y] = new Tile(right, y, true);
        }
        if (emptyAdjacent[2]) {
            map[x][up] = new Tile(x, up, true);
        }
        if (emptyAdjacent[3]) {
            map[x][down] = new Tile(x, down, true);
        }
    }

    /**
     * This method is responsible for getting all of the adjacent null Tiles to
     * a Tile.
     *
     * @param x The x-coordinate of the Tile to check the adjacent Tiles.
     * @param y The y-coordinate of the Tile to check the adjacent Tiles.
     * @return A boolean[] holding whether each adjacent Tile to the specified
     * Tile is null or not. The order of directions is left, right, up, down.
     */
    private boolean[] getNullAdjacent(int x, int y) {
        int left = x - 1;
        int right = x + 1;
        int up = y - 1;
        int down = y + 1;

        boolean[] ret = new boolean[4];

        ret[0] = left > 0 && map[left][y] == null;
        ret[1] = right < width && map[right][y] == null;
        ret[2] = up > 0 && map[x][up] == null;
        ret[3] = down < height && map[x][down] == null;

        return ret;
    }

    /**
     * This method checks the generated map to ensure that there are no spaces
     * in the map that are surrounded by all null Tiles. By rejecting the maps
     * that don't meet this criteria, and running the generation algorithm over
     * again, the problem is eliminated.
     *
     * @return Whether or not this map is satisfactory, meaning that no Tiles
     * are surrounded only by null Tiles
     */
    private boolean mapSatisfactory() {
        //For each Tile in the map
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile[] adjacent = getAdjacent(x, y);

                //Determine if all adjacent Tiles are null.
                boolean allNull = true;
                for (int i = 0; i < adjacent.length; i++) {
                    if (adjacent[i] != null) {
                        allNull = false;
                    }
                }

                //If all of the adjacent Tile are null
                if (allNull) {
                    // It would be possible to just call makeTile() again to fix
                    // this mistake, but the original call to makeTile() ensures
                    // that the initial coordinates can go anywhere on the map
                    // that is not sealed off
                    // makeTile(x, y);
                    return false;
                }
            }
        }

        //The map was successfully checked and there were no Tiles with 4 adjacent null Tiles
        return true;
    }

    /**
     * This method goes through the generated map and creates new empty Tiles
     * where the generation algorithm left nulls.
     */
    private void fillEmpty() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (map[x][y] == null) {
                    map[x][y] = new Tile(x, y, false);
                }
            }
        }
    }

    /**
     * This function draws the Level to the Graphics2D component
     *
     * @param g The Graphics2D object representing the area to draw to
     */
    public void draw(Graphics2D g) {
        if (mapImage != null) {
            g.drawImage(mapImage, 0, 0, null);
        }
    }

    /**
     * This function covers the level in darkness and subtracts the area of the
     * map that has been permanently uncovered (by the Player), and the part of
     * the map that has been temporarily uncovered (by the Enemy).
     *
     * @param g The Graphics2D object representing the area to draw to
     */
    public void drawLight(Graphics2D g) {
        /**
         * http://stackoverflow.com/questions/10852959/java-how-to-draw-a-
         * transparent-shape-using-a-graphics-object-g
         */
        if (LIGHT_ENABLED) {
            //An Area object the size of the entire Level
            //This represents the darkness
            Area fillArea = new Area(new Rectangle2D.Float(0, 0, getWidthPixels(), getHeightPixels()));

            //Determine the permanent lit area + temp lit area
            Area totalLitArea = new Area();
            if (permLitArea != null) {
                totalLitArea.add(permLitArea);
            }
            if (tempLitArea != null) {
                totalLitArea.add(tempLitArea);
            }

            //Subtract the area that has been lit
            fillArea.subtract(totalLitArea);

            //Fill the darkness black
            g.setColor(Color.BLACK);
            g.fill(fillArea);

            //Fill the light with the LIGHT_COLOR (has alpha component)
            g.setColor(LIGHT_COLOR);
            g.fill(totalLitArea);
        }
    }

    /**
     * The PathTile class is an object used in the pathfinding algorithm.
     */
    class PathTile {

        /**
         * The parent of this PathTile. This is used when the actual path is
         * being created.
         */
        private PathTile parent;
        /**
         * The Tile that this PathTile represents.
         */
        private Tile current;
        /**
         * The f-value represents the sum of how many steps it will take to get
         * to this PathTile and the heuristic value, which represents how far
         * away this Tile is from the target. Essentially, the entire algorithm
         * is choosing Tiles that have the smallest f-values.
         */
        private int f;
        /**
         * This represents the number of moves required to get from the starting
         * Tile to this PathTile.
         */
        private int g;

        /**
         * Create a PathTile based on the following parameters.
         *
         * @param parent The parent PathTile to this one. Can be null or a
         * PathTile.
         * @param current The Tile that this PathTile represents. Cannot be
         * null.
         * @param h The heuristic value for this Tile, which is an estimation of
         * how far away from the target this Tile is.
         */
        public PathTile(PathTile parent, Tile current, int h) {
            this.parent = parent;
            this.current = current;
            // Add 1 because making a new PathTile represents a move
            // horizontally or vertically
            if (parent != null) {
                //Add one to the parent's g-value
                g = this.parent.getG() + 1;
            } else {
                //If there is no parent, then the g value is 0
                g = 0;
            }

            //f is the sum of h and g
            f = h + g;
        }

        /**
         * Gets the parent PathTile of this PathTile.
         *
         * @return The parent PathTile of this PathTile.
         */
        public PathTile getParent() {
            return parent;
        }

        /**
         * Gets the Tile that this PathTile represents.
         *
         * @return The Tile that this PathTile represents.
         */
        public Tile getCurrent() {
            return current;
        }

        /**
         * Get the f-value of the PathTile.
         *
         * @return The f-value of the PathTile.
         */
        public int getF() {
            return f;
        }

        /**
         * Get the g-value of the PathTile.
         *
         * @return The g-value of the PathTile.
         */
        public int getG() {
            return g;
        }

        /**
         * Override the equals(Object o) of the Object class so that an
         * ArrayList of PathTile can call contains() and compare only the Tile
         * within the PathTile.
         *
         * @param o The PathTile to compare this PathTile to.
         * @return Whether or not the Tile within this PathTile is equal to the
         * Tile in the passed PathTile. If a PathTile is not given as the
         * parameter, it returns false.
         */
        public boolean equals(Object o) {
            if (o instanceof PathTile) {
                PathTile pt = (PathTile) o;
                return current.equals(pt.getCurrent());
            }

            return false;
        }
    }

    /**
     * This is an implementation of an A* path finding algorithm.
     *
     * http://www.policyalmanac.org/games/aStarTutorial.htm
     *
     * @param path A reference to the ArrayList representing the path. If null,
     * a new ArrayList is returned.
     * @param start The starting Tile.
     * @param target The ending Tile.
     * @return An ArrayList of Tiles that represents a path from the start to
     * the target. The first Tile in the path is at the end of the ArrayList.
     */
    public ArrayList<Tile> getPath(ArrayList<Tile> path, Tile start, Tile target) {
        //Make a new path if it's null, re-use the old one if it's not
        if (path == null) {
            path = new ArrayList<Tile>();
        } else {
            path.clear();
        }
        //If the current Tile is the same as the target Tile, then there is no path
        if (start.equals(target)) {
            return path;
        }

        //The only way having one openList/closedList is if only one thread ever
        //calls this method at one time, which is true.
        if (openList == null) {
            //If openList is not initialized, initialize it.
            openList = new ArrayList<PathTile>();
        } else {
            //Otherwise, just clear it
            openList.clear();
        }

        if (closedList == null) {
            //If closedList is not initialized, initialize it.
            closedList = new ArrayList<PathTile>();
        } else {
            //Otherwise, just clear it
            closedList.clear();
        }

        // Note that although these lists are of PathTiles, contains(Tile) can
        // be used because the definition for a PathTile == Tile is defined in
        // Tile's equals(Object o) method

        boolean targetFound = false;

        //Create a PathTile to represent the first Tile
        PathTile curTile = new PathTile(null, start, 0);

        //While the target has not been found
        while (!targetFound) {
            //Find the PathTile with the smallest f-value in the openList
            PathTile minF = null;
            for (PathTile pathTile : openList) {
                if (minF == null || pathTile.getF() <= minF.getF()) {
                    minF = pathTile;
                }
            }

            //Add the smallest f-value PathTile to the closedList
            if (minF != null) {
                openList.remove(minF);
                closedList.add(minF);
                curTile = minF;

                //Check if this PathTile is the target. If it is, we can stop looking.
                if (curTile.getCurrent().equals(target)) {
                    targetFound = true;
                }
            }

            //Get the adjacent Tiles
            Tile[] adjacent = getAdjacent(curTile.getCurrent());

            for (Tile t : adjacent) {
                //If it's not a wall and the closedList doesn't have the Tile
                if (!t.getIsWall() && !closedList.contains(t)) {
                    //Create a new PathTile with the currentTile as the parent, and with a simple heuristic calculation.

                    //This heuristic calculation is called the Manhattan method because it estimates based on the sum of
                    //the number of horizontal and vertical Tiles to the target.

                    int h = Math.abs(target.getX() - t.getX())
                            + Math.abs(target.getY() - t.getY());

                    PathTile child = new PathTile(curTile, t, h);

                    //If its not on the openList, add it
                    if (!openList.contains(t)) {
                        openList.add(child);
                    } else {
                        //If it's already in the openList, find it
                        int identicalIndex = openList.indexOf(t);
                        PathTile identical = openList.get(identicalIndex);

                        //Get the Tile from the identical PathTile
                        Tile identicalTile = identical.getCurrent();

                        //Since this Tile is identical to the one previously created, the h-values are equal
                        if (curTile.getG() < identical.getG()) {
                            //If this PathTile is a better way to get to the identical PathTile, replace it
                            openList.set(identicalIndex, child);
                        }
                    }
                }
            }

            // If weve exhausted all of the tiles that can be reached from the
            // starting tile,
            // but still havent found the target
            if (openList.isEmpty() && !closedList.contains(target)) {
                path = null;
                // Then the target is unreachable
                return null;
            }
        }

        // The first Tile in the path is the last element in the ArrayList
        if (closedList.size() > 0) {
            PathTile childTile = closedList.get(closedList.size() - 1);

            //Create the path from the closedList
            do {
                //Start at the target and follow the parent Tiles all the way back to the start
                path.add(childTile.getCurrent());
                childTile = childTile.getParent();

                //The very first PathTile that we added had a null parent, so add until we find it
            } while (childTile != null);
        }

        //Remove the starting Tile, if it's in the path
        path.remove(start);

        return path;
    }

    /**
     * This method is responsible for determining if two Tiles are adjacent to
     * each other.
     *
     * @param current The first Tile to compare.
     * @param other The second Tile to compare.
     * @return Whether or not the two Tiles are adjacent.
     */
    public boolean isAdjacent(Tile current, Tile other) {
        //Check left of current
        if (current.getX() - 1 == other.getX()) {
            return true;
        }
        //Check right of current
        if (current.getX() + 1 == other.getX()) {
            return true;
        }
        //Check up from current
        if (current.getY() - 1 == other.getY()) {
            return true;
        }
        //Check down from current
        if (current.getY() + 1 == other.getY()) {
            return true;
        }

        return false;
    }

    /**
     * This method is responsible for returning a Tile array of all adjacent
     * Tiles to the specified Tile.
     *
     * @param x The x-coordinate of the Tile whose adjacent Tiles are going to
     * be found.
     * @param y The y-coordinate of the Tile whose adjacent Tiles are going to
     * be found.
     * @return An array of Tiles containing the adjacent Tiles to the specified
     * Tile.
     */
    /**
     * This method is responsible for returning a Tile array of all adjacent
     * Tiles to the specified Tile.
     *
     * @param t The Tile whose adjacent Tiles are going to be found.
     * @return An array of Tiles containing the adjacent Tiles to the specified
     * Tile.
     */
    public Tile[] getAdjacent(Tile t) {
        //Tile cannot be null
        if (t == null) {
            return null;
        }

        return getAdjacent(t.getX(), t.getY());
    }

    /**
     * This method is responsible for returning a Tile array of all adjacent
     * Tiles to the Tile specified by the coordinates x and y.
     *
     * @param x The x-coordinate of the Tile whose adjacent Tiles are going to
     * be found.
     * @param y The y-coordinate of the Tile whose adjacent Tiles are going to
     * be found.
     * @return An array of Tiles containing the adjacent Tiles to the specified
     * Tile.
     */
    private Tile[] getAdjacent(int x, int y) {
        //The default number of adjacent Tiles
        int numAdjacent = 4;
        //If the current Tile is at the left or right side, there is one less adjacent Tile
        if (x <= 0 || x >= width - 1) {
            numAdjacent--;
        }
        //If the current Tile is at the top or bottom, there is one less adjacent Tile
        if (y <= 0 || y >= height - 1) {
            numAdjacent--;
        }

        //Create the Tile[] and fill it with all of the adjacent Tiles
        Tile[] ret = new Tile[numAdjacent];

        int i = 0;

        // Left
        if (x > 0) {
            ret[i++] = map[x - 1][y];
        }
        // Right
        if (x < width - 1) {
            ret[i++] = map[x + 1][y];
        }
        // Up
        if (y > 0) {
            ret[i++] = map[x][y - 1];
        }
        // Down
        if (y < height - 1) {
            ret[i++] = map[x][y + 1];
        }

        return ret;
    }

    /**
     * This method returns the Tile that is to the left of the specified Tile.
     *
     * @param t The Tile to use to find the Tile to the left.
     * @return The Tile to the left of the specified Tile.
     */
    public Tile getLeft(Tile t) {
        //Tile must not be null
        if (t == null) {
            return null;
        }

        return getLeft(t.getX(), t.getY());
    }

    /**
     * This method returns the Tile that is to the left of the Tile at the
     * specified coordinates.
     *
     * @param x The x-position of the Tile that should be to the right of the
     * returned Tile.
     * @param y The y-position of the Tile that should be to the right of the
     * returned Tile.
     * @return The Tile to the left of the Tile at the specified coordinates.
     */
    private Tile getLeft(int x, int y) {
        //If its off the screen, or on the screen but at the very left
        if (x <= 0 || y < 0 || x >= width || y >= height) {
            //There is no Tile to the left of this one
            return null;
        }

        //Return the Tile to the left of this one
        return map[x - 1][y];
    }

    /**
     * This method returns the Tile that is to the right of the specified Tile.
     *
     * @param t The Tile to use to find the Tile to the right.
     * @return The Tile to the right of the specified Tile.
     */
    public Tile getRight(Tile t) {
        //Tile must not be null
        if (t == null) {
            return null;
        }

        return getRight(t.getX(), t.getY());
    }

    /**
     * This method returns the Tile that is to the right of the Tile at the
     * specified coordinates.
     *
     * @param x The x-position of the Tile that should be to the left of the
     * returned Tile.
     * @param y The y-position of the Tile that should be to the left of the
     * returned Tile.
     * @return The Tile to the right of the Tile at the specified coordinates.
     */
    private Tile getRight(int x, int y) {
        //If its off the screen, or on the screen but at the very right
        if (x < 0 || y < 0 || x >= width - 1 || y >= height) {
            //There is no Tile to the right of this one
            return null;
        }

        //Return the Tile to the right of this one
        return map[x + 1][y];
    }

    /**
     * This method returns the Tile that is above the specified Tile.
     *
     * @param t The Tile to use to find the Tile above.
     * @return The Tile above the specified Tile.
     */
    public Tile getUp(Tile t) {
        //Tile must not be null
        if (t == null) {
            return null;
        }

        return getUp(t.getX(), t.getY());
    }

    /**
     * This method returns the Tile that is above the Tile at the specified
     * coordinates.
     *
     * @param x The x-position of the Tile that should be below the returned
     * Tile.
     * @param y The y-position of the Tile that should be below the returned
     * Tile.
     * @return The Tile above the Tile at the specified coordinates.
     */
    private Tile getUp(int x, int y) {
        //If its off the screen, or on the screen but at the very top
        if (x < 0 || y <= 0 || x >= width || y >= height) {
            //There is no Tile above this one
            return null;
        }

        //Return the Tile above this one
        return map[x][y - 1];
    }

    /**
     * This method returns the Tile that is below the specified Tile.
     *
     * @param t The Tile to use to find the Tile below.
     * @return The Tile below the specified Tile.
     */
    public Tile getDown(Tile t) {
        //Tile must not be null
        if (t == null) {
            return null;
        }

        return getDown(t.getX(), t.getY());
    }

    /**
     * This method returns the Tile that is below the Tile at the specified
     * coordinates.
     *
     * @param x The x-position of the Tile that should be above the returned
     * Tile.
     * @param y The y-position of the Tile that should be above the returned
     * Tile.
     * @return The Tile below the Tile at the specified coordinates.
     */
    private Tile getDown(int x, int y) {
        //If its off the screen, or on the screen but at the very bottom
        if (x < 0 || y < 0 || x >= width || y >= height - 1) {
            //There is no Tile below this one
            return null;
        }

        //Return the Tile below this one
        return map[x][y + 1];
    }

    /**
     * This method returns a reference to the Tile at position (x, y) in the
     * Level.
     *
     * @param x The x-coordinate of the desired Tile.
     * @param y The y-coordinate of the desired Tile.
     * @return A reference to the desired Tile.
     */
    public Tile getTile(int x, int y) {
        return map[x][y];
    }

    /**
     * This method returns the width of the Level in Tiles.
     *
     * @return The width of the Level in Tiles
     */
    public int getWidth() {
        return width;
    }

    /**
     * This method returns the height of the Level in Tiles.
     *
     * @return The height of the Level in Tiles
     */
    public int getHeight() {
        return height;
    }

    /**
     * This method returns the width of the Level in pixels.
     *
     * @return The width of the Level in pixels
     */
    public int getWidthPixels() {
        return width * Tile.WIDTH;
    }

    /**
     * This method returns the height of the Level in pixels.
     *
     * @return The height of the Level in pixels
     */
    public int getHeightPixels() {
        return height * Tile.HEIGHT;
    }

    /**
     * This method returns the difficulty of the Level.
     *
     * @return The difficulty of the Level
     */
    public double getDifficulty() {
        return difficulty;
    }

    /**
     * This method returns the ArrayList<PowerUp> powerUps of the Level.
     *
     * @return The ArrayList<PowerUp> powerUps
     */
    public ArrayList<PowerUp> getPowerUps() {
        return powerUps;
    }

    /**
     * This method sets the value of currentLightArea, which is used in the
     * Level thread (see Task class above) to add up currentLightArea over time.
     *
     * @param currentLightArea
     */
    public void setCurrentLightArea(Area currentLightArea) {
        this.currentLightArea = currentLightArea;
    }

    /**
     * This method sets the area that is currently lit up, but doesn't add to
     * litArea. This area is therefore only temporarily lit up.
     *
     * @param tempLitArea The Area object representing the area that is
     * temporarily lit
     */
    public void setTempLitArea(Area tempLitArea) {
        this.tempLitArea = tempLitArea;
    }
}
