package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.image.VolatileImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import engine.GameEngine;
import interfaces.Game;
import interfaces.TController;
import listener.ControllerListener;
import audio.Audio;
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
    private GraphicsDevice dsd                  = null;
    private Graphics2D g2d                      = null;
    private VolatileImage bufferImage           = null;
    private Spaceship spaceship                 = null;
    private Background background               = null;
    private Enemy enemy                         = null;
    private TController tcontroller             = null;
    private Audio backgroundMusic               = null;
    private Star star                           = null;
    private boolean showFPS                     = true;
    
    //FPS calculation
    private long[] fpsHistory                   = new long[20];
    private int fpsIndex                        = 0;
    private int fpsCount                        = 0;
    private long fpsTotalTime                   = 0;
    private Font fpsFont;

    /* 
        Construtor
    */
    public StarFieldSpectra(long FPS) {
        //dados do frame (window)
        this.setTitle("Star Field Spectra");
        this.setSize(windowWidth, windowHeight);
        this.setPreferredSize(new Dimension(windowWidth, windowHeight));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Pré-carrega a fonte para a renderização do FPS
        this.fpsFont = new Font("Arial", Font.PLAIN, 12);
        
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
        //this.tcontroller    = new TController(FPS, this);
        //Thread thread       = new Thread(tcontroller, "controller");
        //if (thread != null && tcontroller.hasAnyConnectedController() && false) {
        //   thread.start();
        //}

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
                    if (e.getKeyCode() == 37) spaceship.L = true;
                    if (e.getKeyCode() == 39) spaceship.R = true;
                    if (e.getKeyCode() == 38) spaceship.U = true;
                    if (e.getKeyCode() == 40) spaceship.D = true;
                    if (e.getKeyCode() == 32) spaceship.S = true;
                    if (e.getKeyCode() == 66) spaceship.B = true;
                }
            }
            /* Key released */
            public void keyReleased(KeyEvent e) {
                if (tcontroller == null || !tcontroller.hasAnyConnectedController()) {
                    if (e.getKeyCode() == 37) spaceship.L = false;
                    if (e.getKeyCode() == 39) spaceship.R = false;
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
    public void update(long frametime) {

        //System.out.println("Frame time: " + frametime);

        //Atualiza o background
        this.background.update(frametime);

        //Atualiza a spaceship
        this.spaceship.update(frametime, this.enemy);

        //Atualiza o inimigo
        this.enemy.update(frametime, this.spaceship);

        //Verifica a colisão entre a nave e o inimigo
        if (Sprite.areColliding(this.spaceship, this.enemy)) {
            this.enemy.hasCollided(true);
            this.spaceship.hasCollided(true);
        }

        this.star.update(frametime);
    }

    /* 
        Desenha o game
    */
    public void draw(long frametime) {
        //Limpa a janela
        g2d.setBackground(Color.black);
        g2d.clearRect(0, 0, windowWidth, windowHeight);

        //Desenha a spaceship
        this.background.draw();
        
        //Desenha uma estrela
        this.star.draw();

        //Desenha a spaceship
        this.spaceship.draw();

        //Desenha um inimigo
        this.enemy.draw();

        //render the fps counter
        this.renderFPSLayer(frametime);

        //Copia o buffer para o panel
        this.panel.getGraphics().drawImage(this.bufferImage, 0, 0, this);
    }

    /**
     * Show FPS Layer
     * @param frametime
     */
    private void renderFPSLayer(long frametime) {
        //verify if the user want to show the FPS
        if (this.showFPS) {
            // Se o histórico estiver cheio, subtrai o valor mais antigo que será substituído.
            if (this.fpsCount == this.fpsHistory.length) {
                this.fpsTotalTime -= this.fpsHistory[this.fpsIndex];
            }

            // Adiciona o novo frametime ao total e atualiza o histórico.
            this.fpsTotalTime += frametime;
            this.fpsHistory[this.fpsIndex] = frametime;

            this.fpsIndex = (this.fpsIndex + 1) % this.fpsHistory.length;
            if (this.fpsCount < this.fpsHistory.length) {
                this.fpsCount++;
            }

            double average = (this.fpsCount > 0) ? (double)this.fpsTotalTime / this.fpsCount : frametime;

            this.g2d.setColor(Color.RED);
            this.g2d.setFont(this.fpsFont);
            this.g2d.drawString("fps: " + (int)(1_000_000_000D / average), windowWidth - 70, windowHeight - 50);
        }
    }

    public static void main(String[] args) throws Exception {
        Thread thread1 = new Thread(new GameEngine(400), "engine");
        thread1.start();
    }
}