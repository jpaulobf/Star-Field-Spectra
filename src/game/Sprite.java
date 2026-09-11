package game;

import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.Graphics2D;
import java.awt.Color;

/* 
    Classe base para os sprites
*/
public abstract class Sprite {

    /* Membros */
    protected double positionX                  = 0;
    protected double positionY                  = 0;
    protected short spriteWidth                 = 0;
    protected short spriteHeight                = 0;
    protected short halfSpriteWidth             = 0;
    protected short halfSpriteHeight            = 0;
    protected short angle                       = 0;
    protected short panelWidth                  = 0;
    protected short panelHeight                 = 0;
    protected double speed                      = 0;
    protected double defaultSpeed               = 0;
    protected double velocity                   = 1D;
    protected boolean isDestroyed               = false;
    protected Graphics2D g2d                    = null;

    protected boolean isToAnimateDestruction    = false;
    protected boolean destroyedAnimationDone    = false;
    protected double destroyAnimationWidth      = 0;
    protected double destroyAnimationHeight     = 0;
    protected double destroyAnimationX          = 0;
    protected double destroyAnimationY          = 0;
    protected double destructionAnimationStep   = 0;
    protected double defaultDestructionAnimationStep = 2;
    private static final int DESTRUCTION_PARTICLE_COUNT = 22;
    private final double[] particleX = new double[DESTRUCTION_PARTICLE_COUNT];
    private final double[] particleY = new double[DESTRUCTION_PARTICLE_COUNT];
    private final double[] particleVelocityX = new double[DESTRUCTION_PARTICLE_COUNT];
    private final double[] particleVelocityY = new double[DESTRUCTION_PARTICLE_COUNT];
    private final double[] particleSize = new double[DESTRUCTION_PARTICLE_COUNT];
    private final int[] particleLife = new int[DESTRUCTION_PARTICLE_COUNT];

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
    public void hasCollided(boolean keepPosition) {
        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed            = true;
        this.destroyAnimationX      = this.positionX;
        this.destroyAnimationY      = this.positionY;
        this.destroyAnimationWidth  = Math.max(4, this.spriteWidth / 4);
        this.destroyAnimationHeight = Math.max(4, this.spriteHeight / 4);
        this.isToAnimateDestruction = true;
        this.createDestructionParticles();
        if (!keepPosition) {
            this.positionX              = -2000;
            this.positionY              = -2000;
        }
    }

    private void createDestructionParticles() {
        double centerX = this.destroyAnimationX + this.halfSpriteWidth;
        double centerY = this.destroyAnimationY + this.halfSpriteHeight;
        for (int count = 0; count < DESTRUCTION_PARTICLE_COUNT; count++) {
            double angle = (Math.PI * 2D * count) / DESTRUCTION_PARTICLE_COUNT;
            double speed = 0.8D + (count % 5) * 0.35D;
            this.particleX[count] = centerX;
            this.particleY[count] = centerY;
            this.particleVelocityX[count] = Math.cos(angle) * speed;
            this.particleVelocityY[count] = Math.sin(angle) * speed;
            this.particleSize[count] = 2D + (count % 3);
            this.particleLife[count] = 24 + (count % 8);
        }
    }

    protected void drawDestructionParticles() {
        boolean particlesAlive = false;
        double frameScale = this.destructionAnimationStep / this.defaultDestructionAnimationStep;
        if (frameScale <= 0) {
            frameScale = 1D;
        }

        for (int count = 0; count < DESTRUCTION_PARTICLE_COUNT; count++) {
            if (this.particleLife[count] <= 0) {
                continue;
            }
            particlesAlive = true;
            int alpha = Math.min(255, this.particleLife[count] * 10);
            this.g2d.setColor(new Color(255, count % 3 == 0 ? 220 : 100, 35, alpha));
            this.g2d.fillOval((int)this.particleX[count], (int)this.particleY[count],
                    (int)this.particleSize[count], (int)this.particleSize[count]);
            this.particleX[count] += this.particleVelocityX[count] * frameScale;
            this.particleY[count] += this.particleVelocityY[count] * frameScale;
            this.particleVelocityY[count] += 0.04D * frameScale;
            this.particleLife[count]--;
        }

        if (!particlesAlive) {
            this.destroyedAnimationDone = true;
        }
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
