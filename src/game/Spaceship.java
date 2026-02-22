package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

/*
    Inner class Spaceship
*/
public class Spaceship extends Sprite {

    /* Membros */ 
    private final byte maxBullets       = 100;
    private short currentBulletPos      = 0;
    private long lastShot               = 0;
    protected Bullet[] bullets          = new Bullet[maxBullets];
    private byte numberOfBombs          = 5;
    private short bombSpeed             = 0;
    private short defaultBombSpeed      = 12;
    private Arc2D bomb                  = null;
    public boolean L                    = false; //left
    public boolean R                    = false; //right
    public boolean D                    = false; //down
    public boolean U                    = false; //up
    public boolean S                    = false; //shoot
    public boolean B                    = false; //bomb
    private BufferedImage spaceshipImg  = null;

    /* Construtor */
    public Spaceship(short panelWidth, short panelHeight, Graphics2D g2d) {
        super(panelWidth, panelHeight, g2d);
        
        try {
            this.spaceshipImg       = ImageIO.read(new File("img\\2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.spriteWidth                = (short)spaceshipImg.getWidth();
        this.spriteHeight               = (short)spaceshipImg.getHeight();
        this.positionX                  = 10;
        this.defaultSpeed               = 3D;
        this.positionY                  = (short)((this.panelHeight / 2) - (this.spriteHeight / 2));
        this.halfSpriteWidth            = (short)(this.spriteWidth / 2);
        this.halfSpriteHeight           = (short)(this.spriteHeight / 2);
        this.defaultDestructionAnimationStep = 2;
        this.destructionAnimationStep = this.defaultDestructionAnimationStep;
    }

    /* Desenha a nave e seus adendos */
    public void draw() {
        this.g2d.setColor(Color.red);
        if (!this.isDestroyed) {
            //desenha a nave
            //Copia o buffer para o panel
            this.g2d.drawImage(this.spaceshipImg, (int)positionX, (int)positionY, null);
            //this.g2d.drawRect(positionX, positionY, spriteWidth, spriteHeight);
            //desenha a bomba (se houver)
            if (this.bomb != null) {
                this.g2d.drawArc((int)this.bomb.getX(), 
                                 (int)this.bomb.getY(), 
                                 (int)this.bomb.getWidth(), 
                                 (int)this.bomb.getHeight(), 0, 360);
            }
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

        this.g2d.drawOval((int)this.destroyAnimationX + this.halfSpriteWidth, 
                          (int)this.destroyAnimationY + this.halfSpriteHeight, 
                          (int)this.destroyAnimationWidth, 
                          (int)this.destroyAnimationHeight);

        this.g2d.drawOval((int)this.destroyAnimationX + this.halfSpriteWidth + 8, 
                          (int)this.destroyAnimationY + this.halfSpriteHeight + 8, 
                          (int)this.destroyAnimationWidth - 16, 
                          (int)this.destroyAnimationHeight - 16);

        this.g2d.drawOval((int)this.destroyAnimationX + this.halfSpriteWidth + 16, 
                          (int)this.destroyAnimationY + this.halfSpriteHeight + 16, 
                          (int)this.destroyAnimationWidth - 32, 
                          (int)this.destroyAnimationHeight - 32);

        this.destroyAnimationX -= destructionAnimationStep / 2;
        this.destroyAnimationY -= destructionAnimationStep / 2;
    }

    /* Atualiza a nave e seus adendos */
    public void update(long frametime, Sprite enemy) {

        this.speed = this.defaultSpeed * (double)(frametime / 16666666D);

        if (!this.isDestroyed) {    
            if (!(this.L && this.R)) {
                if (this.L) {
                    this.moveLeft();
                } else if (this.R) {
                    this.moveRight();
                }
            }
            if (!(this.U && this.D)) {
                if (this.U) {
                    this.moveUp();
                } else if (this.D) {
                    this.moveDown();
                }
            }
            if (this.S) {
                this.shoot(frametime);
            }
            if (!this.S) {
                this.lastShot = 0;
            }
            if (this.B) {
                this.boom(frametime);
            }
            if (this.bomb != null) {
                bombSpeed = (short)(defaultBombSpeed * ((double)frametime / 16666666D));
                this.bomb.setArc(this.bomb.getX() - bombSpeed / 2, 
                                 this.bomb.getY() - bombSpeed / 2, 
                                 this.bomb.getWidth() + bombSpeed, 
                                 this.bomb.getHeight() + bombSpeed, 0, 360, 
                                 Arc2D.PIE);

                if (this.bomb.getWidth() >= panelWidth) {
                    this.bomb = null;
                } else {
                    if (Sprite.areCollidingBomb(this.bomb, enemy)) {
                        System.out.println("colidiu...");
                        enemy.hasCollided(false);
                    }
                }
            }
        } else {
            if (this.isToAnimateDestruction && !this.destroyedAnimationDone) {
                this.destructionAnimationStep = this.defaultDestructionAnimationStep * ((double) frametime / 16_666_666D);
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
                bullet.update(frametime, enemy);
                if (bullet.bulletDestroyed()) {
                    bullet = null;
                    this.bullets[count] = null;
                } else {
                    if (Sprite.areColliding(bullet, enemy)) {
                        enemy.hasCollided(false);
                    }
                }
            }
        }
    }

    /* Move a nave para baixo */
    private void moveDown() {
        double next = (double)(this.positionY + this.speed);
        if (next > this.panelHeight - spriteHeight - 1) {
            next = (double)(this.panelHeight - spriteHeight - 1);
        }
        this.positionY = (short)Math.ceil(next);
    }

    /* Move a nave para cima */
    private void moveUp() {
        double next = (double)(this.positionY - this.speed);
        if (next < 0) {
            next = 0;
        }
        this.positionY = (short)Math.floor(next);
    }

    /* Move a nave para a direita */
    private void moveRight() {
        double next = (double)(this.positionX + this.speed);
        if (next > this.panelWidth - spriteWidth - 1) {
            next = (double)(this.panelWidth - spriteWidth - 1);
        }
        this.positionX = (short)Math.ceil(next);
    }

    /* Move a nave para a esquerda */
    private void moveLeft() {
        double next = (double)(this.positionX - this.speed);
        if (next < 0) {
            next = 0;
        }
        this.positionX = (short)Math.floor(next);
    }

    /* Atira com a nave */
    private void shoot(long frametime) {
        if (this.lastShot == 0) {
            short x = (short)(this.positionX + this.spriteWidth + 2);
            short y = (short)(this.positionY + this.halfSpriteHeight);
            bullets[currentBulletPos++%maxBullets] = new Bullet((short)45, x, y, this.panelWidth, this.panelHeight, true, this.g2d);
            bullets[currentBulletPos++%maxBullets] = new Bullet((short)30, x, y, this.panelWidth, this.panelHeight, true, this.g2d);
            bullets[currentBulletPos++%maxBullets] = new Bullet((short)65, x, y, this.panelWidth, this.panelHeight, true, this.g2d);
        }
        this.lastShot += frametime;
        if (this.lastShot >= 512_000_000) {
            this.lastShot = 0;
        }
    }

    /* Explode uma bomba */
    private void boom(long frametime) {
        if (this.bomb == null && this.numberOfBombs > 0) {
            this.bomb = new Arc2D.Double((this.positionX + this.halfSpriteWidth), 
                                         this.positionY + this.halfSpriteHeight, 
                                         2, 2, 0, 360, Arc2D.PIE);
            this.numberOfBombs--;
        }
    }
    
    /* Getters */
    public Arc2D getBomb() {return (this.bomb);}
}