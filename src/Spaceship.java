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
    private byte bombSpeed              = 8;
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
        this.speed                      = 3;
        this.positionY                  = (short)((this.panelHeight / 2) - (this.spriteHeight / 2));
        this.halfSpriteWidth            = (short)(this.spriteWidth / 2);
        this.halfSpriteHeight           = (short)(this.spriteHeight / 2);
        this.destructionAnimationStep   = 2;
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
    public void update(long timeStamp, Sprite enemy) {
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
                this.shoot(timeStamp);
            }
            if (this.B) {
                this.boom(timeStamp);
            }
            if (this.bomb != null) {
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
                        enemy.hasCollided();
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
                bullet.update(timeStamp, enemy);
                if (bullet.bulletDestroyed()) {
                    bullet = null;
                    this.bullets[count] = null;
                } else {
                    if (Sprite.areColliding(bullet, enemy)) {
                        enemy.hasCollided();
                    }
                }
            }
        }
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

    /* Move a nave para a direita */
    private void moveRight() {
        short next = (short)(this.positionX - this.speed);
        if (next < 0) {
            next = 0;
        }
        this.positionX = next;
    }

    /* Move a nave para a esquerda */
    private void moveLeft() {
        short next = (short)(this.positionX + this.speed);
        if (next > this.panelWidth - spriteWidth - 1) {
            next = (short)(this.panelWidth - spriteWidth - 1);
        }
        this.positionX = next;
    }

    /* Atira com a nave */
    private void shoot(long timeStamp) {
        if ((timeStamp - this.lastShot) > 300_000_000) {
            short x = (short)(this.positionX + this.spriteWidth + 2);
            short y = (short)(this.positionY + this.halfSpriteHeight);
            bullets[currentBulletPos++%maxBullets] = new Bullet((short)90, x, y, this.panelWidth, this.panelHeight, true, this.g2d);
            bullets[currentBulletPos++%maxBullets] = new Bullet((short)70, x, y, this.panelWidth, this.panelHeight, true, this.g2d);
            bullets[currentBulletPos++%maxBullets] = new Bullet((short)110, x, y, this.panelWidth, this.panelHeight, true, this.g2d);
            this.lastShot = timeStamp;
        }
    }

    /* Explode uma bomba */
    private void boom(long timeStamp) {
        if (this.bomb == null && this.numberOfBombs > 0) {
            this.bomb = new Arc2D.Double(this.positionX + this.halfSpriteWidth, 
                                         this.positionY + this.halfSpriteHeight, 
                                         2, 2, 0, 360, Arc2D.PIE);
            this.numberOfBombs--;
        }
    }
    
    /* Getters */
    public Arc2D getBomb() {return (this.bomb);}
}