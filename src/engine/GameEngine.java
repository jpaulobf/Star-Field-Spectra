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
        long lastTime           = System.nanoTime(); // Usado para calcular o delta time no modo de FPS ilimitado
        long now                = 0;
        long elapsed            = 0;
        long wait               = 0;
        long overSleep          = 0;

        if (UNLIMITED_FPS) {
            while (isEngineRunning) {
                now = System.nanoTime();
                elapsed = now - lastTime;
                lastTime = now;

                // Cap delta time to avoid huge jumps (e.g. 0.1s)
                if (elapsed > 100_000_000) elapsed = 100_000_000;

                this.update(elapsed);
                this.draw(elapsed);
                
                // Yield to prevent CPU starvation
                Thread.yield();
            }
        } else {
            while (isEngineRunning) {
                now = System.nanoTime();
                elapsed = now - lastTime;
                lastTime = now;

                this.update(elapsed);
                this.draw(elapsed);

                // Calculate time taken
                long workTime = System.nanoTime() - now;

                // Calculate wait time, compensating for previous over-sleep/lag
                wait = TARGET_FRAMETIME - workTime - overSleep;

                if (wait > 0) {
                    try {
                        // Hybrid Sleep Strategy:
                        // Sleep for (wait - 2ms) to save CPU, then spin-wait for precision
                        long sleepMs = (wait / 1_000_000) - 2;
                        if (sleepMs > 0) {
                            Thread.sleep(sleepMs);
                        }
                        
                        // Busy-wait for the remaining nanoseconds
                        while (System.nanoTime() < now + TARGET_FRAMETIME - overSleep) {
                            // Cede o tempo de CPU para outras threads enquanto espera, para evitar 100% de uso.
                            Thread.yield();
                        }
                        overSleep = 0;
                    } catch (InterruptedException e) {
                        // ignore
                    }
                } else {
                    // We are behind schedule
                    overSleep = -wait;
                    
                    // Frame Skipping: Se estamos atrasados por mais de um quadro completo,
                    // precisamos recuperar o tempo executando a lógica do jogo sem renderizar.
                    while (overSleep >= TARGET_FRAMETIME) {
                        this.update(TARGET_FRAMETIME); // Executa um passo da simulação para recuperar o tempo
                        overSleep -= TARGET_FRAMETIME; // "Paga" a dívida de tempo de um quadro
                    }
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
