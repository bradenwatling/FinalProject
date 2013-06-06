
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Enemy extends Actor {

    public static final int NUM_FRAMES = 2;
    public static final int GHOST_DAMAGE = 3, BAT_DAMAGE = 6, DOG_DAMAGE = 10;
    /**
     * This represents the number of times out of 10 that each type of enemy
     * will choose a random direction rather than following the path.
     */
    public static final int GHOST_CHOOSE_RANDOM = 5, BAT_CHOOSE_RANDOM = 0, DOG_CHOOSE_RANDOM = 0;
    public static final double GHOST_SPEED = 1.5, BAT_SPEED = 0.75, DOG_SPEED = 1.75;
    /**
     * http://www.spriters-resource.com/ds/pokeheartgoldsoulsilver/sheet/26794
     * http://www.spriters-resource.com/fullview/26795/
     * http://www.spriters-resource.com/fullview/26794/
     */
    public static BufferedImage ghostImage, batImage, dogImage;
    Player player;
    BufferedImage drawImage;
    int difficulty;
    EnemyType enemyType;
    Tile lastPlayerPosition;
    ArrayList<Tile> pathToPlayer;

    public enum EnemyType {

        GHOST, BAT, DOG,
    }

    public Enemy(Tile position, Level currentLevel, int health,
            Player player, EnemyType enemyType) {
        super(position, currentLevel, health, NUM_FRAMES, 15);

        this.player = player;
        this.enemyType = enemyType;

        switch (enemyType) {
            case GHOST:
                this.speed = GHOST_SPEED;
                drawImage = ghostImage;
                break;
            case BAT:
                this.speed = BAT_SPEED;
                drawImage = batImage;
                break;
            case DOG:
                this.speed = DOG_SPEED;
                drawImage = dogImage;
                break;
        }

        // Dont go to a default frame when the projectile stops
        xDefaultFrame = -1;
        yDefaultFrame = -1;
    }

    @Override
    void update() {
        if (currentLevel == null || position == null || player == null) {
            return;
        }

        boolean chooseRandom = false;

        if (animationComplete) {
            switch (enemyType) {
                case GHOST:
                    chooseRandom = Math.random() * 10 < GHOST_CHOOSE_RANDOM;

                    break;
                case BAT:
                    //The bat is extremely smart
                    chooseRandom = Math.random() * 10 < BAT_CHOOSE_RANDOM;

                    break;
                case DOG:
                    //The dog is extremely smart
                    chooseRandom = Math.random() * 10 < DOG_CHOOSE_RANDOM;
                    break;
            }

            Tile curPlayerPosition = player.getPosition();
            if (enemyType == EnemyType.DOG) {
                if (pathToPlayer == null || pathToPlayer.isEmpty()) {
                    pathToPlayer = currentLevel.getPath(position,
                            player.getPosition());
                }
            } else {

                if (lastPlayerPosition == null || !lastPlayerPosition.equals(curPlayerPosition)) {
                    lastPlayerPosition = curPlayerPosition;
                    if (pathToPlayer != null) {
                        pathToPlayer.clear();
                    }
                    pathToPlayer = currentLevel.getPath(position,
                            player.getPosition());
                }
            }

            if (chooseRandom) {
                target = getRandomAdjacent();
                if (target != null) {
                    pathToPlayer = currentLevel.getPath(target,
                            player.getPosition());
                }
            } else {
                if (!position.equals(curPlayerPosition) && pathToPlayer != null
                        && pathToPlayer.size() > 0) {
                    target = pathToPlayer.get(0);
                    pathToPlayer.remove(0);
                }
            }

            moveToTarget();
        }

        updateAnimation();
    }

    private Tile getRandomAdjacent() {
        int direction = (int) (Math.random() * 4);
        direction = direction >= 4 ? 3 : direction;

        Tile ret = null;

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
        if (drawImage != null && position != null) {
            BufferedImage frame = getCurrentFrame(drawImage);
            if (frame != null && doMove(g, frame)) {
                drawImage(g, frame, position.getXPixels(),
                        position.getYPixels());
            }
        }

        if (pathToPlayer != null) {
            g.setColor(new Color(255, 0, 0, 50));
            for (int i = 0; i < pathToPlayer.size(); i++) {
                Tile t = pathToPlayer.get(i);
                g.fillRect(t.getXPixels(), t.getYPixels(), Tile.TILE_WIDTH,
                        Tile.TILE_HEIGHT);
            }
        }
    }

    @Override
    protected Area getLight() {
        int x = position.getXPixels() + xMove + Tile.TILE_WIDTH / 2;
        int y = position.getYPixels() + yMove + Tile.TILE_HEIGHT / 2;

        float smallRadius = lightRadius * 1.5f, bigRadius = lightRadius * 2;

        /*
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
        switch (enemyType) {
            case GHOST:
                return GHOST_DAMAGE;
            case BAT:
                return BAT_DAMAGE;
            case DOG:
                return DOG_DAMAGE;
        }

        return 0;
    }
}
