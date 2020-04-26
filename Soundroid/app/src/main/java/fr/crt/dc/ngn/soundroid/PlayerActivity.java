package fr.crt.dc.ngn.soundroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import fr.crt.dc.ngn.soundroid.controller.PlayerController;
import fr.crt.dc.ngn.soundroid.service.SongService;
import jp.wasabeef.blurry.Blurry;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.Serializable;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {

    public static class State implements Serializable {

        private PlayerController playerController;

        State(PlayerController playerController) {
            this.playerController = playerController;
        }
    }


    private PlayerController playerController;
    private ImageView ivBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        this.ivBack = findViewById(R.id.iv_player_back);

        Blurry.with(this)
                .radius(10)
                .sampling(8)
                .color(Color.argb(66, 255, 255, 0))
                .async()
                .onto(findViewById(R.id.crtLay_player));

        Intent intent = getIntent();

        //attempt get artist info with intent for first launch
        //problem : how to get info from mainActivity withtout songService ?
       /*if(intent !=null){
            String artist = intent.getStringExtra("artist");
            this.playerController.setTextViewArtistSong(artist);
        }*/

        if (savedInstanceState != null) {
            State state = (State) savedInstanceState.getSerializable("state");
            this.playerController = state.playerController;
        }

        this.playerController = new PlayerController(this, findViewById(R.id.crtLay_player));
        playerController.setListenerRating();
        playerController.changeSeekBar();
        this.setIvBackListener();

    }

    @Override
    protected void onStart() {
        super.onStart();
        playerController.setWidgetsValues();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void setIvBackListener() {
        this.ivBack.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        State state = new State(this.playerController);
        outState.putSerializable("state", state);
    }
}
