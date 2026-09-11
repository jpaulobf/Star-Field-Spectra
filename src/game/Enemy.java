package game;

import java.awt.Color;
import java.awt.Graphics2D;

/* Classe para os inimigos */
public class Enemy extends Sprite {

    /* Membros */
    private final byte maxBullets = 20;
    private short currentBulletPos = 0;
    private long shotElapsed = 0;
    private long initialShotDelay = 0;
    private long movementElapsed = 0;
    private double parabolicPhase = 0;
    private short baseY = 0;
    private boolean parabolicMovement = false;
    private int diagonalDirection = 0;
    private int verticalDirection = 1;
    protected Bullet[] bullets = new Bullet[maxBullets];

    /* Construtor */
    public Enemy(short panelWidth, short panelHeight, Graphics2D g2d, short bulletsPerSeconds) {
        super(panelWidth, panelHeight, g2d);
        this.spriteWidth = 50;
        this.spriteHeight = 50;
        this.positionX = 400;
        this.positionY = 100;
        this.speed = 2;
        this.defaultSpeed = 2D;
        this.halfSpriteWidth = (short) (this.spriteWidth / 2);
        this.halfSpriteHeight = (short) (this.spriteHeight / 2);
        this.defaultDestructionAnimationStep = 2;
        this.destructionAnimationStep = this.defaultDestructionAnimationStep;
    }

    public void reset(short positionX, short positionY) {
        this.reset(positionX, positionY, false, 2D);
    }

    public void reset(short positionX, short positionY, boolean parabolicMovement) {
        this.reset(positionX, positionY, parabolicMovement, 2D);
    }

    public void reset(short positionX, short positionY, boolean parabolicMovement, double movementSpeed) {
        this.reset(positionX, positionY, parabolicMovement, 0, movementSpeed);
    }

    public void reset(short positionX, short positionY, boolean parabolicMovement,
                      int diagonalDirection, double movementSpeed) {
        this.reset(positionX, positionY, parabolicMovement, diagonalDirection, movementSpeed, 0);
    }

    public void reset(short positionX, short positionY, boolean parabolicMovement,
                      int diagonalDirection, double movementSpeed, long initialShotDelay) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.baseY = positionY;
        this.parabolicMovement = parabolicMovement;
        this.diagonalDirection = diagonalDirection;
        this.verticalDirection = 1;
        this.defaultSpeed = movementSpeed;
        this.isDestroyed = false;
        this.isToAnimateDestruction = false;
        this.destroyedAnimationDone = false;
        this.destroyAnimationWidth = 0;
        this.destroyAnimationHeight = 0;
        this.shotElapsed = 0;
        this.initialShotDelay = initialShotDelay;
        this.movementElapsed = 0;
        this.parabolicPhase = 0;
        this.currentBulletPos = 0;
        for (int count = 0; count < this.bullets.length; count++) {
            this.bullets[count] = null;
        }
    }

    public boolean hasParabolicMovement() {
        return this.parabolicMovement;
    }

    /* Desenha a nave e seus adendos */
    public void draw() {
        this.g2d.setColor(Color.red);
        if (!this.isDestroyed) {
            this.g2d.drawRect((int) this.positionX, (int) this.positionY, this.spriteWidth, this.spriteHeight);
        } else {
            if (!this.destroyedAnimationDone) {
                this.drawDestroyAnimation();
            }
        }
        // desenha as balas
        for (Bullet bullet : this.bullets) {
            if (bullet != null)
                bullet.draw();
        }
    }

    /* Animação da destruição do Sprite */
    protected void drawDestroyAnimation() {
        this.drawDestructionParticles();
    }

    /* Atualiza a nave e seus adendos */
    public void update(long frametime, Sprite spaceship) {

        this.speed = this.defaultSpeed * (double) (frametime / 16666666D);

        if (!this.isDestroyed) {
            this.shotElapsed += frametime;
            if (this.shotElapsed >= 1_200_000_000L + this.initialShotDelay) {
                this.shoot(frametime);
                this.shotElapsed = 0;
                this.initialShotDelay = 0;
            }
            this.advance(frametime);
        } else {
            if (this.isToAnimateDestruction && !this.destroyedAnimationDone) {
                this.destructionAnimationStep = this.defaultDestructionAnimationStep * ((double) frametime / 16_666_666D);
                this.destroyAnimationWidth += this.destructionAnimationStep;
                this.destroyAnimationHeight += this.destructionAnimationStep;
                if (this.destroyAnimationWidth >= this.spriteWidth) {
                    this.destroyedAnimationDone = true;
                }
            }
        }

        for (int count = 0; count < this.bullets.length; count++) {
            var bullet = this.bullets[count];
            if (bullet != null) {
                bullet.update(frametime, spaceship);

                if (bullet.bulletDestroyed()) {
                    if (bullet.isToAnimateDestruction()) {
                        if (bullet.isDestroyedAnimationDone()) {
                            bullet = null;
                            this.bullets[count] = null;
                        }
                    } else {
                        bullet = null;
                        this.bullets[count] = null;
                    }
                } else {
                        if (!((Spaceship) spaceship).isRespawning()
                            && Sprite.areColliding(bullet, spaceship)) {
                        spaceship.hasCollided(true);
                        bullet.hasCollided(false);
                    }
                    if (Sprite.areCollidingBomb(((Spaceship) spaceship).getBomb(), bullet)) {
                        bullet.hasCollided(false);
                    }
                }
            }
        }
    }

    private void advance(long frametime) {
        double frameScale = frametime / 16_666_666D;
        this.positionX -= this.defaultSpeed * frameScale;

        if (this.diagonalDirection != 0) {
            this.positionY += this.defaultSpeed * this.diagonalDirection * 0.45D * frameScale;
        }

        if (this.parabolicMovement) {
            this.movementElapsed += frametime;
            this.parabolicPhase += frametime / 1_000_000_000D * 2.4D * this.verticalDirection;
            double arc = Math.sin(this.parabolicPhase) * 70D;
            double nextY = this.baseY + arc;
            this.positionY = Math.max(0, Math.min(this.panelHeight - this.spriteHeight - 1, nextY));
        }
    }

    public void reverseVerticalDirection() {
        if (this.diagonalDirection != 0) {
            this.diagonalDirection *= -1;
        }
        if (this.parabolicMovement) {
            this.verticalDirection *= -1;
        }
    }

    /* Atira com a nave */
    private void shoot(long frametime) {
        short x = (short) (this.positionX - 2);
        short y = (short) (this.positionY + this.halfSpriteHeight);
        this.bullets[currentBulletPos++ % maxBullets] = new Bullet((short) 45, x, y, this.panelWidth, this.panelHeight,
                false, this.g2d);
    }

    public long getMovementElapsed() {
        return movementElapsed;
    }
}