import java.awt.Color;
import java.awt.Graphics2D;

/* Classe para os inimigos */
public class Enemy extends Sprite {

    /* Membros */    
    private final byte maxBullets   = 20;
    private short currentBulletPos  = 0;
    private short calcBulletsPS     = 0;
    private long frameCount         = 0;
    protected Bullet[] bullets      = new Bullet[maxBullets];
    private byte direction           = 0; //0 - up | 1 - down

    /* Construtor */
    public Enemy(short panelWidth, short panelHeight, Graphics2D g2d, short bulletsPerSeconds) {
        super(panelWidth, panelHeight, g2d);
        this.spriteWidth                = 50;
        this.spriteHeight               = 50;
        this.positionX                  = 400;
        this.positionY                  = 100;
        this.speed                      = 2;
        this.calcBulletsPS              = (short)(60 / bulletsPerSeconds);
        this.halfSpriteWidth            = (short)(this.spriteWidth / 2);
        this.halfSpriteHeight           = (short)(this.spriteHeight / 2);
        this.destructionAnimationStep   = 2;
    }

    /* Desenha a nave e seus adendos */
    public void draw() {
        this.g2d.setColor(Color.red);
        if (!this.isDestroyed) {
            this.g2d.drawRect(positionX, positionY, spriteWidth, spriteHeight);
        } else {
            if (!this.destroyedAnimationDone) {
                this.drawDestroyAnimation();
            }
        }
        //desenha as balas
        for (Bullet bullet : this.bullets) {
            if (bullet != null) bullet.draw();
        }
    }

    /* Animação da destruição do Sprite */
    protected void drawDestroyAnimation() {
        this.g2d.setColor(Color.red);

        this.g2d.drawOval(this.destroyAnimationX + this.halfSpriteWidth, 
                          this.destroyAnimationY + this.halfSpriteHeight, 
                          this.destroyAnimationWidth, 
                          this.destroyAnimationHeight);

        this.g2d.drawOval(this.destroyAnimationX + this.halfSpriteWidth + 8, 
                          this.destroyAnimationY + this.halfSpriteHeight + 8, 
                          this.destroyAnimationWidth - 16, 
                          this.destroyAnimationHeight - 16);

        this.g2d.drawOval(this.destroyAnimationX + this.halfSpriteWidth + 16, 
                          this.destroyAnimationY + this.halfSpriteHeight + 16, 
                          this.destroyAnimationWidth - 32, 
                          this.destroyAnimationHeight - 32);

        this.destroyAnimationX -= destructionAnimationStep / 2;
        this.destroyAnimationY -= destructionAnimationStep / 2;
    }

    /* Atualiza a nave e seus adendos */
    public void update(long timeStamp, Sprite spaceship) {
        if (!this.isDestroyed) {
            //Atira a cada X quadros
            if (this.frameCount > this.calcBulletsPS) {
                this.shoot(timeStamp);
                this.frameCount = 0;
            }
            //Move para cima e para baixo
            if (this.frameCount%2 == 0) {
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
                this.destroyAnimationWidth  += this.destructionAnimationStep;
                this.destroyAnimationHeight += this.destructionAnimationStep;
                if (this.destroyAnimationWidth >= this.spriteWidth) {
                    this.destroyedAnimationDone = true;
                }
            }
        }

        for (int count = 0; count < this.bullets.length; count++) {
            var bullet = this.bullets[count];
            if (bullet != null) {
                bullet.update(timeStamp, spaceship);

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
                        spaceship.hasCollided();
                    }
                    if (Sprite.areCollidingBomb(((Spaceship)spaceship).getBomb(), bullet)) {
                        bullet.hasCollided();
                    }
                }
            }
        }
        this.frameCount++;
    }

    /* Move a nave para baixo */
    private void moveDown() {
        short next = (short)(this.positionY + this.speed);
        if (next > this.panelHeight - spriteHeight - 1) {
            next = (short)(this.panelHeight - spriteHeight - 1);
        }
        this.positionY = next;
    }

    /* Move a nave para cima */
    private void moveUp() {
        short next = (short)(this.positionY - this.speed);
        if (next < 0) {
            next = 0;
        }
        this.positionY = next;
    }

    /* Atira com a nave */        
    private void shoot(long timeStamp) {
        short x = (short)(this.positionX - 2);
        short y = (short)(this.positionY + this.halfSpriteHeight);
        this.bullets[currentBulletPos++%maxBullets] = new Bullet((short)90, x, y, this.panelWidth, this.panelHeight, false, this.g2d);
    }
}