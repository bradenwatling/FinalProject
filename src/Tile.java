
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Tile {

    public static BufferedImage tileImage;
    public static BufferedImage emptyImage;
    public static int WIDTH, HEIGHT;
    int x, y;
    boolean isWall;
    //boolean isOccupied;

    public Tile(int x, int y, boolean isWall) {
        this.x = x;
        this.y = y;
        this.isWall = isWall;
    }

    public static void loadImages(BufferedImage tileImage, BufferedImage emptyImage) throws Exception {
        Tile.tileImage = tileImage;
        Tile.emptyImage = emptyImage;

        int tileWidth = tileImage.getWidth(), tileHeight = tileImage.getHeight();

        if (tileWidth == emptyImage.getWidth() && tileHeight == emptyImage.getHeight()) {
            Tile.WIDTH = tileWidth;
            Tile.HEIGHT = tileHeight;
        } else {
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
            g.drawImage(tileImage, x * WIDTH, y * HEIGHT, null);
        } else {
            g.drawImage(emptyImage, x * WIDTH, y * HEIGHT, null);
        }
    }

    public boolean equals(Object o) {
        if (o instanceof Tile) {
            Tile t = (Tile) o;

            return this.x == t.x && this.y == t.y;
        }
        if (o instanceof Level.PathTile) {
            Level.PathTile pt = (Level.PathTile) o;

            return this.equals(pt.getCurrent());
        }

        return false;
    }

    public boolean getIsWall() {
        return isWall;
    }

    public void setIsWall(boolean isWall) {
        this.isWall = isWall;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getXPixels() {
        return x * WIDTH;
    }

    public int getYPixels() {
        return y * HEIGHT;
    }
}
