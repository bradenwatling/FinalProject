
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Player extends Actor implements KeyListener {

    /**
     * Default time between shots
     */
    public static final int DEFAULT_SHOOT_INTERVAL = 1000;
    public static final int NUM_FRAMES = 3;
    /**
     * http://www.spriters-resource.com/community/printthread.php?tid=19817
     *
     * http://charas-project.net/charas2/index.php
     */
    public static BufferedImage playerImage;
    private ArrayList<Projectile> projectiles;
    private ArrayList<Character> keys;
    private int shootInterval;
    private long lastProjectileTime;

    public Player(Tile position, Level currentLevel,
            ArrayList<Projectile> projectiles, int health) {
        super(position, currentLevel, NUM_FRAMES, 15);
        this.projectiles = projectiles;
        this.health = health;
        keys = new ArrayList<Character>();
        shootInterval = DEFAULT_SHOOT_INTERVAL;
        lastProjectileTime = 0;

        lightRadius *= 2;

        xDefaultFrame = 1;
        yDefaultFrame = -1; // Dont change the direction of the player when it
        // stops moving
    }

    @Override
    void update() {
        if (currentLevel == null || position == null) {
            return;
        }

        if (keys.contains(' ')) {
            Tile projectileDirection = null;
            Tile parent = (target != null && !target.getIsWall()) ? target
                    : position;
            if (moveLeft) {
                projectileDirection = currentLevel.getLeft(parent);
            } else if (moveRight) {
                projectileDirection = currentLevel.getRight(parent);
            } else if (moveUp) {
                projectileDirection = currentLevel.getUp(parent);
            } else if (moveDown) {
                projectileDirection = currentLevel.getDown(parent);
            }
            long now = System.currentTimeMillis();
            if (projectileDirection != null && !projectileDirection.getIsWall()
                    && now - lastProjectileTime > shootInterval) {
                projectiles.add(new Projectile(parent, projectileDirection,
                        currentLevel));
                lastProjectileTime = now;
            }
        }

        if (animationComplete) {
            if (keys.size() > 0) {
                for (int i = keys.size() - 1; i >= 0; i--) {
                    char c = keys.get(i);

                    if (c == 'a' || c == 'd' || c == 'w' || c == 's') {
                        switch (c) {
                            case 'a':
                                target = currentLevel.getLeft(position);
                                break;
                            case 'd':
                                target = currentLevel.getRight(position);
                                break;
                            case 'w':
                                target = currentLevel.getUp(position);
                                break;
                            case 's':
                                target = currentLevel.getDown(position);
                                break;
                        }
                        break;
                    }
                }
            }

            moveToTarget();
        }

        updateAnimation();
    }

    @Override
    void draw(Graphics2D g) {
        if (playerImage != null) {
            BufferedImage frame = getCurrentFrame(playerImage);
            if (doMove(g, frame) && position != null) {
                drawImage(g, frame, position.getXPixels(),
                        position.getYPixels());
            }
        }
    }

    @Override
    protected Area getLight() {
        int x = position.getXPixels() + xMove + Tile.WIDTH / 2;
        int y = position.getYPixels() + yMove + Tile.HEIGHT / 2;

        return new Area(new Ellipse2D.Float(x - lightRadius / 2, y
                - lightRadius / 2, lightRadius, lightRadius));
    }

    void move(boolean xDirection, boolean yDirection) {
    }

    public void reset(Level currentLevel, Tile position) {
        super.reset(currentLevel, position);

        keys.clear();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        char c = Character.toLowerCase(e.getKeyChar());
        if (!keys.contains(c)) {
            keys.add(c);
        }
    }

    public void keyReleased(KeyEvent e) {
        char c = Character.toLowerCase(e.getKeyChar());

        if (keys.contains(c)) {
            keys.remove(keys.indexOf(c));
        }
    }
}
