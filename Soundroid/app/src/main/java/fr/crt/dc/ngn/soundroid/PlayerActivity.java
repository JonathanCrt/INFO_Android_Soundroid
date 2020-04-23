package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;
import fr.crt.dc.ngn.soundroid.controller.PlayerController;

import android.os.Bundle;

public class PlayerActivity extends AppCompatActivity {

    private PlayerController playerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        this.playerController = new PlayerController(this, findViewById(R.id.crtLay_player));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
