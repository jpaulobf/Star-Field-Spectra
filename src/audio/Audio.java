package audio;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
  
public class Audio {
  
    private long currentFrame                   = 0;
    private Clip clip                           = null;
    private String status                       = null;
    private AudioInputStream audioInputStream   = null;
    private static String filePath              = null;
    private boolean ready                       = false;
  
    public Audio(String filePath) {
        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
        } catch (Exception e) {
            e.printStackTrace();
            status = "Impossíver criar o Input Stream do arquivo de audio informado. Erro: " + e.getMessage();
        }

        if (audioInputStream != null) {
            try {
                clip = AudioSystem.getClip();    
            } catch (Exception e) {
                status = "Impossíver recuperar o clip do áudio. Erro: " + e.getMessage();
            }

            try {
                clip.open(audioInputStream);

                if (clip != null) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }

                this.ready = true;
            } catch (Exception e) {
                status = "Impossíver abrir o arquivo de áudio. Erro: " + e.getMessage();
            }
        }
    }
      
    // Method to play the audio
    public void play() {
        //start the clip
        clip.start();
        status = "play";
    }
      
    // Method to pause the audio
    public void pause() {
        if (status.equals("paused")) {
            return;
        }
        this.currentFrame = this.clip.getMicrosecondPosition();
        clip.stop();
        status = "paused";
    }
      
    public void resumeAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (status.equals("play")) {
            return;
        }
        clip.close();
        resetAudioStream();
        clip.setMicrosecondPosition(currentFrame);
        this.play();
    }
      
    public void restart() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        clip.stop();
        clip.close();
        resetAudioStream();
        currentFrame = 0L;
        clip.setMicrosecondPosition(0);
        this.play();
    }
      
    // Method to stop the audio
    public void stop() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        currentFrame = 0L;
        clip.stop();
        clip.close();
    }
      
    // Method to jump over a specific part
    public void jump(long c) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (c > 0 && c < clip.getMicrosecondLength()) {
            clip.stop();
            clip.close();
            resetAudioStream();
            currentFrame = c;
            clip.setMicrosecondPosition(c);
            this.play();
        }
    }
      
    // Method to reset audio stream
    public void resetAudioStream() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        audioInputStream = AudioSystem.getAudioInputStream(
        new File(filePath).getAbsoluteFile());
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public boolean isReady() {
        return ready;
    }
}