package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import fr.crt.dc.ngn.soundroid.controller.PlayerController;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.service.SongService;
import fr.crt.dc.ngn.soundroid.utility.Utility;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
    String currentTitleReceived;
    String currentArtisteReceived;
    int currentNoteReceived;
    Bitmap currentArtworkReceived;
    long currentDurationReceived;
    private boolean connectionEstablished;
    private Handler mHandler;
    private Runnable mRunnable;
    private long currentSongLength;
    private ToolbarController toolbarController;

    private SoundroidDatabase soundroidDatabaseInstance;
    public static int MAX_SIZE_TAG = 25;

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
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(this);
        /*
        final LayoutInflater factory = getLayoutInflater();
        final View mainActivityView = factory.inflate(R.layout.toolbar_player, null);
        this.toolbarController = new ToolbarController(this, mainActivityView.findViewById(R.id.crt_layout));

         */
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.doBindService(); // asynchrone -- onServiceConnected
        this.currentTitleReceived = getIntent().getStringExtra("TITLE_SONG");
        this.currentArtisteReceived = getIntent().getStringExtra("ARTIST_SONG");
        this.currentNoteReceived = getIntent().getIntExtra("RATING_SONG", 0);
        this.currentArtworkReceived = getIntent().getParcelableExtra("ARTWORK_SONG");
        this.currentDurationReceived = getIntent().getLongExtra("DURATION_SONG", 0L);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mHandler.removeCallbacks(mRunnable);
        this.doUnbindService();
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
            runUIThreadToSetProgressSeekBar();
            setWidgetsValues(currentTitleReceived, currentArtisteReceived, currentNoteReceived, currentArtworkReceived, currentDurationReceived);
            songService.getPlayer().setOnCompletionListener(mp -> {
                if(songService.getSongIndex() + 1 < songService.getPlaylistSongs().size()) {
                    songService.playNextSong();
                    setWidgetsValues(
                            songService.getPlaylistSongs().get(songService.getSongIndex()).getTitle(),
                            songService.getPlaylistSongs().get(songService.getSongIndex()).getArtist(),
                            songService.getPlaylistSongs().get(songService.getSongIndex()).getRating(),
                            Utility.convertByteToBitmap(songService.getPlaylistSongs().get(songService.getSongIndex()).getArtwork()),
                            songService.getPlaylistSongs().get(songService.getSongIndex()).getDuration()
                    );
                }
            });
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
        this.setStarRating(rating);
        this.ivArtworkSong.setImageBitmap(artwork);
        this.tvDuration.setText(Utility.convertDuration(duration));
        if(songService.playerIsPlaying()){
            ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        }
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
        this.ivAddTag.setOnClickListener(v -> openAlertDialogToAddTag());
    }

    private void openAlertDialogToAddTag() {
        EditText editTextTag = new EditText(this);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(editTextTag);
        alertDialogBuilder.setIcon(R.drawable.ic_playlist_tag_black);
        alertDialogBuilder.setTitle("Ajout d'un tag");

        //String currentTag = this.soundroidDatabaseInstance.songDao().

        alertDialogBuilder.setPositiveButton("OK", (dialog, whichButton) -> {
            String tag = editTextTag.getText().toString();
            if (tag.length() > MAX_SIZE_TAG) {
                new AlertDialog.Builder(this)
                        .setTitle("Erreur lors de l'ajout du tag")
                        .setMessage("Le tag saisi est supérieur à 25 caractères ! ")
                        .show();
            } else {
                this.soundroidDatabaseInstance.songDao().updateSongTagById(tag, this.songService.getPlaylistSongs().get(songService.getSongIndex()).getId());
                Log.d("PlayerActivity add tag", this.soundroidDatabaseInstance.songDao().getAllSongs().toString());
            }

        }).setNegativeButton("ANNULER", (dialog, whichButton) -> finish())
                .create();

        AlertDialog ad = alertDialogBuilder.create();

        ad.show();
        ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
        ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
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

    private void clearRating() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        Log.d("PlayerActivity clear rating", this.soundroidDatabaseInstance.songDao().getAllSongs().toString());
    }


    private void setStarRating(int givenRating) {

        switch (givenRating) {
            case 1:
                ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
                ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
                ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
                ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(1);
                this.soundroidDatabaseInstance.songDao().updateSongRatingById(1, this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getId());
                Log.d("PlayerActivity update Rating 1.", this.soundroidDatabaseInstance.songDao().getAllSongs().toString());
                break;
            case 2:
                ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
                ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
                ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(2);
                this.soundroidDatabaseInstance.songDao().updateSongRatingById(2, this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getId());
                Log.d("PlayerActivity update Rating 2.", this.soundroidDatabaseInstance.songDao().getAllSongs().toString());
                break;
            case 3:
                ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
                ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(3);
                this.soundroidDatabaseInstance.songDao().updateSongRatingById(3, this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getId());
                Log.d("PlayerActivity update Rating 3.", this.soundroidDatabaseInstance.songDao().getAllSongs().toString());
                break;
            case 4:
                ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(4);
                this.soundroidDatabaseInstance.songDao().updateSongRatingById(4, this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getId());
                Log.d("PlayerActivity update Rating 4.", this.soundroidDatabaseInstance.songDao().getAllSongs().toString());
                break;
            case 5:
                ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(5);
                this.soundroidDatabaseInstance.songDao().updateSongRatingById(5, this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getId());
                Log.d("PlayerActivity update Rating 5.", this.soundroidDatabaseInstance.songDao().getAllSongs().toString());
        }

    }

    public void setListenerRating() {
        this.ivNoteStarOne.setOnClickListener(v -> {
            if (!isNoteSet) {
                this.setStarRating(1);
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarTwo.setOnClickListener(v -> {
            if (!isNoteSet) {
                this.setStarRating(2);
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarThree.setOnClickListener(v -> {
            if (!isNoteSet) {
                this.setStarRating(3);
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarFour.setOnClickListener(v -> {
            if (!isNoteSet) {
                this.setStarRating(4);
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarFive.setOnClickListener(v -> {
            if (!isNoteSet) {
                this.setStarRating(5);
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
                // mediaPlayer is Playing ?
                if(songService.playerIsPlaying()) {
                    currentSongLength = songService.getSongDuration();
                    seekBarPlayback.setMax((int) currentSongLength / 1000); // To calculate the progress of the song
                    int mCurrentPosition = songService.getCurrentPositionPlayer() / 1000;
                    seekBarPlayback.setProgress(mCurrentPosition); // To set progress to current position
                    tvElapsedTime.setText(Utility.convertDuration(songService.getCurrentPositionPlayer())); // Put the current time in the tv
                    mHandler.postDelayed(this, 1000); // Every second
                }
            }
        };

        this.runOnUiThread(mRunnable);
    }

    private void pushPlayControl() {
        this.songService.setToolbarPushed(true);
        if (!songService.playOrPauseSong()) {
            Toast.makeText(this, "State : Pause", Toast.LENGTH_SHORT).show();
            ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_white_2x));
            this.mHandler.removeCallbacks(mRunnable);
        } else {
            Toast.makeText(this, "State : Play", Toast.LENGTH_SHORT).show();
            ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
            this.runUIThreadToSetProgressSeekBar();
        }
    }

    private void pushNextControl() {
        this.songService.playNextSong();
        ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        Log.d("PlayerActivity pushNext", " " + this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getRating());
        this.clearRating();
        setWidgetsValues(
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getTitle(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getArtist(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getRating(),
                Utility.convertByteToBitmap(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork()),
                this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getDuration()
        );
    }

    private void pushPreviousControl() {
        this.songService.playPreviousSong();
        ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        Log.d("PlayerActivity pushPrevious", " " + this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getRating());
        this.clearRating();
        setWidgetsValues(
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getTitle(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getArtist(),
                this.songService.getPlaylistSongs().get(songService.getSongIndex()).getRating(),
                Utility.convertByteToBitmap(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork()),
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
