
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

/**
 * This class represents anything that moves throughout the Level. It handles
 * the animation of the object's sprite sheet, and creates smooth motion between
 * Tiles in the Level.
 *
 * @author Braden Watling
 */
public abstract class Actor {

    /**
     * The maximum amount of health that an Actor can have.
     */
    public static final int MAX_HEALTH = 100;
    /**
     * The amount that the Actor's speed is multiplied by during a speed boost
     */
    public static final int SPEED_BONUS_FACTOR = 2;
    /**
     * The amount that the Actor has moved, in pixels, since starting a smooth
     * transition between Tiles.
     */
    protected int xMove, yMove;
    /**
     * The Actor's current position Tile.
     */
    protected Tile position;
    /**
     * The Tile adjacent to the Actor that the Actor would like to move to.
     */
    protected Tile target;
    /**
     * The current Level that the Actor is situated in.
     */
    protected Level currentLevel;
    /**
     * The amount of health that the Actor has remaining.
     */
    protected int health;
    /**
     * The speed of the Actor in relation to the default speed (the width or
     * height of a Tile divided by 8).
     */
    protected double speed;
    /**
     * This Image is defined by each Actor subclass, and stores each frame of
     * the Actor's movement animation is the following order from top to bottom:
     * down, left, right, up. The number of frames in each row should be
     * identical to the number specified by numFrames.
     */
    protected BufferedImage spriteSheetImage;
    /**
     * The number of frames in the Actor's sprite sheet.
     */
    protected int numFrames;
    /**
     * The coordinated on the sprite sheet of the default frame to be displayed
     * when the Actor is not moving. If set to -1, there will be no default
     * frame and for that direction, and the frame that was last used will
     * continue to be displayed.
     */
    protected int xDefaultFrame, yDefaultFrame;
    /**
     * This represents the number of frames per second that this Actor should be
     * displayed at.
     */
    private int FPS;
    /**
     * Whether or not the Actor has completed its transition from its position
     * Tile to its Target tile.
     */
    protected boolean transitionComplete;
    /**
     * The radius of the circle of light surrounding the Actor. The default
     * light radius for an Actor is twice the length of the diagonal of a single
     * Tile.
     */
    protected int lightRadius;
    /**
     * These are used to determine the direction that the Actor is currently
     * moving in.
     */
    protected boolean moveLeft, moveRight, moveUp, moveDown;
    /**
     * This represents the time, in milliseconds, that the last speed bonus was
     * initiated.
     */
    private long speedBonusStartTime;
    /**
     * This represents how long the current speed bonus will last.
     */
    private long speedBonusDuration;
    /**
     * This represents the time, in milliseconds, of the last frame increment.
     */
    private long lastFrameTime;
    /**
     * These represent the coordinates on the sprite sheet of the current frame.
     * Note that this is numbered, not measured in pixels.
     */
    private int xCurFrame, yCurFrame;

    /**
     * Create a new Actor based on these parameters.
     * 
     * @param position The starting Tile of the Actor
     * @param currentLevel The Level that the Actor is currently in
     * @param spriteSheet The BufferedImage that represents this Actor's sprite
     * sheet.
     * @param numFrames The number of frames in the Actor's sprite sheet
     * @param FPS The number of frames per second for the Actor's animation
     */
    public Actor(Tile position, Level currentLevel, BufferedImage spriteSheet, int numFrames, int FPS) {
        this.position = position;
        this.currentLevel = currentLevel;
        this.spriteSheetImage = spriteSheet;
        this.numFrames = numFrames;
        this.FPS = FPS;
        this.health = MAX_HEALTH;
        this.transitionComplete = true;

        //Set the light radius of the Actor to be twice the length of the diagonal
        //of a single Tile
        this.lightRadius = (int) (Math.sqrt(Math.pow(Tile.WIDTH, 2)
                + Math.pow(Tile.HEIGHT, 2)) * 2);

        // Default speed is 1.0, if it should be changed, it can be done in the
        // subclass' constructor
        speed = 1.0;

        lastFrameTime = 0;
        xCurFrame = 0;
        yCurFrame = 0;

        xDefaultFrame = 0;
        yDefaultFrame = 0;
    }

    /**
     * This method must be overridden by the subclass. It should be called in
     * the update thread of the game for every instance of the subclass.
     */
    abstract void update();

    /**
     * This method is responsible for drawing the Actor in its current position
     * on the Level. It should be called in the paint thread of the game for
     * every instance of the subclass.
     */
    public void draw(Graphics2D g) {
        if (spriteSheetImage != null) {
            //Update the movement of the Actor
            doMove();

            //Draw the image of the Actor in its transition from one tile to the next
            drawImage(g);
        }
    }

    /**
     * This method draws the actor's image in the center of each tile
     *
     * @param g The Graphics2D object to draw the image to
     * @param actorImage The current frame of the Actor's image
     * @param x The x position, in pixels, of the top left hand corner of where
     * to draw the image.
     * @param y The y position, in pixels, of the top left hand corner of where
     * to draw the image.
     */
    protected void drawImage(Graphics2D g) {
        if (position != null) {
            BufferedImage frame = getCurrentFrame(spriteSheetImage);
            int x = position.getXPixels() + xMove;
            int y = position.getYPixels() + yMove;
            g.drawImage(frame, x + Tile.WIDTH / 2 - frame.getWidth()
                    / 2, y + Tile.HEIGHT / 2 - frame.getHeight() / 2,
                    null);
        }
    }

    /**
     * This method must be overridden by the subclass. This method should return
     * an Area object representing the Area around the Actor that is not
     * darkness in the Level.
     */
    protected abstract Area getLight();

    /**
     * This method handles the fluid movement of the Actor between Tiles in the
     * Level. In order for the Actor to start moving to the target Tile, the
     * target must not be null, must not be a wall, and must be adjacent to the
     * Actor's current position.
     *
     * @return Whether or not the movement process has successfully started.
     */
    protected boolean moveToTarget() {
        // If target exists and its not null and not a wall
        // current position exists and the target is adjacent to it
        if (target != null && !target.getIsWall() && position != null
                && currentLevel.isAdjacent(position, target)) {
            transitionComplete = false;
            xMove = 0;
            yMove = 0;
            return true;
        } else {
            target = null;
            return false;
        }
    }

    /**
     * This method allows for the Actor to have damage done to it.
     *
     * @param damage The amount of damage to do to the Actor.
     */
    public void doDamage(int damage) {
        health -= damage;

        if (health < 0) {
            health = 0;
        }
    }

    /**
     * This method allows for the Actor to gain health. This is meant for use by
     * a healing power up. This method cannot give the Actor more than
     * MAX_HEALTH health.
     *
     * @param addHealth The amount of health to add to the Actor.
     */
    public void addHealth(int addHealth) {
        this.health += addHealth;

        if (this.health > MAX_HEALTH) {
            this.health = MAX_HEALTH;
        }
    }

    /**
     * Returns whether or not the Actor has no more health.
     *
     * @return Whether or not the Actor has no more health.
     */
    public boolean isDead() {
        return health <= 0;
    }

    /**
     * Starts a speed boost by a factor of SPEED_BONUS_FACTOR for the Actor.
     *
     * @param duration The amount of time that the speed boost will last.
     */
    public void addSpeedBonus(int duration) {
        this.speedBonusDuration = duration;
        this.speedBonusStartTime = System.currentTimeMillis();
    }

    /**
     * This method is called to update the member variables moveLeft, moveRight,
     * moveUp, and moveDown with the current direction of movement.
     */
    private void checkDirection() {
        if (position != null && target != null) {
            moveLeft = position.getX() > target.getX();
            moveRight = position.getX() < target.getX();
            moveUp = position.getY() > target.getY();
            moveDown = position.getY() < target.getY();
        }
    }

    /**
     * This method is used to determine the current frame of animation to use.
     * The factors involved in determining the current frame are the current
     * direction of movement, as well as the time since the last frame update,
     * which is dependent on the FPS of this Actor.
     */
    protected void updateAnimation() {
        // Animate
        if (target == null || target.getIsWall()) {
            //If we can't move, go to the default frame
            if (xDefaultFrame != -1) {
                xCurFrame = xDefaultFrame;
            }
            if (yDefaultFrame != -1) {
                yCurFrame = yDefaultFrame;
            }
        } else {
            //Otherwise, update the current frame
            long now = System.currentTimeMillis();
            if (now - lastFrameTime > 1000 / FPS) {
                //If it's been enough time since the last frame
                if (xCurFrame == numFrames - 1) {
                    //Limit the current frame to the number of frames
                    xCurFrame = 0;
                } else {
                    xCurFrame++;
                }
                lastFrameTime = now;
            }

            //Update the direction of movement
            checkDirection();

            //Set the y position of the frame accordingly
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

    /**
     * Gets the current frame in the Actor's animation, accounting for direction
     * of movement.
     *
     * @param image The Actor's sprite sheet
     * @return
     */
    protected BufferedImage getCurrentFrame(BufferedImage image) {
        int frameWidth = image.getWidth() / numFrames;
        int frameHeight = image.getHeight() / 4;
        return image.getSubimage(xCurFrame * frameWidth, yCurFrame
                * frameHeight, frameWidth, frameHeight);
    }

    /**
     * This method is responsible for moving the Actor from one Tile to the
     * next, in a smooth fashion. This is done by changing the xMove and yMove
     * values
     */
    protected void doMove() {
        if (position == null || target == null || transitionComplete) {
            return;
        }

        if (target.equals(position)) {
            //End the animation if its reached its target
            transitionComplete = true;
        } else {
            //Update direction
            checkDirection();

            //These are the difference in pixels of the target and position of
            //the Actor. These are used to determine if the Actor has finished
            //moving to the target.
            int xDiff = target.getXPixels() - position.getXPixels();
            int yDiff = target.getYPixels() - position.getYPixels();

            // Tile.TILE_WIDTH / 8 is the standard speed of the player
            int xSpeed = (int) (speed * Tile.WIDTH / 8);
            int ySpeed = (int) (speed * Tile.HEIGHT / 8);

            long now = System.currentTimeMillis();

            //If we need to account for a speed bonus
            if (speedBonusDuration > 0) {
                //If the speed bonus is still in effect
                if (now - speedBonusStartTime < speedBonusDuration) {
                    //Do the speed bonus
                    xSpeed *= SPEED_BONUS_FACTOR;
                    ySpeed *= SPEED_BONUS_FACTOR;
                } else {
                    //The speed bonus is finished
                    speedBonusDuration = 0;
                }
            }

            //Move in the proper direction based on the direction determined by
            //checkDirection(). Update transitionComplete if the target has been reached.
            if (moveLeft) {
                xMove -= Math.abs(xSpeed);
                transitionComplete = xMove <= xDiff;
            } else if (moveRight) {
                xMove += Math.abs(xSpeed);
                transitionComplete = xMove >= xDiff;
            } else if (moveUp) {
                yMove -= Math.abs(ySpeed);
                transitionComplete = yMove <= yDiff;
            } else if (moveDown) {
                yMove += Math.abs(ySpeed);
                transitionComplete = yMove >= yDiff;
            }
        }

        //If we've determined that the animation is completed
        if (transitionComplete) {
            if (target != null) {
                //Set the position to be the target if it isn't null
                position = target;
            }

            //Reset the xMove and yMove and target values
            xMove = 0;
            yMove = 0;
            target = null;
        }
    }

    /**
     * This method is responsible for resetting the Actor to its default
     * starting position. This is useful for Actors that must remain intact
     * between Levels.
     *
     * @param newLevel The new Level of the Actor.
     * @param newPosition The new position Tile of the Actor.
     */
    public void reset(Level newLevel, Tile newPosition) {
        this.currentLevel = newLevel;
        this.position = newPosition;

        //Reset health to max
        health = MAX_HEALTH;
        // Avoid glitch where animation keeps going when map changes
        transitionComplete = true;
        //No target
        target = null;
        //Reset xMove and yMove
        xMove = 0;
        yMove = 0;
    }

    /**
     * This method returns the Actor's position.
     *
     * @return The Actor's position Tile.
     */
    public Tile getPosition() {
        return position;
    }

    /**
     * This method returns the Actor's target, which is the Tile that the Actor
     * is trying to get to (always adjacent to the position Tile).
     *
     * @return The Actor's target Tile.
     */
    public Tile getTarget() {
        return target;
    }

    /**
     * Gets the Level that the Actor is currently situated in.
     *
     * @return The Level that the Actor is currently situated in.
     */
    public Level getCurrentLevel() {
        return currentLevel;
    }

    /**
     * This method returns the Actor's health.
     *
     * @return The Actor's health.
     */
    public int getHealth() {
        return health;
    }
}
