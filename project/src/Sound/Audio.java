package Sound;

import java.io.InputStream;
import java.io.FileInputStream;

import sun.audio.AudioPlayer;


public class Audio {
    private InputStream inputStream = null;
    private String file = "";

    public Audio(String file) {
        this.file = file;
    }

    public Audio() {

    }

    public void play() {
        try {
            inputStream = new FileInputStream(file);
            AudioPlayer.player.start(inputStream);
        } catch (Exception exception) {
            //System.out.println(exception);
        }
    }

    /*public static void BGM() {
        try {
            InputStream in = new FileInputStream("sounds/bgm.wav");
            AudioStream as = new AudioStream(in);
            AudioData data = as.getData();
            ContinuousAudioDataStream bgm = new ContinuousAudioDataStream(data);
            AudioPlayer.player.start(bgm);
        } catch (Exception exception) {
            System.out.println(exception);
        }
    }*/
}
