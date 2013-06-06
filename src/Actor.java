
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

public abstract class Actor {

    public static final int MAX_HEALTH = 100;
    
    protected int xMove, yMove;
    protected Tile position;
    protected Tile target;
    protected Level currentLevel;
    protected int health;
    protected double speed;
    private int numFrames;
    private int FPS;
    protected boolean animationComplete;
    protected int lightRadius;
    private long lastFrameTime;
    private int xCurFrame, yCurFrame;
    protected int xDefaultFrame, yDefaultFrame;
    protected boolean moveLeft, moveRight, moveUp, moveDown;

    public Actor(Tile position, Level currentLevel, int health, int numFrames, int FPS) {
        this.position = position;
        this.currentLevel = currentLevel;
        if(health < 0) {
            //You cant have less than 0 health
            health = 0;
        } else if(health > MAX_HEALTH) {
            health = MAX_HEALTH;
        }
        this.health = health;
        this.numFrames = numFrames;
        this.FPS = FPS;
        this.animationComplete = true;
        this.lightRadius = (int) (Math.sqrt(Math.pow(Tile.TILE_WIDTH, 2) + Math.pow(Tile.TILE_HEIGHT, 2)) * 2);

        //Default speed is 1.0, if it should be changed, it can be done in the subclass' constructor
        speed = 1.0;
        
        lastFrameTime = 0;
        xCurFrame = 0;
        yCurFrame = 0;

        xDefaultFrame = 0;
        yDefaultFrame = 0;
    }

    abstract void update();

    abstract void draw(Graphics2D g);

    protected abstract Area getLight();

    protected boolean moveToTarget() {
        // If target exists and its not null and not a wall
        if (target != null && !target.getIsWall() && currentLevel.isAdjacent(position, target)) {
            animationComplete = false;
            xMove = 0;
            yMove = 0;
            return true;
        } else {
            target = null;
            return false;
        }
    }

    protected void doDamage(int damage) {
        health -= damage;
        
        if(health < 0) {
            health = 0;
        }
    }

    private void checkDirection() {
        if (position != null && target != null) {
            moveLeft = position.getX() > target.getX();
            moveRight = position.getX() < target.getX();
            moveUp = position.getY() > target.getY();
            moveDown = position.getY() < target.getY();
        }
    }

    protected void updateAnimation() {
        //Animate
        if (target == null || target.getIsWall()) {
            if (xDefaultFrame != -1) {
                xCurFrame = xDefaultFrame;
            }
            if (yDefaultFrame != -1) {
                yCurFrame = yDefaultFrame;
            }
        } else {
            long now = System.currentTimeMillis();
            if (now - lastFrameTime > 1000 / FPS) {
                if (xCurFrame == numFrames - 1) {
                    xCurFrame = 0;
                } else {
                    xCurFrame++;
                }
                lastFrameTime = now;
            }
            
            checkDirection();

            if (moveLeft) {
                yCurFrame = 1;
            } else if (moveRight) {
                yCurFrame = 2;
            } else if (moveUp) {
                yCurFrame = 3;
            } else {
                yCurFrame = 0;
            }
        }
    }

    protected BufferedImage getCurrentFrame(BufferedImage image) {
        return image.getSubimage(xCurFrame * Tile.TILE_WIDTH, yCurFrame * Tile.TILE_HEIGHT, Tile.TILE_WIDTH, Tile.TILE_HEIGHT);
    }

    protected boolean doMove(Graphics2D g, BufferedImage image) {
        if (position == null || target == null || animationComplete) {
            return true;
        }

        if (target.equals(position)) {
            animationComplete = true;
        } else {

            checkDirection();

            int xDiff = target.getXPixels() - position.getXPixels();
            int yDiff = target.getYPixels() - position.getYPixels();
            
            //Tile.TILE_WIDTH / 8 is the standard speed of the player
            int xSpeed = (int) (speed * Tile.TILE_WIDTH / 8);
            int ySpeed = (int) (speed * Tile.TILE_HEIGHT / 8);

            if (moveLeft) {
                xMove -= Math.abs(xSpeed);
                animationComplete = xMove <= xDiff;
            } else if (moveRight) {
                xMove += Math.abs(xSpeed);
                animationComplete = xMove >= xDiff;
            } else if (moveUp) {
                yMove -= Math.abs(ySpeed);
                animationComplete = yMove <= yDiff;
            } else if (moveDown) {
                yMove += Math.abs(ySpeed);
                animationComplete = yMove >= yDiff;
            }
        }

        drawImage(g, image, position.getXPixels() + xMove, position.getYPixels() + yMove);

        if (animationComplete) {
            position = target;
            xMove = 0;
            yMove = 0;
            target = null;
        }

        return animationComplete;
    }

    /**
     * This method draws the actor's image in the center of each tile
     *
     * @param g
     * @param actorImage
     * @param x
     * @param y
     */
    protected void drawImage(Graphics2D g, BufferedImage actorImage, int x, int y) {
        g.drawImage(actorImage, x + Tile.TILE_WIDTH / 2 - actorImage.getWidth() / 2, y + Tile.TILE_HEIGHT / 2 - actorImage.getHeight() / 2, null);
    }

    public Tile getPosition() {
        return position;
    }
    
    public Tile getTarget() {
        return target;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public void reset(Level currentLevel, Tile position) {
        this.currentLevel = currentLevel;
        this.position = position;
        
        health = MAX_HEALTH;
        //Avoid glitch where animation keeps going when map changes
        animationComplete = true;
        target = null;
        xMove = 0;
        yMove = 0;
    }

    public int getHealth() {
        return health;
    }
    
    public boolean isDead() {
        return health <= 0;
    }
}
