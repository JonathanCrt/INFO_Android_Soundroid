package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;
import fr.crt.dc.ngn.soundroid.controller.PlayerController;
import jp.wasabeef.blurry.Blurry;

import android.graphics.Color;
import android.os.Bundle;

public class PlayerActivity extends AppCompatActivity {

    private PlayerController playerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        this.playerController = new PlayerController(this, findViewById(R.id.crtLay_player));

        Blurry.with(this)
                .radius(10)
                .sampling(8)
                .color(Color.argb(66, 255, 255, 0))
                .async()
                .onto(findViewById(R.id.crtLay_player));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
