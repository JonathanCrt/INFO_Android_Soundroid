package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import fr.crt.dc.ngn.soundroid.controller.PlayerController;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.service.SongService;
import fr.crt.dc.ngn.soundroid.utility.Utility;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


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
    private TextView tvElapsedTime;
    private TextView tvDuration;


    private SongService songService;
    private Intent intent;
    private boolean connectionEstablished;
    private Handler mHandler;
    private Runnable mRunnable;
    private long currentSongLength;


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
        this.tvElapsedTime = findViewById(R.id.tv_player_elapsed_time);
        this.tvDuration = findViewById(R.id.tv_player_end_time);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        this.initializeViews();
        this.mHandler = new Handler();
        this.setListenerRating();
        this.changeSeekBar();
        this.setIvBackListener();
        this.setTextViewSelected();
        this.installListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.doBindService();
        String currentTitle = getIntent().getStringExtra("TITLE_SONG");
        String currentArtist = getIntent().getStringExtra("ARTIST_SONG");
        int currentNote = getIntent().getIntExtra("RATING_SONG", 0);
        Bitmap currentArtwork = getIntent().getParcelableExtra("ARTWORK_SONG");
        long currentDuration = getIntent().getLongExtra("DURATION_SONG", 0L);
        Log.i("Values: ", currentTitle + " " + " " + currentArtist + " " + currentNote);
        this.setWidgetsValues(currentTitle, currentArtist, currentNote, currentArtwork, currentDuration);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mHandler.removeCallbacks(mRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
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

    /**
     * bind service to obtain a persistent connection
     */
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

    /**
     * set widgets values according to the given parameters
     *
     * @param title   title of song
     * @param artist  artist of song
     * @param rating  rating of song
     * @param artwork artwork of song
     */
    public void setWidgetsValues(String title, String artist, int rating, Bitmap artwork, long duration) {
        this.tvTitleSong.setText(title);
        this.tvArtistSong.setText(artist);
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
        this.tvDuration.setText(Utility.convertDuration(duration));
    }

    private void setTextViewSelected() {
        this.tvTitleSong.setSelected(true);
        this.tvArtistSong.setSelected(true);
    }

    private void installListener() {

        this.ivControlPlaySong.setOnClickListener(v -> pushPlayControl());
        this.ivControlNextSong.setOnClickListener(v -> pushNextControl());
        this.ivControlPreviousSong.setOnClickListener((v -> pushPreviousControl()));
        this.ivRandomPlayback.setOnClickListener(v -> pushShuffleControl());
    }

    /**
     * return to MainActivity by clicking on the Imageview ivBack
     */
    public void setIvBackListener() {
        this.ivBack.setOnClickListener(v -> finish());
    }

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

    /**
     * set progress of seekbar
     */
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

    private void runUIThreadToSetProgressSeekBar() {
        this.mRunnable = new Runnable() { // UI Thread
            @Override
            public void run() {
                currentSongLength = songService.getSongDuration();
                seekBarPlayback.setMax((int) currentSongLength / 1000); // To calculate the progress of the song
                int mCurrentPosition = songService.getCurrentPositionPlayer() / 1000;
                seekBarPlayback.setProgress(mCurrentPosition); // To set progress to current position
                tvElapsedTime.setText(Utility.convertDuration(songService.getCurrentPositionPlayer())); // Put the current time in the tv
                mHandler.postDelayed(this, 1000); // Every second
            }
        };

        this.runOnUiThread(mRunnable);
    }

    private void pushPlayControl() {
        //this.songService.setToolbarPushed(true);
        Log.i("PlayerActivity av", this.songService.toString());
        if (!songService.playOrPauseSong()) {
            Toast.makeText(this, "State : Pause", Toast.LENGTH_SHORT).show();
            ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_white_2x));
        } else {
            Toast.makeText(this, "State : Play", Toast.LENGTH_SHORT).show();
            ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        }
        Log.i("PlayerActivity aft", this.songService.toString());
    }

    private void pushNextControl() {
        this.songService.playNextSong();
        ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        setWidgetsValues(
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getTitle(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getArtist(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getRating(),
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork(),
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getDuration()
        );
    }

    private void pushPreviousControl() {
        this.songService.playPreviousSong();
        ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        setWidgetsValues(
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getTitle(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getArtist(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getRating(),
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork(),
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getDuration()
        );
    }

    /**
     * start the shuffle mode
     */
    private void pushShuffleControl() {

        this.songService.toShuffle();

    }

}
