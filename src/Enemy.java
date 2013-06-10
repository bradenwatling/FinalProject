
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class Enemy extends Actor {

    public static final int NUM_FRAMES = 2;
    /**
     * http://www.spriters-resource.com/ds/pokeheartgoldsoulsilver/sheet/26794
     * http://www.spriters-resource.com/fullview/26795/
     * http://www.spriters-resource.com/fullview/26794/
     */
    Player player;
    BufferedImage drawImage;
    Tile lastPlayerPosition;
    ArrayList<Tile> pathToPlayer;
    /**
     * This represents the number of times out of 10 that each type of enemy
     * will choose a random direction rather than following the path.
     */
    int randomChoice;
    int damage;
    int retargetTime;

    public Enemy(Tile position, Level currentLevel, Player player) {
        super(position, currentLevel, NUM_FRAMES, 15);

        this.player = player;

        // Dont go to a default frame when the projectile stops
        xDefaultFrame = -1;
        yDefaultFrame = -1;
    }
    long timeSinceRetarget = 0;

    @Override
    void update() {
        if (currentLevel == null || position == null || player == null) {
            return;
        }

        boolean chooseRandom;
        Tile curPlayerPosition = player.getPosition();

        if (animationComplete) {
            chooseRandom = Math.random() * 10 < randomChoice;

            long now = System.currentTimeMillis();
            if (updatePath()) {
                if (pathToPlayer != null) {
                    pathToPlayer.clear();
                }
                pathToPlayer = currentLevel.getPath(position,
                        player.getPosition());

                timeSinceRetarget = now;
            }
            lastPlayerPosition = curPlayerPosition;

            if (now - timeSinceRetarget > retargetTime) {
                if (chooseRandom) {
                    target = getRandomAdjacent();
                    if (target != null) {
                        pathToPlayer = currentLevel.getPath(target,
                                curPlayerPosition);
                    }
                } else {
                    if (!position.equals(curPlayerPosition)
                            && pathToPlayer != null && pathToPlayer.size() > 0) {

                        target = pathToPlayer.get(0);
                        pathToPlayer.remove(0);
                    }
                }
            }

            updateAnimation();

            if (!moveToTarget()) {
                pathToPlayer = currentLevel.getPath(position,
                        player.getPosition());
            }
        }
    }

    protected boolean updatePath() {
        return lastPlayerPosition == null
                || !lastPlayerPosition.equals(player.getPosition());
    }

    private Tile getRandomAdjacent() {
        int direction = (int) (Math.random() * 4);
        direction = direction >= 4 ? 3 : direction;

        Tile ret;

        switch (direction) {
            case 0:
                ret = currentLevel.getLeft(position);
                break;
            case 1:
                ret = currentLevel.getRight(position);
                break;
            case 2:
                ret = currentLevel.getUp(position);
                break;
            case 3:
            default:
                ret = currentLevel.getDown(position);
                break;
        }

        return ret;
    }

    @Override
    void draw(Graphics2D g) {
        if (drawImage != null) {
            BufferedImage frame = getCurrentFrame(drawImage);
            if (frame != null && doMove(g, frame) && position != null) {
                drawImage(g, frame, position.getXPixels(),
                        position.getYPixels());
            }
        }

        /*
         * if (pathToPlayer != null) { g.setColor(new Color(255, 0, 0, 50)); for
         * (int i = 0; i < pathToPlayer.size(); i++) { Tile t =
         * pathToPlayer.get(i); g.fillRect(t.getXPixels(), t.getYPixels(),
         * Tile.TILE_WIDTH, Tile.TILE_HEIGHT); } }
         */
    }

    @Override
    protected Area getLight() {
        int x = position.getXPixels() + xMove + Tile.TILE_WIDTH / 2;
        int y = position.getYPixels() + yMove + Tile.TILE_HEIGHT / 2;

        /*
         * float smallRadius = lightRadius * 1.5f, bigRadius = lightRadius * 2;
         * if (moveLeft) { return new Area(new Ellipse2D.Float(x - bigRadius, y
         * - smallRadius / 2, bigRadius, smallRadius)); } else if (moveRight) {
         * return new Area(new Ellipse2D.Float(x, y - smallRadius / 2,
         * bigRadius, smallRadius)); } else if (moveUp) { return new Area(new
         * Ellipse2D.Float(x - smallRadius / 2, y - bigRadius, smallRadius,
         * bigRadius)); } else if (moveDown) { return new Area(new
         * Ellipse2D.Float(x - smallRadius / 2, y, smallRadius, bigRadius)); }
         * else {
         */
        return new Area(new Ellipse2D.Float(x - lightRadius / 2, y
                - lightRadius / 2, lightRadius, lightRadius));
        // }
    }

    public int getDamageAmount() {
        return damage;
    }
}
