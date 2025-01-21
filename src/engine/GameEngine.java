package engine;
/* Classe game-engine */

import game.StarFieldSpectra;
import interfaces.Game;

public class GameEngine implements Runnable {

    private boolean isEngineRunning     = true;
    private Game game                   = null;
    private volatile boolean isToDraw   = false;
    private volatile boolean isToUpdate = false;
    private long FPS                    = (long)(1_000_000_000 / 60);

    /* Método de update, só executa quando a flag permite */
    public void update(long timeStamp) {
        if (this.isToUpdate) {
            this.game.update(timeStamp);
            this.isToUpdate = false;
        }
    }

    /* Método de desenho, só executa quando a flag permite */
    public void draw() {
        if (this.isToDraw) {
            game.draw();
            this.isToDraw = false;
        }
    }

    /* Método de execução da thread */
    public void run() {
        this.game = new StarFieldSpectra(FPS);
        long timeStamp = System.nanoTime();
        long tempPeriod = 0;
        int accumulator = 0;

        while (isEngineRunning) {
            //Chama o comando de atualização
            this.update(timeStamp);

            //Calcula o tempo de execução e acumula a diferença entre um update e outro
            long afterExecution = System.nanoTime();
            long diff = ((afterExecution - timeStamp));
            accumulator += diff;
            tempPeriod = afterExecution;

            //Se o acumulado for maior que o tempo para cada frame, permite novo update e desenho
            if (accumulator >= FPS) {
                this.isToDraw   = true;
                this.isToUpdate = true;
                accumulator     = 0;
            }

            //Caso a diferença entre uma execução e outra tenha ultrapassado o tempo do quadro, atualiza para recuperar o timing
            while (diff > FPS) {
                this.isToUpdate = true;
                this.update(tempPeriod);
                tempPeriod = System.nanoTime();
                diff -= FPS;
            }

            //chama o comando de desenho
            this.draw();

            //atualiza o timestamp com o tempo pós execução
            timeStamp = afterExecution;
        }
    }
}