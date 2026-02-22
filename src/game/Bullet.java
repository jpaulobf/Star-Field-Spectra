package game;

import java.awt.Color;
import java.awt.Graphics2D;

/* Class Bullet */
public class Bullet extends Sprite {

    /* Membros */
    private boolean isMeShooting    = true;
    private final byte ME           = 1;
    private final byte ENEMY        = -1;
    private byte direction          = ME;

    /* Construtor */
    public Bullet(short angle, 
                  short positionX, 
                  short positionY, 
                  short panelWidth, 
                  short panelHeight, 
                  boolean isMeShooting, 
                  Graphics2D g2d) {
        super(panelWidth, panelHeight, g2d);
        this.angle                      = (short)(angle - 45);
        this.positionX                  = positionX;
        this.positionY                  = positionY;
        this.isMeShooting               = isMeShooting;
        this.spriteWidth                = 10;
        this.spriteHeight               = 4;
        this.defaultSpeed               = 8;
        this.speed                      = 0;
        this.halfSpriteWidth            = (short)(this.spriteWidth / 2);
        this.halfSpriteHeight           = (short)(this.spriteHeight / 2);
        this.direction                  = this.isMeShooting?ME:ENEMY;
        this.defaultDestructionAnimationStep = 2;
        this.destructionAnimationStep = this.defaultDestructionAnimationStep;
    }

    /* Verifica se a bala se destruiu (saiu da tela ou atingiu um inimigo ou elemento do cenário [foreground]) */
    public boolean bulletDestroyed() {
        if (this.positionX > panelWidth || this.positionY > panelHeight || 
            this.positionX < 0 || this.positionY < 0 ) {
            this.isDestroyed            = true;
            this.positionX              = -100;
            this.positionY              = -100;
        }
        return (this.isDestroyed);
    }

    /* Desenha o bullet */
    public void draw() {
        this.g2d.setColor(Color.red);
        
        if (!this.isDestroyed) {
            if (this.angle != 0) {
                short tX = (short)(this.positionX + this.halfSpriteWidth);
                short tY = (short)(this.positionY + this.halfSpriteHeight);

                //translate - center of the bullet
                this.g2d.translate(tX, tY);

                //rotate
                this.g2d.rotate(Math.toRadians(this.angle));

                //draw the bullets
                this.g2d.drawRect(0, 0, this.spriteWidth, this.spriteHeight);
            
                //undo rotate
                this.g2d.rotate(Math.toRadians(-this.angle));

                //undo translate
                this.g2d.translate(-tX, -tY);
            } else {
                this.g2d.drawRect((int)this.positionX, (int)this.positionY, this.spriteWidth, this.spriteHeight);
            }
        } else {
            if (!this.destroyedAnimationDone) {
                this.drawDestroyAnimation();
            }
        }
    }

    /* Atualiza */
    public void update(long frametime, Sprite sprite) {
        this.speed = (short)(this.defaultSpeed * (double)(frametime / 16_666_666D));
        if (!this.isDestroyed) {
            this.positionX += (speed * direction);    
            if (this.angle < 0) {
                this.positionY -= (this.speed * 0.25);
            } else if (this.angle > 0) {
                this.positionY += (this.speed * 0.25);
            }
        } else {
            if (this.isToAnimateDestruction && !this.destroyedAnimationDone) {
                this.destructionAnimationStep = this.defaultDestructionAnimationStep * ((double) frametime / 16_666_666D);
                this.destroyAnimationWidth  += this.destructionAnimationStep;
                this.destroyAnimationHeight += this.destructionAnimationStep;
                if (this.destroyAnimationWidth >= 20) {
                    this.destroyedAnimationDone = true;
                }
            }
        }
    }

    /* desenha a animação de destruição */
    protected void drawDestroyAnimation() {
        this.g2d.setColor(Color.red);
        this.g2d.drawOval((int)this.destroyAnimationX + this.halfSpriteWidth, 
                          (int)this.destroyAnimationY + this.halfSpriteHeight, 
                          (int)this.destroyAnimationWidth, 
                          (int)this.destroyAnimationHeight);
        this.destroyAnimationX -= destructionAnimationStep / 2;
        this.destroyAnimationY -= destructionAnimationStep / 2;
    }
}