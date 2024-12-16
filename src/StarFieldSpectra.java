import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.image.VolatileImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import Interfaces.Game;

import java.awt.event.KeyAdapter;

/* 
    Classe responsável pelo jogo
*/
public class StarFieldSpectra extends JFrame implements ControllerListener, Game {

    private static final long serialVersionUID  = 297111147922924467L;
    private int windowWidth                     = 1280 + 16;
    private int windowHeight                    = 480 + 14;
    private JPanel panel                        = null;
    private GraphicsEnvironment ge              = null;
    private GraphicsDevice dsd                   = null;
    private Graphics2D g2d                      = null;
    private VolatileImage bufferImage           = null;
    private Spaceship spaceship                 = null;
    private Background background               = null;
    private Enemy enemy                         = null;
    private TController tcontroller             = null;
    private Audio backgroundMusic               = null;
    private Star star                           = null;

    /* 
        Construtor
    */
    public StarFieldSpectra(long FPS) {
        //dados do frame (window)
        this.setTitle("Star Field Spectra");
        this.setSize(windowWidth, windowHeight);
        this.setPreferredSize(new Dimension(windowWidth, windowHeight));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //dados do panel
        this.panel = new JPanel();
        this.panel.setSize(windowWidth, windowHeight);
        this.panel.setVisible(true);
        this.panel.setBackground(Color.WHITE);
        this.panel.setVisible(true);
        this.panel.setOpaque(false);
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.requestFocus();

        //cria o buffer
        this.ge             = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.dsd            = ge.getDefaultScreenDevice();
        this.bufferImage    = dsd.getDefaultConfiguration().createCompatibleVolatileImage(windowWidth, windowHeight);
        this.g2d            = (Graphics2D)bufferImage.getGraphics();

        //cria o bg
        this.background     = new Background(g2d);

        //objetos do jogo
        byte bulletspersecs = 4;
        this.spaceship      = new Spaceship((short)this.panel.getWidth(), (short)this.panel.getHeight(), this.g2d);
        this.enemy          = new Enemy((short)this.panel.getWidth(), (short)this.panel.getHeight(), this.g2d, bulletspersecs);
        this.star           = new Star(g2d, 100, 100, 5, 5, 0);

        //thread para o controle (quando presente)
        this.tcontroller    = new TController(FPS, this);
        Thread thread       = new Thread(tcontroller, "controller");
        if (thread != null && tcontroller.hasAnyConnectedController()) {
            thread.start();
        }

        //variável para música de background
        this.backgroundMusic = new Audio("audio/1.snd");
        if (this.backgroundMusic != null && this.backgroundMusic.isReady()) {
            this.backgroundMusic.play();
        }

        //adiciona o keylistener
        this.addKeyListener(new KeyAdapter() {
            /* Key pressed */
            public void keyPressed(KeyEvent e) {
                if (tcontroller == null || !tcontroller.hasAnyConnectedController()) {
                    if (e.getKeyCode() == 39) spaceship.L = true;
                    if (e.getKeyCode() == 37) spaceship.R = true;
                    if (e.getKeyCode() == 38) spaceship.U = true;
                    if (e.getKeyCode() == 40) spaceship.D = true;
                    if (e.getKeyCode() == 32) spaceship.S = true;
                    if (e.getKeyCode() == 66) spaceship.B = true;
                }
            }
            /* Key released */
            public void keyReleased(KeyEvent e) {
                if (tcontroller == null || !tcontroller.hasAnyConnectedController()) {
                    if (e.getKeyCode() == 39) spaceship.L = false;
                    if (e.getKeyCode() == 37) spaceship.R = false;
                    if (e.getKeyCode() == 38) spaceship.U = false;
                    if (e.getKeyCode() == 40) spaceship.D = false;
                    if (e.getKeyCode() == 32) spaceship.S = false;
                    if (e.getKeyCode() == 66) spaceship.B = false;
                }
            }
        });
    }

    /* 
        Método de observação
    */
    public void notify(boolean U, boolean D, boolean L, boolean R, boolean S, boolean B) {
        this.spaceship.L = L;
        this.spaceship.R = R;
        this.spaceship.U = U;
        this.spaceship.D = D;
        this.spaceship.S = S;
        this.spaceship.B = B;
    }

    /* 
        Atualiza o game
    */
    public void update(long timeStamp) {

        //Atualiza o background
        this.background.update(timeStamp);

        //Atualiza a spaceship
        this.spaceship.update(timeStamp, this.enemy);

        //Atualiza o inimigo
        this.enemy.update(timeStamp, this.spaceship);

        //Verifica a colisão entre a nave e o inimigo
        if (Sprite.areColliding(this.spaceship, this.enemy)) {
            this.enemy.hasCollided();
            this.spaceship.hasCollided();
        }

        this.star.update();
    }

    /* 
        Desenha o game
    */
    public void draw() {
        //Limpa a janela
        g2d.setBackground(Color.black);
        g2d.clearRect(0, 0, windowWidth, windowHeight);

        //Desenha a spaceship
        this.background.draw();
        
        //Desenha um inimigo
        this.star.draw();

        //Desenha a spaceship
        this.spaceship.draw();

        //Desenha um inimigo
        this.enemy.draw();

        //Copia o buffer para o panel
        this.panel.getGraphics().drawImage(this.bufferImage, 0, 0, this);
    }

    public static void main(String[] args) throws Exception {
        Thread thread1 = new Thread(new GameEngine(), "engine");
        thread1.start();
    }
}