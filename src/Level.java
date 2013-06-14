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
	public static final int MAX_DIFFICULTY = 4, MAX_ENEMIES = 4,
			MAX_POWERUPS = 5;
	public static final int ENEMY_STARTING_DISTANCE_FROM_PLAYER = 10;
	MainApplet mainApplet;
	Tile[][] map;
	int width, height;
	double difficulty;
	ArrayList<PowerUp> powerUps;
	Area litArea;
	Area currentLightArea;
	Area tempLitArea;
	private Task currentTask;
	private BufferedImage mapImage;

	class Task extends TimerTask {

		/**
		 * Level needs to have its own thread so that it can handle lighting the
		 * screen as the player explores the level. It requires a thread so that
		 * it does not interrupt the other threads of the program.
		 */
		@Override
		public void run() {
			long startTime = System.currentTimeMillis();
			if (currentLightArea != null) {
				Rectangle screen = new Rectangle(width * Tile.TILE_WIDTH,
						height * Tile.TILE_HEIGHT);
				if (!litArea.contains(screen)) {
					if (!litArea.contains(currentLightArea.getBounds2D())) {
						litArea.add(currentLightArea);
						currentLightArea = null;
					}
				} else {
					cancel();
					// Win
					mainApplet.endLevel(true);
				}
			}
			if (System.currentTimeMillis() - startTime > this
					.scheduledExecutionTime()) {
				System.out.println("Level thread flooded");
			}
		}
	}

	/**
	 * 
	 * @param width
	 * @param height
	 * @param difficulty
	 *            Difficulty of the level. Can range from 1-MAX_DIFFICULTY
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

		litArea = new Area();

		do {
			map = new Tile[width][height];
			makeTile(0, 0);
		} while (!mapSatisfactory());

		fillEmpty();

		mapImage = new BufferedImage(this.width * Tile.TILE_WIDTH, this.height
				* Tile.TILE_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = mapImage.createGraphics();

		g.setColor(Color.white);
		g.fillRect(0, 0, width * Tile.TILE_WIDTH, height * Tile.TILE_HEIGHT);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (map[x][y] != null) {
					map[x][y].draw(g);
				} else {
					g.drawRect(x * Tile.TILE_WIDTH, y * Tile.TILE_HEIGHT,
							Tile.TILE_WIDTH, Tile.TILE_HEIGHT);
				}
			}
		}
	}

	public void addEnemies(int numSimple, int numSearch, int numRandom,
			ArrayList<Enemy> enemies, Player player) {
		int totalEnemies = numSimple + numSearch + numRandom;

		for (int i = 0; i < totalEnemies && i < MAX_ENEMIES; i++) {
			Tile t = getRandomTile(player.getPosition());

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
	 * @param numHealth
	 * @param player
	 *            The position of the Player must be known so that a path exists
	 *            to the PowerUp
	 */
	public void addPowerUps(int numHealth, int numSpeed, Player player) {
		int totalPowerUps = numHealth + numSpeed;

		for (int i = 0; i < totalPowerUps && i < MAX_POWERUPS; i++) {
			Tile t = getRandomTile(player.getPosition());

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

	public Tile getRandomTile(Tile playerPosition) {
		Tile t = null;
		while (t == null || t.getIsWall()) {
			t = getTile((int) (Math.random() * (width - 1)),
					(int) (Math.random() * (height - 1)));
			ArrayList<Tile> path = getPath(null, t, playerPosition);

			if (path == null
					|| path.size() < ENEMY_STARTING_DISTANCE_FROM_PLAYER) {
				t = null;
			}
		}
		return t;
	}

	public void startThread(Timer timer) {
		currentTask = new Task();
		// Make the frequency of the light thread half that of the other threads
		// to keep it from being laggy
		timer.scheduleAtFixedRate(currentTask, 0, 4 * 1000 / MainApplet.FPS);
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

	private void fillEmpty() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (map[x][y] == null) {
					map[x][y] = new Tile(x, y, false);
				}
			}
		}
	}

	public void draw(Graphics2D g) {
		if (mapImage != null) {
			g.drawImage(mapImage, 0, 0, null);
		}

		/*
		 * for (int x = 0; x < width; x++) { for (int y = 0; y < height; y++) {
		 * if (map[x][y] != null) { map[x][y].draw(g); } else { g.drawRect(x *
		 * Tile.TILE_WIDTH, y * Tile.TILE_HEIGHT, Tile.TILE_WIDTH,
		 * Tile.TILE_HEIGHT); } } }
		 */
	}

	public void drawLight(Graphics2D g) {
		/**
		 * http://stackoverflow.com/questions/10852959/java-how-to-draw-a-
		 * transparent-shape-using-a-graphics-object-g
		 */
		if (LIGHT_ENABLED) {

			Area fillArea = new Area(new Rectangle2D.Float(0, 0, width
					* Tile.TILE_WIDTH, height * Tile.TILE_HEIGHT));
			fillArea.subtract(litArea);
			if (tempLitArea != null) {
				fillArea.subtract(tempLitArea);
			}

			g.setColor(Color.black);
			g.fill(fillArea);
		}
	}

	public void setCurrentLightArea(Area currentLightArea) {
		this.currentLightArea = currentLightArea;
	}

	public void setTempLitArea(Area tempLitArea) {
		this.tempLitArea = tempLitArea;
	}

	public Area getTempLitArea() {
		return tempLitArea;
	}

	public Area getLitArea() {
		return litArea;
	}

	public Tile getTile(int x, int y) {
		return map[x][y];
	}

	class PathTile {

		PathTile parent;
		Tile current;
		int f;
		int g;

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

		public int getF() {
			return f;
		}

		public int getG() {
			return g;
		}

		public boolean equals(Object o) {
			if (o instanceof PathTile) {
				PathTile pt = (PathTile) o;
				return current.equals(pt.current);
			}
			if (o instanceof Tile) {
				Tile t = (Tile) o;
				return current.equals(t);
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
	public ArrayList<Tile> getPath(ArrayList<Tile> path, Tile current,
			Tile target) {
		if (path == null) {
			path = new ArrayList<Tile>();
		} else {
			path.clear();
		}
		if (current.equals(target)) {
			return path;
		}

		ArrayList<PathTile> openList = new ArrayList<PathTile>();
		ArrayList<PathTile> closedList = new ArrayList<PathTile>();

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

				if (curTile.current.equals(target)) {
					targetFound = true;
				}
			}

			Tile[] adjacent = getAdjacent(curTile.current);

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
						Tile identicalTile = identical.current;
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
				path.add(childTile.current);
				childTile = childTile.parent;
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

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public double getDifficulty() {
		return difficulty;
	}

	public ArrayList<PowerUp> getPowerUps() {
		return powerUps;
	}
}
