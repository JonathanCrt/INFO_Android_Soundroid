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
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {

    private PlayerController playerController;
    private ImageView ivBack;
    private TextView tvTitleSong;
    private TextView tvArtistSong;


    public static class State implements Serializable {
        private PlayerController playerController;
        State(PlayerController playerController) {
            this.playerController = playerController;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        this.ivBack = findViewById(R.id.iv_player_back);
        this.tvArtistSong = findViewById(R.id.tv_player_artist);


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
        String currentTitle = getIntent().getStringExtra("TITLE_SONG");
        String currentArtist = getIntent().getStringExtra("ARTIST_SONG");
        int currentNote = getIntent().getIntExtra("RATING_SONG", 0);
        this.playerController.setWidgetsValues(currentTitle, currentArtist, currentNote);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //this.playerController.unbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //this.playerController.unbindService();
    }

    public void setIvBackListener() {
        this.ivBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        State state = new State(this.playerController);
        outState.putSerializable("state", state);
    }

}
