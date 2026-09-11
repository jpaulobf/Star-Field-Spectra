package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** Orquestra a primeira fase: entrada gradual, formação e encerramento. */
public class LevelOne {

    private static final int ENEMY_COUNT = 50;
    private static final int DIAGONAL_ENEMY_COUNT = 18;
    private static final int PARABOLIC_START = 18;
    private static final int STRAIGHT_START = 33;
    private static final long PHASE_START_DELAY = 1_000_000_000L;
    private static final long DIAGONAL_GROUP_INTERVAL = 2_200_000_000L;
    private static final long FORMATION_INTERVAL = 180_000_000L;
    private static final long STRAIGHT_INTERVAL = 650_000_000L;

    private final Enemy[] enemies = new Enemy[ENEMY_COUNT];
    private final boolean[] active = new boolean[ENEMY_COUNT];
    private final Graphics2D g2d;
    private final short panelWidth;
    private final short panelHeight;
    private long elapsed;
    private long phaseElapsed;
    private int nextSpawn;
    private int phase;
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
        phaseElapsed += frametime;
        spawnCurrentPhase();

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
            boolean outsideScreen = enemy.getPositionX() + enemy.getWidth() < 0
                    || enemy.getPositionY() > panelHeight
                    || enemy.getPositionY() + enemy.getHeight() < 0;
            if (!enemy.isDestroyed && outsideScreen) {
                active[count] = false;
            }
            if (enemy.isDestroyed && enemy.isDestroyedAnimationDone()) {
                active[count] = false;
                score += 100;
            }
        }

        separateEnemies();

        advancePhaseWhenCleared();
    }

    private void spawnCurrentPhase() {
        long startDelay = phase == 0 || phase == 1 ? PHASE_START_DELAY : 0;
        long interval = phase == 0 ? DIAGONAL_GROUP_INTERVAL
            : phase == 2 ? STRAIGHT_INTERVAL : FORMATION_INTERVAL;
        int phaseStart = phase == 0 ? 0 : phase == 1 ? PARABOLIC_START : STRAIGHT_START;
        int phaseEnd = phase == 0 ? DIAGONAL_ENEMY_COUNT
            : phase == 1 ? STRAIGHT_START : ENEMY_COUNT;

        while (nextSpawn < phaseEnd && phaseElapsed >= startDelay
                + getSpawnOffset(nextSpawn, phaseStart, interval)) {
                int parabolicOffset = nextSpawn - PARABOLIC_START;
                if (phase == 1 && parabolicOffset > 0
                    && parabolicOffset % 5 == 0
                    && !isParabolicTrainReady(nextSpawn - 5)) {
                break;
            }
            spawn(nextSpawn);
            nextSpawn++;
        }
    }

    private boolean isParabolicTrainReady(int firstEnemyIndex) {
        if (!active[firstEnemyIndex]) {
            return true;
        }
        Enemy firstEnemy = enemies[firstEnemyIndex];
        return firstEnemy.isDestroyed || firstEnemy.getPositionX() <= panelWidth / 2;
    }

    private long getSpawnOffset(int index, int phaseStart, long interval) {
        if (phase == 0) {
            return getDiagonalGroup(index) * interval;
        }
        return (index - phaseStart) * interval;
    }

    private void advancePhaseWhenCleared() {
        int phaseEnd = phase == 0 ? DIAGONAL_ENEMY_COUNT
            : phase == 1 ? STRAIGHT_START : ENEMY_COUNT;
        int phaseStart = phase == 0 ? 0
            : phase == 1 ? PARABOLIC_START : STRAIGHT_START;
        if (nextSpawn < phaseEnd || (phase > 0 && hasActiveEnemies(phaseStart, phaseEnd))) {
            return;
        }

        if (phase == 2) {
            complete = true;
            return;
        }

        phase++;
        phaseElapsed = 0;
        nextSpawn = phase == 1 ? PARABOLIC_START : STRAIGHT_START;
    }

    private void spawn(int index) {
        short x;
        short y;
        int diagonalDirection;
        boolean parabolic;
        double speed;

        if (index < DIAGONAL_ENEMY_COUNT) {
            int groupIndex = getDiagonalGroup(index);
            int memberIndex = index - getDiagonalGroupStart(groupIndex);
            boolean enteringFromTop = groupIndex % 2 != 0;
            x = (short)(panelWidth - 80 - memberIndex * 110);
                y = enteringFromTop ? (short)-20 : panelHeight;
            diagonalDirection = enteringFromTop ? 1 : -1;
            parabolic = false;
            speed = 1.8D;
            long shotDelay = memberIndex * 20L * 16_666_666L;
            enemies[index].reset(x, y, parabolic, diagonalDirection, speed, shotDelay);
        } else if (index < STRAIGHT_START) {
            int groupIndex = (index - PARABOLIC_START) / 5;
            int memberIndex = (index - PARABOLIC_START) % 5;
            x = (short)(panelWidth + 60 + memberIndex * 45);
            y = (short)(130 + groupIndex * 90);
            diagonalDirection = 0;
            parabolic = true;
            speed = 1.65D + (memberIndex % 3) * 0.12D;
            long shotDelay = memberIndex * 20L * 16_666_666L;
            enemies[index].reset(x, y, parabolic, diagonalDirection, speed, shotDelay);
        } else {
            int memberIndex = index - STRAIGHT_START;
            x = (short)(panelWidth + 60);
            y = (short)(40 + (memberIndex % 7) * 65);
            diagonalDirection = 0;
            parabolic = false;
            speed = 3.2D + (memberIndex % 4) * 0.35D;
            enemies[index].reset(x, y, parabolic, diagonalDirection, speed, 0);
        }
        active[index] = true;
    }

    private int getDiagonalGroup(int index) {
        int[] groupStarts = { 0, 3, 6, 9, 12, 15 };
        int[] groupSizes = { 3, 3, 3, 3, 3, 3 };
        for (int group = 0; group < groupStarts.length; group++) {
            if (index < groupStarts[group] + groupSizes[group]) {
                return group;
            }
        }
        return groupStarts.length - 1;
    }

    private int getDiagonalGroupStart(int group) {
        int[] groupStarts = { 0, 3, 6, 9, 12, 15 };
        return groupStarts[group];
    }

    private boolean hasActiveEnemies() {
        return hasActiveEnemies(0, enemies.length);
    }

    private boolean hasActiveEnemies(int start, int end) {
        for (int count = start; count < end; count++) {
            if (active[count]) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyActiveEnemies() {
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
                firstEnemy.reverseVerticalDirection();
                secondEnemy.reverseVerticalDirection();
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
        int progress = (int)(180 * Math.min(1D, (double)nextSpawn / ENEMY_COUNT));
        g2d.fillRect(20, 58, progress, 6);

        if (complete) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 26));
            g2d.drawString("FASE 1-1 CONCLUIDA", panelWidth / 2 - 150, panelHeight / 2);
        }
    }
}