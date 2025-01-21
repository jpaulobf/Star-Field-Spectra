package game;

import java.awt.Graphics2D;
import java.awt.Color;

/* Classe para desenho de uma estrela no Canvas */
public class Star {
    
    private int inclinationAngle    = -20;      /* ângulo de inclinação */
    private int rays                = 0;        /* raios da estrela - somente 0 ou pares */
    private boolean hasRing         = true;    /* possui anel ao redor */
    private double radiusX          = 1d;       /* raio em pixels */
    private double radiusY          = 1d;       /* raio em pixels */
    private double alpha            = 100d;       /* transparência de 0 a 100 */
    private Graphics2D g2d          = null;
    private int positionX           = 0;
    private int positionY           = 0;
    private short tX                = 0;
    private short tY                = 0;
    private double contrast         = .1;

    /* Construtor */
    public Star(Graphics2D g2d, int positionX, int positionY, double radiusX, double radiusY, int rays) {
        this.g2d        = g2d;
        this.positionX  = positionX;
        this.positionY  = positionY;
        this.radiusX    = radiusX;
        this.radiusY    = radiusY;
        this.rays       = rays;
        this.tX         = (short)(this.positionX + this.radiusX);
        this.tY         = (short)(this.positionY + this.radiusY);
    }

    /* Atualiza a estrela */
    public void update() {
        if (this.alpha + contrast > 130) {
            contrast = -.5;
        } else if (this.alpha + contrast < 100) {
            contrast = 2;
        }

        this.alpha += contrast;
    }

    /* desenha a estrela no canvas */
    public void draw() {
        if (this.g2d != null) {

            //cor branca com a transparência desejada
            this.g2d.setColor(new Color(255, 255, 255, (int)this.alpha));

            if (this.rays == 0) {
                
                //translate - center of the bullet
                this.g2d.translate(tX, tY);

                //rotate
                this.g2d.rotate(Math.toRadians(this.inclinationAngle));

                //desenha o círculo
                this.g2d.fillOval(0 - (int)this.radiusX,
                                  0 - (int)this.radiusY,
                                  (int)this.radiusX * 2, 
                                  (int)this.radiusY * 2);

                //cor branca com a transparência desejada
                this.g2d.setColor(Color.LIGHT_GRAY);

                //desenha o anel ao redor da estrela (TODO: melhorar...)
                if (this.hasRing) {
                    //desenha o círculo
                    this.g2d.drawArc(0 - (int)this.radiusX - 6,
                                     0 - (int)this.radiusY + 2,
                                    (int)this.radiusX * 2 + 12, 
                                    (int)this.radiusY * 2 - 5, 120, 300);
                    
                    
                    //desenha o círculo
                    this.g2d.drawArc(0 - (int)this.radiusX - 4,
                                     0 - (int)this.radiusY + 2,
                                    (int)this.radiusX * 2 + 8, 
                                    (int)this.radiusY * 2 - 5, 120, 300);
                }

                //undo rotate
                this.g2d.rotate(Math.toRadians(-this.inclinationAngle));

                //undo translate
                this.g2d.translate(-tX, -tY);
                
            } else {

                int angle = 360 / (this.rays * 2);

                for (short i = 0; i < this.rays; i++) {

                    //translate - center of the bullet
                    this.g2d.translate(tX, tY);

                    //rotate
                    this.g2d.rotate(Math.toRadians(i * angle));

                    this.g2d.drawLine(0 - (int)this.radiusX * 2, 0, (int)this.radiusX * 2, 0);

                    //undo rotate
                    this.g2d.rotate(Math.toRadians(-i * angle));

                    //undo translate
                    this.g2d.translate(-tX, -tY);
                }
            }
        }
    }
}
