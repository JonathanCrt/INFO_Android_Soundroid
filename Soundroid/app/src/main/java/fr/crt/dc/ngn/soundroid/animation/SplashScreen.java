package fr.crt.dc.ngn.soundroid.animation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import fr.crt.dc.ngn.soundroid.MainActivity;
import fr.crt.dc.ngn.soundroid.R;

/**
 * Created by CRETE JONATHAN on 02/04/2020.
 */
public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Montre le splash screen pendant 2 secondes.
        int SPLASH_TIME_OUT = 1000;
        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(i);
            finish();
        }, SPLASH_TIME_OUT);
    }
}
