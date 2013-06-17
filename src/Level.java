
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Level {

    public static final int MIN_WIDTH = 20, MAX_WIDTH = 30, HEIGHT = 20;
    public static final Color LIGHT_COLOR = new Color(255, 255, 200, 150);
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
    private MainApplet mainApplet;
    private Tile[][] map;
    private int width, height;
    private double difficulty;
    private ArrayList<PowerUp> powerUps;
    private Area permLitArea;
    private Area currentLightArea;
    private Area tempLitArea;
    private Task currentTask;
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
    private ArrayList<PathTile> openList;
    private ArrayList<PathTile> closedList;

    /**
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

        if (difficulty < 1.0) {
            difficulty = 1.0;
        } else if (difficulty > MAX_DIFFICULTY) {
            difficulty = MAX_DIFFICULTY;
        }
        this.difficulty = difficulty;

        powerUps = new ArrayList<PowerUp>();
        permLitArea = new Area();

        generateMap();

        mapImage = new BufferedImage(getWidthPixels(), getHeightPixels(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = mapImage.createGraphics();

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidthPixels(), getHeightPixels());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (map[x][y] != null) {
                    map[x][y].draw(g);
                } else {
                    g.drawRect(x * Tile.WIDTH, y * Tile.HEIGHT,
                            Tile.WIDTH, Tile.HEIGHT);
                }
            }
        }
    }

    class Task extends TimerTask {

        /**
         * Level needs to have its own thread so that it can handle lighting the
         * screen as the player explores the level. It requires a thread so that
         * it does not interrupt the other threads of the program.
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
     * This method is used to
     *
     * @param timer
     */
    public void startThread(Timer timer) {
        currentTask = new Task();
        // Make the frequency of the light thread half that of the other threads
        // to keep it from being laggy
        timer.scheduleAtFixedRate(currentTask, 0, LEVEL_THREAD_DELAY_FACTOR * 1000 / MainApplet.FPS);
    }

    /**
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
        int numRandom = difficulty < HARD_DIFFICULTY ? 1 : 2;

        int totalEnemies = numSimple + numSearch + numRandom;

        for (int i = 0; i < totalEnemies && i < MAX_ENEMIES; i++) {
            Tile t = getRandomTile(player.getPosition(), MIN_STARTING_DISTANCE_FROM_PLAYER);

            Enemy newEnemy = null;

            if (numSimple > 0) {
                newEnemy = new SimpleEnemy(t, this, player);
                numSimple--;
            } else if (numSearch > 0) {
                newEnemy = new SearchEnemy(t, this, player);
                numSearch--;
            } else if (numRandom > 0) {
                newEnemy = new RandomEnemy(t, this, player);
                numRandom--;
            }

            if (newEnemy != null) {
                enemies.add(newEnemy);
            }
        }
    }

    /**
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

        if (multiplePowerUps) {
            numHealth += (int) (Math.random() * 2);
            numSpeed += (int) (Math.random() * 2);
        }

        int totalPowerUps = numHealth + numSpeed;

        for (int i = 0; i < totalPowerUps && i < MAX_POWER_UPS; i++) {
            Tile t = getRandomTile(playerPosition, MIN_STARTING_DISTANCE_FROM_PLAYER);

            PowerUp newPowerUp = null;

            if (numHealth > 0) {
                newPowerUp = new HealthPowerUp(t, 50);
                numHealth--;
            } else if (numSpeed > 0) {
                newPowerUp = new SpeedPowerUp(t);
                numSpeed--;
            }

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

    private boolean makeTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height || map[x][y] != null) {
            return false;
        }

        // Make the current tile empty
        map[x][y] = new Tile(x, y, false);

        int left = x - 1;
        int right = x + 1;
        int up = y - 1;
        int down = y + 1;

        boolean forceOneEmpty = false;

        while (!forceOneEmpty) {
            boolean[] emptyAdjacent = getEmptyAdjacent(x, y);

            // Dont enforce at least one being chosen if there is already an
            // adjacent empty
            // tile
            boolean tileEmptyLeft = left > 0 && map[left][y] != null
                    && !map[left][y].getIsWall();
            boolean tileEmptyRight = right < width && map[right][y] != null
                    && !map[right][y].getIsWall();
            boolean tileEmptyUp = up > 0 && map[x][up] != null
                    && !map[x][up].getIsWall();
            boolean tileEmptyDown = down < height && map[x][down] != null
                    && !map[x][down].getIsWall();

            forceOneEmpty = tileEmptyLeft || tileEmptyRight || tileEmptyUp
                    || tileEmptyDown;

            if (!emptyAdjacent[0] && !emptyAdjacent[1] && !emptyAdjacent[2]
                    && !emptyAdjacent[3]) {
                // Base case
                // No more to create
                break;
            }

            for (int i = 0; i < emptyAdjacent.length; i++) {
                if (emptyAdjacent[i]) {
                    boolean isEmpty = Math.random() * 10 < 10 - difficulty;
                    forceOneEmpty = isEmpty ? true : forceOneEmpty;
                    if (isEmpty) {
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

        boolean[] emptyAdjacent = getEmptyAdjacent(x, y);

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

        return true;
    }

    private boolean[] getEmptyAdjacent(int x, int y) {
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
     * @return
     */
    private boolean mapSatisfactory() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile[] adjacent = getAdjacent(x, y);
                boolean allNull = true;
                for (int i = 0; i < adjacent.length; i++) {
                    if (adjacent[i] != null) {
                        allNull = false;
                    }
                }
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

        private PathTile parent;
        private Tile current;
        private int f;
        private int g;

        public PathTile(PathTile parent, Tile current, int h) {
            this.parent = parent;
            this.current = current;
            // Add 1 because making a new PathTile represents a move
            // horizontally or vertically
            if (parent != null) {
                g = this.parent.getG() + 1;
            } else {
                g = 0;
            }
            f = h + g;
        }

        public PathTile getParent() {
            return parent;
        }

        public Tile getCurrent() {
            return current;
        }

        public int getF() {
            return f;
        }

        public int getG() {
            return g;
        }

        public boolean equals(Object o) {
            if (o instanceof PathTile) {
                PathTile pt = (PathTile) o;
                return current.equals(pt.getCurrent());
            }

            return false;
        }
    }

    /**
     * This will be an implementation of A* path finding
     *
     * http://www.policyalmanac.org/games/aStarTutorial.htm
     *
     * @param current
     * @param target
     * @return
     */
    public ArrayList<Tile> getPath(ArrayList<Tile> path, Tile current, Tile target) {
        //Make a new path if it's null, re-use the old one if it's not
        if (path == null) {
            path = new ArrayList<Tile>();
        } else {
            path.clear();
        }
        //If the current Tile is the same as the target Tile, then there is no path
        if (current.equals(target)) {
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
        // Tile's
        // equals(Object o) method

        boolean targetFound = false;
        PathTile curTile = new PathTile(null, current, 0);

        while (!targetFound) {
            PathTile minF = null;
            for (PathTile pathTile : openList) {
                if (minF == null || pathTile.getF() <= minF.getF()) {
                    minF = pathTile;
                }
            }

            if (minF != null) {
                openList.remove(minF);
                closedList.add(minF);
                curTile = minF;

                if (curTile.getCurrent().equals(target)) {
                    targetFound = true;
                }
            }

            Tile[] adjacent = getAdjacent(curTile.getCurrent());

            for (Tile t : adjacent) {
                if (!t.getIsWall() && !closedList.contains(t)) {
                    PathTile child = new PathTile(curTile, t, Math.abs(target
                            .getX() - t.getX())
                            + Math.abs(target.getY() - t.getY()));
                    if (!openList.contains(child)) {
                        openList.add(child);
                    } else {
                        int indenticalIndex = openList.indexOf(child);
                        PathTile identical = openList.get(indenticalIndex);
                        Tile identicalTile = identical.getCurrent();
                        int h = Math.abs(target.getX() - identicalTile.getX())
                                + Math.abs(target.getY() - identicalTile.getY());
                        if (curTile.getG() < identical.getG()) {
                            openList.set(indenticalIndex, new PathTile(curTile,
                                    identicalTile, h));
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

            do {
                path.add(childTile.getCurrent());
                childTile = childTile.getParent();
            } while (childTile != null);
        }

        path.remove(current);

        return path;
    }

    public boolean isAdjacent(Tile current, Tile other) {

        boolean ret = false;

        ret = current.getX() - 1 == other.getX() ? true : ret;
        ret = current.getX() + 1 == other.getX() ? true : ret;
        ret = current.getY() - 1 == other.getY() ? true : ret;
        ret = current.getY() + 1 == other.getY() ? true : ret;

        return ret;
    }

    public Tile[] getAdjacent(Tile t) {
        if (t == null) {
            return null;
        }

        return getAdjacent(t.getX(), t.getY());
    }

    private Tile[] getAdjacent(int x, int y) {
        int numAdjacent = 4;
        if (x <= 0 || x >= width - 1) {
            numAdjacent--;
        }
        if (y <= 0 || y >= height - 1) {
            numAdjacent--;
        }

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

    public Tile getLeft(Tile t) {
        if (t == null) {
            return null;
        }

        return getLeft(t.getX(), t.getY());
    }

    private Tile getLeft(int x, int y) {
        if (x <= 0 || y < 0 || x >= width || y >= height) {
            return null;
        }

        return map[x - 1][y];
    }

    public Tile getRight(Tile t) {
        if (t == null) {
            return null;
        }

        return getRight(t.getX(), t.getY());
    }

    private Tile getRight(int x, int y) {
        if (x < 0 || y < 0 || x >= width - 1 || y >= height) {
            return null;
        }

        return map[x + 1][y];
    }

    public Tile getUp(Tile t) {
        if (t == null) {
            return null;
        }

        return getUp(t.getX(), t.getY());
    }

    private Tile getUp(int x, int y) {
        if (x < 0 || y <= 0 || x >= width || y >= height) {
            return null;
        }

        return map[x][y - 1];
    }

    public Tile getDown(Tile t) {
        if (t == null) {
            return null;
        }

        return getDown(t.getX(), t.getY());
    }

    private Tile getDown(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height - 1) {
            return null;
        }

        return map[x][y + 1];
    }

    /**
     * This method returns a reference to the Tile at position (x, y) in the Level.
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

    public void setTempLitArea(Area tempLitArea) {
        this.tempLitArea = tempLitArea;
    }
}
