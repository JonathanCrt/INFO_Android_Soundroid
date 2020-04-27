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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {


    // Views
    private ImageView ivBack;
    private TextView tvTitleSong;
    private TextView tvArtistSong;
    private ImageView ivArtworkSong;
    private ImageView ivRandomPlayback;
    private ImageView ivNoteStarOne;
    private ImageView ivNoteStarTwo;
    private ImageView ivNoteStarThree;
    private ImageView ivNoteStarFour;
    private ImageView ivNoteStarFive;
    private ImageView ivAddTag;
    private SeekBar seekBarPlayback;
    private ImageView ivControlPlaySong;
    private ImageView ivControlNextSong;
    private ImageView ivControlPreviousSong;
    private boolean isNoteSet;


    private SongService songService;
    private Intent intent;
    private boolean connectionEstablished;


    private PlayerController playerController;


    private void initializeViews() {
        this.ivBack = findViewById(R.id.iv_player_back);
        this.tvTitleSong = findViewById(R.id.tv_player_title);
        this.tvArtistSong = findViewById(R.id.tv_player_artist);
        this.ivArtworkSong = findViewById(R.id.iv_player_artwork);
        this.ivRandomPlayback = findViewById(R.id.iv_player_shuffle_playback);

        this.ivNoteStarOne = findViewById(R.id.iv_player_note_star_1);
        this.ivNoteStarTwo = findViewById(R.id.iv_player_note_star_2);
        this.ivNoteStarThree = findViewById(R.id.iv_player_note_star_3);
        this.ivNoteStarFour = findViewById(R.id.iv_player_note_star_4);
        this.ivNoteStarFive = findViewById(R.id.iv_player_note_star_5);

        this.ivAddTag = findViewById(R.id.iv_player_add_tag);
        this.seekBarPlayback = findViewById(R.id.seekBar_player);
        this.ivControlPreviousSong = findViewById(R.id.iv_player_control_previous);
        this.ivControlNextSong = findViewById(R.id.iv_player_control_next);
        this.ivControlPlaySong = findViewById(R.id.iv_player_control_play);
    }


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
        this.initializeViews();


        Intent intent = getIntent();

        //attempt get artist info with intent for first launch
        //problem : how to get info from mainActivity withtout songService ?
       /*if(intent !=null){
            String artist = intent.getStringExtra("artist");
            this.playerController.setTextViewArtistSong(artist);
        }*/
       /*
        if (savedInstanceState != null) {
            State state = (State) savedInstanceState.getSerializable("state");
            this.playerController = state.playerController;
        }
        //this.playerController = new PlayerController(this, findViewById(R.id.crtLay_player));

        */
        this.setListenerRating();
        this.changeSeekBar();
        this.setIvBackListener();

        installListener();

    }

    @Override
    protected void onStart() {
        super.onStart();
        this.doBindService();
        String currentTitle = getIntent().getStringExtra("TITLE_SONG");
        String currentArtist = getIntent().getStringExtra("ARTIST_SONG");
        int currentNote = getIntent().getIntExtra("RATING_SONG", 0);
        Bitmap currentArtwork = intent.getParcelableExtra("ARTWORK_SONG");
        Log.i("Values: ", currentTitle + " " + " " + currentArtist + " " + currentNote);
        this.setWidgetsValues(currentTitle, currentArtist, currentNote, currentArtwork);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    public void doBindService() {
        if (intent == null) {
            intent = new Intent(this, SongService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        startService(intent);
    }

    public void doUnbindService() {
        if (connectionEstablished) {
            unbindService(serviceConnection);
            connectionEstablished = false;
        }
        this.stopService(intent);
        this.songService = null;
    }


    public void setWidgetsValues(String title, String artist, int rating, Bitmap artwork) {
        this.tvTitleSong.setText(title);
        this.tvArtistSong.setText(artist);
        //setArtworkSong(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork(), this.ivArtworkSong);
        Log.i("Values: ", title + " " + " " + artist + " " + rating);
        switch (rating) {
            case 1:
                setRatingToOne();
                break;
            case 2:
                setRatingToTwo();
                break;
            case 3:
                setRatingToThree();
                break;
            case 4:
                setRatingToFour();
                break;
            case 5:
                setRatingToFive();
                break;
        }
        this.ivArtworkSong.setImageBitmap(artwork);
        seekBarPlayback.setProgress(0);
    }

    private void installListener() {
        this.ivControlPlaySong.setOnClickListener(v -> pushPlayControl());
        this.ivControlNextSong.setOnClickListener(v -> pushNextControl());
        this.ivControlPreviousSong.setOnClickListener((v -> pushPreviousControl()));
        this.ivRandomPlayback.setOnClickListener(v -> pushShuffleControl());
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

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongService.SongBinder songBinder = (SongService.SongBinder) service;
            // Permet de récupérer le service
            songService = songBinder.getService();
            connectionEstablished = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectionEstablished = false;
        }
    };


    private void resetRating() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(0);
    }


    private void setRatingToOne() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(1);
    }

    private void setRatingToTwo() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(2);
    }

    private void setRatingToThree() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(3);
    }

    private void setRatingToFour() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(4);
    }

    private void setRatingToFive() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(5);
    }


    public void setListenerRating() {
        this.ivNoteStarOne.setOnClickListener(v -> {
            if (!isNoteSet) {
                this.setRatingToOne();
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarTwo.setOnClickListener(v -> {
            if (!isNoteSet) {
                this.setRatingToTwo();
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarThree.setOnClickListener(v -> {
            if (!isNoteSet) {
                this.setRatingToThree();
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarFour.setOnClickListener(v -> {
            if (!isNoteSet) {
                this.setRatingToFour();
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarFive.setOnClickListener(v -> {
            if (!isNoteSet) {
                this.setRatingToFive();
            } else {
                isNoteSet = false;
                resetRating();
            }
        });
    }


    public void changeSeekBar() {
        seekBarPlayback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                songService.handleSeekBar(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void pushPlayControl() {
        this.songService.setToolbarPushed(true);
        if (!songService.playOrPauseSong()) {
            Toast.makeText(this, "State : Pause", Toast.LENGTH_SHORT).show();
            ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_white_2x));
        } else {
            Toast.makeText(this, "State : Play", Toast.LENGTH_SHORT).show();
            ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
            //setWidgetsValues();
        }
    }

    private void pushNextControl() {
        this.songService.playNextSong();
        ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        setWidgetsValues(
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getTitle(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getArtist(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getRating(),
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork());
    }

    private void pushPreviousControl() {
        this.songService.playPreviousSong();
        ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        setWidgetsValues(
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getTitle(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getArtist(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getRating(),
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork());
    }

    private void pushShuffleControl() {
        //appelle fonction shuffle dans songSERvice
        this.songService.shuffleSongList();
        //setWidgetsValues();
    }

}
