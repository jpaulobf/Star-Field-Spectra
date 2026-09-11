package game;

import java.awt.Color;
import java.awt.Graphics2D;

/* Classe para os inimigos */
public class Enemy extends Sprite {

    /* Membros */
    private final byte maxBullets = 20;
    private short currentBulletPos = 0;
    private long shotElapsed = 0;
    private long movementElapsed = 0;
    private short baseY = 0;
    private boolean parabolicMovement = false;
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
        this.positionX = positionX;
        this.positionY = positionY;
        this.baseY = positionY;
        this.parabolicMovement = parabolicMovement;
        this.defaultSpeed = movementSpeed;
        this.isDestroyed = false;
        this.isToAnimateDestruction = false;
        this.destroyedAnimationDone = false;
        this.destroyAnimationWidth = 0;
        this.destroyAnimationHeight = 0;
        this.shotElapsed = 0;
        this.movementElapsed = 0;
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
        this.g2d.setColor(Color.red);
        short offsetY = 8;

        this.g2d.drawOval((int) this.destroyAnimationX + this.halfSpriteWidth,
                (int) this.destroyAnimationY + this.halfSpriteHeight + offsetY,
                (int) this.destroyAnimationWidth,
                (int) this.destroyAnimationHeight);

        this.g2d.drawOval((int) this.destroyAnimationX + this.halfSpriteWidth + 8,
                (int) this.destroyAnimationY + this.halfSpriteHeight + offsetY + 8,
                (int) this.destroyAnimationWidth - 16,
                (int) this.destroyAnimationHeight - 16);

        this.g2d.drawOval((int) this.destroyAnimationX + this.halfSpriteWidth + 16,
                (int) this.destroyAnimationY + this.halfSpriteHeight + offsetY + 16,
                (int) this.destroyAnimationWidth - 32,
                (int) this.destroyAnimationHeight - 32);

        this.destroyAnimationX -= destructionAnimationStep / 2;
        this.destroyAnimationY -= destructionAnimationStep / 2;
    }

    /* Atualiza a nave e seus adendos */
    public void update(long frametime, Sprite spaceship) {

        this.speed = this.defaultSpeed * (double) (frametime / 16666666D);

        if (!this.isDestroyed) {
            this.shotElapsed += frametime;
            if (this.shotElapsed >= 1_200_000_000L) {
                this.shoot(frametime);
                this.shotElapsed = 0;
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

        if (this.parabolicMovement) {
            this.movementElapsed += frametime;
            double phase = this.movementElapsed / 1_000_000_000D * 2.4D;
            double arc = Math.sin(phase) * 70D;
            double nextY = this.baseY + arc;
            this.positionY = Math.max(0, Math.min(this.panelHeight - this.spriteHeight - 1, nextY));
        }
    }

    /* Atira com a nave */
    private void shoot(long frametime) {
        short x = (short) (this.positionX - 2);
        short y = (short) (this.positionY + this.halfSpriteHeight);
        this.bullets[currentBulletPos++ % maxBullets] = new Bullet((short) 45, x, y, this.panelWidth, this.panelHeight,
                false, this.g2d);
    }
}