package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** Orquestra a primeira fase: entrada gradual, formação e encerramento. */
public class LevelOne {

    private static final int ENEMY_COUNT = 15;
    private static final long[] SPAWN_TIMES = {
        1_000_000_000L, 3_500_000_000L, 6_000_000_000L,
        8_000_000_000L, 8_180_000_000L, 8_360_000_000L,
        11_000_000_000L, 11_180_000_000L, 11_360_000_000L, 11_540_000_000L,
        14_000_000_000L, 14_180_000_000L, 14_360_000_000L, 14_540_000_000L,
        14_720_000_000L
    };

    private final Enemy[] enemies = new Enemy[ENEMY_COUNT];
    private final boolean[] active = new boolean[ENEMY_COUNT];
    private final Graphics2D g2d;
    private final short panelWidth;
    private final short panelHeight;
    private long elapsed;
    private int nextSpawn;
    private int score;
    private boolean complete;

    public LevelOne(short panelWidth, short panelHeight, Graphics2D g2d, short bulletsPerSecond) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.g2d = g2d;
        for (int count = 0; count < enemies.length; count++) {
            enemies[count] = new Enemy(panelWidth, panelHeight, g2d, bulletsPerSecond);
            enemies[count].reset((short)-1000, (short)-1000);
        }
    }

    public void update(long frametime, Spaceship spaceship) {
        if (complete) {
            return;
        }

        elapsed += frametime;
        while (nextSpawn < SPAWN_TIMES.length && elapsed >= SPAWN_TIMES[nextSpawn]) {
            spawn(nextSpawn);
            nextSpawn++;
        }

        for (int count = 0; count < enemies.length; count++) {
            if (!active[count]) {
                continue;
            }

            Enemy enemy = enemies[count];
            enemy.update(frametime, spaceship);
                if (!enemy.isDestroyed && !spaceship.isDestroyed && !spaceship.isRespawning()
                    && Sprite.areColliding(enemy, spaceship)) {
                enemy.hasCollided(false);
                spaceship.hasCollided(true);
            }
            if (!enemy.isDestroyed && enemy.getPositionX() + enemy.getWidth() < 0) {
                active[count] = false;
            }
            if (enemy.isDestroyed && enemy.isDestroyedAnimationDone()) {
                active[count] = false;
                score += 100;
            }
        }

        separateEnemies();

        if (nextSpawn == SPAWN_TIMES.length && !hasActiveEnemies()
                && elapsed > SPAWN_TIMES[SPAWN_TIMES.length - 1] + 2_000_000_000L) {
            complete = true;
        }
    }

    private void spawn(int index) {
        boolean parabolic = index >= 3;
        int groupStart = index < 3 ? index : index < 6 ? 3 : index < 10 ? 6 : 10;
        int memberIndex = index - groupStart;
        short x = (short)(panelWidth + 60 + (parabolic ? memberIndex * 45 : 0));
        short y = (short)(parabolic ? groupStart == 3 ? 150 : groupStart == 6 ? 250 : 180
            : 70 + index * 140);
        double speed = parabolic ? 1.65D + memberIndex * 0.12D : 1.35D + index * 0.65D;
        enemies[index].reset(x, y, parabolic, speed);
        active[index] = true;
    }

    private boolean hasActiveEnemies() {
        for (boolean value : active) {
            if (value) {
                return true;
            }
        }
        return false;
    }

    private void separateEnemies() {
        for (int first = 0; first < enemies.length; first++) {
            if (!active[first] || enemies[first].isDestroyed) {
                continue;
            }
            for (int second = first + 1; second < enemies.length; second++) {
                if (!active[second] || enemies[second].isDestroyed
                        || !Sprite.areColliding(enemies[first], enemies[second])) {
                    continue;
                }

                Enemy firstEnemy = enemies[first];
                Enemy secondEnemy = enemies[second];
                double firstCenterX = firstEnemy.positionX + firstEnemy.halfSpriteWidth;
                double secondCenterX = secondEnemy.positionX + secondEnemy.halfSpriteWidth;
                double firstCenterY = firstEnemy.positionY + firstEnemy.halfSpriteHeight;
                double secondCenterY = secondEnemy.positionY + secondEnemy.halfSpriteHeight;
                double overlapX = Math.min(firstEnemy.positionX + firstEnemy.spriteWidth,
                        secondEnemy.positionX + secondEnemy.spriteWidth)
                        - Math.max(firstEnemy.positionX, secondEnemy.positionX);
                double overlapY = Math.min(firstEnemy.positionY + firstEnemy.spriteHeight,
                        secondEnemy.positionY + secondEnemy.spriteHeight)
                        - Math.max(firstEnemy.positionY, secondEnemy.positionY);

                if (firstEnemy.hasParabolicMovement() ^ secondEnemy.hasParabolicMovement()) {
                    Enemy waveEnemy = firstEnemy.hasParabolicMovement() ? firstEnemy : secondEnemy;
                    Enemy otherEnemy = firstEnemy.hasParabolicMovement() ? secondEnemy : firstEnemy;
                    double deltaX = waveEnemy.positionX - otherEnemy.positionX;
                    double deltaY = waveEnemy.positionY - otherEnemy.positionY;
                    if (overlapX <= overlapY) {
                        waveEnemy.positionX += deltaX <= 0 ? -(overlapX + 1D) : overlapX + 1D;
                    } else {
                        waveEnemy.positionY += deltaY <= 0 ? -(overlapY + 1D) : overlapY + 1D;
                    }
                } else if (overlapX <= overlapY) {
                    double direction = firstCenterX <= secondCenterX ? -1D : 1D;
                    firstEnemy.positionX += direction * (overlapX / 2D + 1D);
                    secondEnemy.positionX -= direction * (overlapX / 2D + 1D);
                } else {
                    double direction = firstCenterY <= secondCenterY ? -1D : 1D;
                    firstEnemy.positionY += direction * (overlapY / 2D + 1D);
                    secondEnemy.positionY -= direction * (overlapY / 2D + 1D);
                }
            }
        }
    }

    public Sprite[] getEnemies() {
        return enemies;
    }

    public void draw() {
        for (int count = 0; count < enemies.length; count++) {
            if (active[count]) {
                enemies[count].draw();
            }
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("FASE 1-1", 20, 28);
        g2d.drawString("PONTOS " + score, 20, 48);
        g2d.setColor(new Color(255, 255, 255, 90));
        g2d.drawRect(20, 58, 180, 6);
        g2d.setColor(new Color(90, 220, 255));
        int progress = (int)(180 * Math.min(1D, (double)nextSpawn / SPAWN_TIMES.length));
        g2d.fillRect(20, 58, progress, 6);

        if (complete) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 26));
            g2d.drawString("FASE 1-1 CONCLUIDA", panelWidth / 2 - 150, panelHeight / 2);
        }
    }
}