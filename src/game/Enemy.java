package game;

import java.awt.Color;
import java.awt.Graphics2D;

/* Classe para os inimigos */
public class Enemy extends Sprite {

    /* Membros */
    private final byte maxBullets = 20;
    private short currentBulletPos = 0;
    private short calcBulletsPS = 0;
    private long frameCount = 0;
    protected Bullet[] bullets = new Bullet[maxBullets];
    private byte direction = 0; // 0 - up | 1 - down

    /* Construtor */
    public Enemy(short panelWidth, short panelHeight, Graphics2D g2d, short bulletsPerSeconds) {
        super(panelWidth, panelHeight, g2d);
        this.spriteWidth = 50;
        this.spriteHeight = 50;
        this.positionX = 400;
        this.positionY = 100;
        this.speed = 2;
        this.defaultSpeed = 2D;
        this.calcBulletsPS = (short) (60 / bulletsPerSeconds);
        this.halfSpriteWidth = (short) (this.spriteWidth / 2);
        this.halfSpriteHeight = (short) (this.spriteHeight / 2);
        this.defaultDestructionAnimationStep = 2;
        this.destructionAnimationStep = this.defaultDestructionAnimationStep;
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
            // Atira a cada X quadros
            if (this.frameCount > (this.calcBulletsPS / (double)(frametime / 16666666D))) {
                this.shoot(frametime);
                this.frameCount = 0;
            }
            // Move para cima e para baixo
            if (this.frameCount % 2 == 0) {
                if (this.direction == 0) {
                    if (this.positionY <= 0) {
                        this.direction = 1;
                    } else {
                        this.moveUp();
                    }
                } else {
                    if (this.positionY >= this.panelHeight - spriteHeight - 1) {
                        this.direction = 0;
                    } else {
                        this.moveDown();
                    }
                }
            }
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
                    if (Sprite.areColliding(bullet, spaceship)) {
                        spaceship.hasCollided(true);
                    }
                    if (Sprite.areCollidingBomb(((Spaceship) spaceship).getBomb(), bullet)) {
                        bullet.hasCollided(false);
                    }
                }
            }
        }
        this.frameCount++;
    }

    /* Move a nave para baixo */
    private void moveDown() {
        double next = (double) (this.positionY + this.speed);
        if (next > this.panelHeight - spriteHeight - 1) {
            next = (double) (this.panelHeight - spriteHeight - 1);
        }
        this.positionY = (short) Math.ceil(next);
    }

    /* Move a nave para cima */
    private void moveUp() {
        double next = (double) (this.positionY - this.speed);
        if (next < 0) {
            next = 0;
        }
        this.positionY = (short) Math.floor(next);
    }

    /* Atira com a nave */
    private void shoot(long frametime) {
        short x = (short) (this.positionX - 2);
        short y = (short) (this.positionY + this.halfSpriteHeight);
        this.bullets[currentBulletPos++ % maxBullets] = new Bullet((short) 45, x, y, this.panelWidth, this.panelHeight,
                false, this.g2d);
    }
}