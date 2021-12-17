import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/* 
    Classe responsável pelo background
*/
public class Background {

    private VolatileImage bgBufferImage = null;
    private VolatileImage bgFrameImage  = null;
    private Graphics2D g2d              = null;
    private double bgPosX               = 0;
    private double bgPosY               = 0;
    private final int maxColumns        = 30;
    private final int maxBefLastFrame   = 20;
    private final int columnWidth       = 64;
    private int actualColumn            = 1;
    private int actualColumn2           = 0;
    private final int windowWidth       = 1280;

    public Background(Graphics2D g2d) {
        this.g2d = g2d;
        
        //Ler o arquivo de tiles e popular a matriz
        //E cria a volatile image
        BufferedImage bimage    = null;
        Graphics2D bufferG2D    = null;
        GraphicsEnvironment ge  = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd       = ge.getDefaultScreenDevice();

        try {
            bimage                  = ImageIO.read(new File("img\\1.gif"));
            this.bgBufferImage      = gd.getDefaultConfiguration().createCompatibleVolatileImage(bimage.getWidth(), bimage.getHeight());
            bufferG2D               = this.bgBufferImage.createGraphics();
            bufferG2D.drawImage(bimage, null, 0, 0); 
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bufferG2D.dispose(); 
        }

        if (this.bgBufferImage != null) {
            try {
                //Copia a primeira etapa do background para o frame
                this.bgFrameImage = gd.getDefaultConfiguration().createCompatibleVolatileImage(this.windowWidth + (columnWidth * 2), this.bgBufferImage.getHeight());
                bufferG2D = this.bgFrameImage.createGraphics();
                bufferG2D.drawImage(this.bgBufferImage, 0, 0, this.windowWidth + (columnWidth * 2), this.bgBufferImage.getHeight(), //destine
                                    0, 0, this.windowWidth + (columnWidth * 2), this.bgBufferImage.getHeight(), //source
                                    null);
            } finally {
                bufferG2D.dispose();
                bufferG2D = null;
            }
        }
    }

    /* 
        Atualiza  o background
    */
    public void update(long timeStamp) {
        
        this.bgPosX -= .25;

        if (this.bgPosX <= -(columnWidth)) {
            
            //copia a imagem começando na próxima coluna
            Graphics2D bufferG2D = null;
            try {
                //calcula a nova posição de início da cópia (na imagem fonte)
                int iPosX = this.actualColumn * this.columnWidth;
                int iPox2 = this.actualColumn2 * this.columnWidth;

                //copia o pedaço descontando a coluna que se 'escondeu'
                bufferG2D = this.bgFrameImage.createGraphics();
                bufferG2D.drawImage(this.bgBufferImage, 
                                    0, 0, this.windowWidth + (columnWidth * 2), this.bgBufferImage.getHeight(), //destine
                                    iPosX, 0, iPosX + this.windowWidth + (columnWidth * 2), this.bgBufferImage.getHeight(), //source
                                    null);
                
               if (this.actualColumn >= this.maxBefLastFrame) {
                    bufferG2D.drawImage(this.bgBufferImage, 
                                        this.windowWidth - iPox2, 0, this.windowWidth + columnWidth, this.bgBufferImage.getHeight(), //destine
                                        0, 0, iPox2 + columnWidth, this.bgBufferImage.getHeight(), //source
                                        null);
                    ++this.actualColumn2;
                    if (this.actualColumn2 == 10) {
                        this.actualColumn2 = 0;
                    }
                }
                
                //atualiza a coluna atual
                this.actualColumn = (++this.actualColumn)%maxColumns;
            } finally {
                bufferG2D.dispose();
                bufferG2D = null;
            }
            
            //reseta a posição de bgPosX
            this.bgPosX = 0;
        }
    }

    /* 
        Desenha o background
    */
    public void draw() {
        //Copia o buffer para o panel
        this.g2d.drawImage(this.bgFrameImage, (int)this.bgPosX, (int)this.bgPosY, null);
    }
}