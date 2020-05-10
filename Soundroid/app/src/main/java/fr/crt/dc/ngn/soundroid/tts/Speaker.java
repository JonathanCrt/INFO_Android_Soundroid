package fr.crt.dc.ngn.soundroid.tts;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by CRETE JONATHAN on 10/05/2020.
 */
public class Speaker implements TextToSpeech.OnInitListener {

    private TextToSpeech textToSpeech;
    private boolean isReady;

    public Speaker(Context context) {
        this.textToSpeech = new TextToSpeech(context, this);
    }

    /**
     * This method is called when the TTS engine has been initialized.
     * Once we know that the initialization was successful, we set the language of the TTS engine (french)
     * @param status lets us know if the initialization was successful
     *
     */
    @Override
    public void onInit(int status) {
        Log.d("Speaker tts", "onInit");
        if(status == TextToSpeech.SUCCESS) {
            this.textToSpeech.setLanguage(Locale.FRANCE);
            this.isReady = true;
        } else {
            this.isReady = false;
        }
    }

    public void speakText(String textToSpeak) {
        if(this.isReady) {
            Log.d("Speaker tts", "speakText");
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
            this.textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_ADD, map);
        }
    }

    public void pause(int duration) {
        this.textToSpeech.playSilentUtterance(duration, TextToSpeech.QUEUE_ADD, null);
    }

    public void shutdownTTS() {
        this.textToSpeech.shutdown();
    }

}
