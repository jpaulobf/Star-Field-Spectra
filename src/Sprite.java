import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.Graphics2D;

/* 
    Classe base para os sprites
*/
public abstract class Sprite {

    /* Membros */
    protected short positionX                   = 0;
    protected short positionY                   = 0;
    protected short spriteWidth                 = 0;
    protected short spriteHeight                = 0;
    protected short halfSpriteWidth             = 0;
    protected short halfSpriteHeight            = 0;
    protected short angle                       = 0;
    protected short panelWidth                  = 0;
    protected short panelHeight                 = 0;
    protected short speed                       = 0;
    protected boolean isDestroyed               = false;
    protected Graphics2D g2d                    = null;

    protected boolean isToAnimateDestruction    = false;
    protected boolean destroyedAnimationDone    = false;
    protected short destroyAnimationWidth       = 0;
    protected short destroyAnimationHeight      = 0;
    protected short destroyAnimationX           = 0;
    protected short destroyAnimationY           = 0;
    protected byte destructionAnimationStep     = 0;

    /* Construtor */
    public Sprite(short panelWidth, short panelHeight, Graphics2D g2d) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.g2d = g2d;
    }

    /* Getters */
    public int getWidth()       {return (this.spriteWidth);}
    public int getHeight()      {return (this.spriteHeight);}
    public int getPositionX()   {return ((int)this.positionX);}
    public int getPositionY()   {return ((int)this.positionY);}

    /* Set the collision */
    public void hasCollided() {
        this.isDestroyed            = true;
        this.destroyAnimationX      = this.positionX;
        this.destroyAnimationY      = this.positionY;
        this.isToAnimateDestruction = true;
        this.positionX              = -2000;
        this.positionY              = -2000;
    }

    public abstract void draw();
    public abstract void update(long timeStamp, Sprite sprite);
    protected abstract void drawDestroyAnimation();

    /* Métodos estáticos de colisão */
    public static boolean areColliding(Sprite sprite1, Sprite sprite2) {
        if (sprite1 == null || sprite2 == null) return false;
        return ((new Rectangle(sprite1.getPositionX(),
                               sprite1.getPositionY(),
                               sprite1.getWidth(),
                               sprite1.getHeight())).intersects(
                 new Rectangle(sprite2.getPositionX(),
                               sprite2.getPositionY(),
                               sprite2.getWidth(),
                               sprite2.getHeight())));
    }

    public static boolean areCollidingBomb(Sprite sprite1, Sprite sprite2) {
        if (sprite1 == null || sprite2 == null) return false;
        return ((new Arc2D.Double(sprite1.getPositionX(), 
                                  sprite1.getPositionY(),
                                  sprite1.getWidth(),
                                  sprite1.getHeight(), 
                                  0, 360, Arc2D.PIE)).intersects(
                 new Rectangle(sprite2.getPositionX(),
                               sprite2.getPositionY(),
                               sprite2.getWidth(),
                               sprite2.getHeight())));
    }

    public static boolean areCollidingBomb(Arc2D bomb, Sprite sprite2) {
        if (bomb == null || sprite2 == null) return false;
        return (bomb.intersects(new Rectangle(sprite2.getPositionX(),sprite2.getPositionY(),sprite2.getWidth(),sprite2.getHeight())));
    }

    public boolean isToAnimateDestruction() {
        return isToAnimateDestruction;
    }

    public boolean isDestroyedAnimationDone() {
        return destroyedAnimationDone;
    }
}
