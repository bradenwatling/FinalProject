
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Tile {

    public static BufferedImage tileImage;
    public static int TILE_WIDTH, TILE_HEIGHT;
    int x, y;
    boolean isWall;
    //boolean isOccupied;

    public Tile(int x, int y, boolean isWall) {
        this.x = x;
        this.y = y;
        this.isWall = isWall;
    }

    public static void loadImage(BufferedImage tileImage) {
        Tile.tileImage = tileImage;
        Tile.TILE_WIDTH = tileImage.getWidth();
        Tile.TILE_HEIGHT = tileImage.getHeight();
    }

    public void draw(Graphics2D g) {
        if (isWall) {
            g.drawImage(tileImage, x * tileImage.getWidth(), y * tileImage.getHeight(), null);
        }
    }

    public boolean equals(Object o) {
        if (o instanceof Tile) {
            Tile t = (Tile) o;

            return this.x == t.x && this.y == t.y;
        }
        if(o instanceof Level.PathTile) {
            Level.PathTile pt = (Level.PathTile) o;
            
            return this.equals(pt.current);
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
        return x * TILE_WIDTH;
    }

    public int getYPixels() {
        return y * TILE_HEIGHT;
    }
}
