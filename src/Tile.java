
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * This class represents a position in a Level that is either walkable, or is a
 * wall.
 *
 * June 17, 2013
 *
 * @author Braden Watling
 */
public class Tile {

    /**
     * The image for a wall.
     */
    public static BufferedImage wallImage;
    /**
     * The image for an empty Tile.
     */
    public static BufferedImage emptyImage;
    /**
     * The dimensions of a Tile.
     */
    public static int WIDTH, HEIGHT;
    /**
     * The position of the Tile.
     */
    int x, y;
    /**
     * Whether or not the Tile is a wall.
     */
    boolean isWall;

    /**
     * Create a Tile based on the following parameters.
     *
     * @param x The x-position of the Tile.
     * @param y The y-position of the Tile.
     * @param isWall Whether or not the Tile is a wall.
     */
    public Tile(int x, int y, boolean isWall) {
        this.x = x;
        this.y = y;
        this.isWall = isWall;
    }

    /**
     * This method is responsible for loading all images associated with a Tile,
     * and making sure that they are all the same size.
     *
     * @param wallImage The image for a wall Tile
     * @param emptyImage The image for an empty Tile
     * @throws Exception If the dimensions of the two images are not identical
     */
    public static void loadImages(BufferedImage wallImage, BufferedImage emptyImage) throws Exception {
        Tile.wallImage = wallImage;
        Tile.emptyImage = emptyImage;

        int tileWidth = wallImage.getWidth(), tileHeight = wallImage.getHeight();

        if (tileWidth == emptyImage.getWidth() && tileHeight == emptyImage.getHeight()) {
            //Store the width and height as static variables for future use.
            Tile.WIDTH = tileWidth;
            Tile.HEIGHT = tileHeight;
        } else {
            //Throw an exception if the width and height of each are not identical
            throw new Exception("tileImage size does not match emptyImage size");
        }
    }

    /**
     * This function is responsible for drawing a single Tile to the Graphics2D
     * object.
     *
     * @param g The Graphics2D object representing the area to draw to
     */
    public void draw(Graphics2D g) {
        if (isWall) {
            g.drawImage(wallImage, x * WIDTH, y * HEIGHT, null);
        } else {
            g.drawImage(emptyImage, x * WIDTH, y * HEIGHT, null);
        }
    }

    /**
     * This method overrides the definition of equals(Object o) defined in
     * Object. The purpose of this method is to allow tileOne.equals(tileTwo) to
     * return true if the x- and y-coordinates are equal. The same functionality
     * is added for Level.PathTiles, so that an ArrayList<Level.PathTile> can
     * call contains(Tile t).
     *
     * @param o The object to compare to. Can be either a Tile or a
     * Level.PathTile
     * @return Whether or not the x- and y- coordinates of this Tile are equal
     * to those of the given Tile or Level.PathTile.
     */
    public boolean equals(Object o) {
        if (o instanceof Tile) {
            Tile t = (Tile) o;

            //Return whether the coordinates are equal.
            return this.x == t.x && this.y == t.y;
        }
        if (o instanceof Level.PathTile) {
            Level.PathTile pt = (Level.PathTile) o;

            //Return if this Tile equals the Level.PathTile's current Tile
            return this.equals(pt.getCurrent());
        }

        //If it's not a Tile or Level.PathTile, then it's not equal.
        return false;
    }

    /**
     * Gets whether or not this Tile is a wall.
     *
     * @return Whether or not this Tile is a wall.
     */
    public boolean getIsWall() {
        return isWall;
    }

    /**
     * Gets the x-position of the Tile. This is not in pixels, but numbered.
     *
     * @return The x-position of the Tile.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-position of the Tile. This is not in pixels, but numbered.
     *
     * @return The y-position of the Tile.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the x-position of the Tile, in pixels.
     *
     * @return The x-position of the Tile, in pixels.
     */
    public int getXPixels() {
        return x * WIDTH;
    }

    /**
     * Gets the y-position of the Tile, in pixels.
     *
     * @return The y-position of the Tile, in pixels.
     */
    public int getYPixels() {
        return y * HEIGHT;
    }
}
