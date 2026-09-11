package engine;

import game.StarFieldSpectra;
import interfaces.Game;

/**
 * Class of GameEngine
 */
public class GameEngine implements Runnable {

    private boolean isEngineRunning     = true;
    private long FPS240                 = (long)(1_000_000_000 / 240);
    private long FPS120                 = (long)(1_000_000_000 / 120);
    private long FPS90                  = (long)(1_000_000_000 / 90);
    private long FPS60                  = (long)(1_000_000_000 / 60);
    private long FPS30                  = (long)(1_000_000_000 / 30);
    private long TARGET_FRAMETIME       = FPS60;
    private boolean UNLIMITED_FPS       = false;
    private final int MAX_UPDATES_PER_FRAME = 5;
    private Game game                   = null;
    
    /*
        WTMD: constructor
                receives the target FPS (0, 30, 60, 120, 240) and starts the engine
    */
    public GameEngine(int targetFPS) {

        this.UNLIMITED_FPS = false;
        switch(targetFPS) {
            case 30:
                this.TARGET_FRAMETIME = FPS30;
                break;
            case 60:
                this.TARGET_FRAMETIME = FPS60;
                break;
            case 90:
                this.TARGET_FRAMETIME = FPS90;
                break;
            case 120:
                this.TARGET_FRAMETIME = FPS120;
                break;
            case 240:
                this.TARGET_FRAMETIME = FPS240;
                break;
            case 0:
                this.UNLIMITED_FPS = true;
                break;
            default:
                this.TARGET_FRAMETIME = (long)(1_000_000_000 / targetFPS);
                break;
        }

        this.game = new StarFieldSpectra(targetFPS);
    }
    
    /* Método de execução da thread */
    public void run() {
        long previousTime = System.nanoTime();
        long accumulator = 0;

        while (isEngineRunning && !Thread.currentThread().isInterrupted()) {
            long frameStart = System.nanoTime();
            long elapsed = frameStart - previousTime;
            previousTime = frameStart;

            if (UNLIMITED_FPS) {
                elapsed = Math.min(elapsed, 100_000_000L);
                this.update(elapsed);
                this.draw(elapsed);
                Thread.yield();
                continue;
            }

            accumulator += Math.min(elapsed, 250_000_000L);
            int updates = 0;
            while (accumulator >= TARGET_FRAMETIME && updates < MAX_UPDATES_PER_FRAME) {
                this.update(TARGET_FRAMETIME);
                accumulator -= TARGET_FRAMETIME;
                updates++;
            }

            // Descarta atraso excessivo para evitar o efeito de "espiral da morte".
            if (updates == MAX_UPDATES_PER_FRAME && accumulator >= TARGET_FRAMETIME) {
                accumulator = 0;
            }

            this.draw(TARGET_FRAMETIME);
            long remaining = TARGET_FRAMETIME - (System.nanoTime() - frameStart);
            if (remaining > 0) {
                try {
                    long sleepMillis = remaining / 1_000_000L;
                    int sleepNanos = (int)(remaining % 1_000_000L);
                    Thread.sleep(sleepMillis, sleepNanos);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Atualiza a lógica do jogo.
     * 
     * Este método é chamado a cada frame para processar a lógica do jogo, como
     * movimentação de personagens, detecção de colisões e outros cálculos necessários.
     * 
     * @param frametime O tempo de duração do frame atual, em nanossegundos.
     */
    public void update(long frametime) {
        this.game.update(frametime);
    }

    /**
     * Renderiza os gráficos do jogo.
     * 
     * Este método é chamado a cada frame para desenhar os elementos do jogo na tela,
     * como personagens, cenários e efeitos visuais.
     * 
     * @param frametime O tempo de duração do frame atual, em nanossegundos.
     */
    public void draw(long frametime) {
        this.game.draw(frametime);
    }
}
