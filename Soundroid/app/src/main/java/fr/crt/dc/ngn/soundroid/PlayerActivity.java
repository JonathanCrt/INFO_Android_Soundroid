package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import fr.crt.dc.ngn.soundroid.controller.PlayerController;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.dao.SongDao;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
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
import android.view.ViewGroup;
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
    private ImageView ivAddTag;
    private SeekBar seekBarPlayback;
    private ImageView ivControlPlaySong;
    private ImageView ivControlNextSong;
    private ImageView ivControlPreviousSong;
    private boolean isNoteSet;
    private TextView tvElapsedTime;
    private TextView tvDuration;
    private TextView tvTag;
    private ImageView ivDeleteTag;

    private SongService songService;
    private Intent intent;
    String currentTitleReceived;
    String currentArtisteReceived;
    int currentNoteReceived;
    Bitmap currentArtworkReceived;
    long currentDurationReceived;
    String currentTagReceived;
    private boolean connectionEstablished;
    private Handler mHandler;
    private Runnable mRunnable;
    private long currentSongLength;
    private ToolbarController toolbarController;
    private String currentTagOfSong = null;

    private SongDao songDaoInstance;
    public static int MAX_SIZE_TAG = 25;
    private ImageView[] stars;

    private void initializeViews() {
        this.ivBack = findViewById(R.id.iv_player_back);
        this.tvTitleSong = findViewById(R.id.tv_player_title);
        this.tvArtistSong = findViewById(R.id.tv_player_artist);
        this.ivArtworkSong = findViewById(R.id.iv_player_artwork);
        this.ivRandomPlayback = findViewById(R.id.iv_player_shuffle_playback);

        ImageView ivNoteStarOne = findViewById(R.id.iv_player_note_star_1);
        ImageView ivNoteStarTwo = findViewById(R.id.iv_player_note_star_2);
        ImageView ivNoteStarThree = findViewById(R.id.iv_player_note_star_3);
        ImageView ivNoteStarFour = findViewById(R.id.iv_player_note_star_4);
        ImageView ivNoteStarFive = findViewById(R.id.iv_player_note_star_5);

        this.ivAddTag = findViewById(R.id.iv_player_add_tag);
        this.seekBarPlayback = findViewById(R.id.seekBar_player);
        this.ivControlPreviousSong = findViewById(R.id.iv_player_control_previous);
        this.ivControlNextSong = findViewById(R.id.iv_player_control_next);
        this.ivControlPlaySong = findViewById(R.id.iv_player_control_play);
        this.tvElapsedTime = findViewById(R.id.tv_player_elapsed_time);
        this.tvDuration = findViewById(R.id.tv_player_end_time);
        this.tvTag = findViewById(R.id.tv_player_tag);
        this.ivDeleteTag = findViewById(R.id.iv_player_delete_tag);

        this.stars = new ImageView[]{ivNoteStarOne, ivNoteStarTwo, ivNoteStarThree, ivNoteStarFour, ivNoteStarFive};
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
        SoundroidDatabase soundroidDatabaseInstance = SoundroidDatabase.getInstance(this);
        this.songDaoInstance = soundroidDatabaseInstance.songDao();
        this.installOnClickListenerButtonDeleteTag();




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
        this.currentTagReceived = getIntent().getStringExtra("TAG_SONG");
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
            setWidgetsValues(currentTitleReceived, currentArtisteReceived, currentNoteReceived, currentArtworkReceived, currentDurationReceived, currentTagReceived);
            songService.getPlayer().setOnCompletionListener(mp -> {
                if (songService.getSongIndex() + 1 < songService.getPlaylistSongs().size()) {
                    songService.playNextSong();
                    setWidgetsValues();
                }
            });
            installDisplayOfTagWithDeleteButton();
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
    private void setWidgetsValues(String title, String artist, int rating, Bitmap artwork, long duration, String tag) {
        this.tvTitleSong.setText(title);
        this.tvArtistSong.setText(artist);
        this.setRating(rating);
        this.ivArtworkSong.setImageBitmap(artwork);
        this.tvDuration.setText(Utility.convertDuration(duration));
        if (songService.playerIsPlaying()) {
            ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        }
        this.tvTag.setText(tag);
        if(tag != null && tag.length() > 0){
            this.ivDeleteTag.setVisibility(View.VISIBLE);
        } else {
            this.ivDeleteTag.setVisibility(View.INVISIBLE);
        }
    }

    public void setWidgetsValues() {
        new Thread(()->{
            Song currentSong = this.songService.getPlaylistSongs().get(this.songService.getSongIndex());
            this.currentTagOfSong = this.songDaoInstance.findTagBySongId(currentSong.getSongId());
            int currentRating  = this.songDaoInstance.findRatingBySongId(currentSong.getSongId());
            Log.d("PlayerActivity rating", " val:" + this.songDaoInstance.findRatingBySongId(currentSong.getSongId()));
            this.runOnUiThread(()->{
                setWidgetsValues(
                        currentSong.getTitle(),
                        currentSong.getArtist(),
                        currentRating,
                        Utility.convertByteToBitmap(currentSong.getArtwork()),
                        currentSong.getDuration(),
                        this.currentTagOfSong
                );
            });
        }).start();
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
        new Thread(()->{
            long currentSongId = this.songService.getPlaylistSongs().get(songService.getSongIndex()).getSongId();
            this.currentTagOfSong = this.songDaoInstance.findTagBySongId(currentSongId);
            EditText editTextTag = new EditText(this);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

            alertDialogBuilder.setView(editTextTag).setIcon(R.drawable.ic_playlist_tag_black).setTitle("Tag de la chanson (max 25 caractères)");
            editTextTag.setText(this.currentTagOfSong);
            alertDialogBuilder.setPositiveButton("VALIDER", (dialog, whichButton) -> {
                String tag = editTextTag.getText().toString();
                if (tag.length() > MAX_SIZE_TAG) {
                    new AlertDialog.Builder(this)
                            .setTitle("Erreur lors de l'ajout du tag")
                            .setMessage("Le tag saisi est supérieur à 25 caractères ! ")
                            .show();
                } else {
                    this.currentTagOfSong = tag;
                    new Thread(()->{
                        this.songDaoInstance.updateSongTagById(this.currentTagOfSong, currentSongId);
                    }).start();
                    this.runOnUiThread(()->{
                        this.tvTag.setText(currentTagOfSong);
                        this.ivDeleteTag.setVisibility(View.VISIBLE);
                    });
                }

            }).setNegativeButton("ANNULER", (dialog, whichButton) -> dialog.dismiss())
                    .create();
            this.runOnUiThread(()->{
                AlertDialog ad = alertDialogBuilder.create();
                ad.show();
                ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
                ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
            });
        }).start();
    }

    /**
     * return to MainActivity by clicking on the Imageview ivBack
     */
    public void setIvBackListener() {
        this.ivBack.setOnClickListener(v -> finish());
    }

    private void resetRating() {
        for (ImageView star : this.stars) {
            star.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        }
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(0);
    }

    private void clearRating() {
        resetRating();
    }

    /**
     * Put an image of filled stars corresponding to the rating
     *
     * @param nbStars number of stars
     */
    private void setRating(int nbStars) {
        // fill the stars
        for (int i = 0; i < nbStars; i++) {
            this.stars[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filled_star_note));
        }
        // reset stars if the note has been decreased
        for (int i = nbStars; i < this.stars.length; i++) {
            this.stars[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_outline_star_note));
        }
        new Thread(()->{
            this.songDaoInstance.updateSongRatingById(nbStars, this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getSongId());
            Log.d("PlayerActivity update Rating : " + nbStars, this.songDaoInstance.getAllSongs().toString());
        }).start();
    }

    /**
     * Return a listener to put on image view of stars
     *
     * @param nbStars number of stars
     * @return listener
     */
    private View.OnClickListener getStarsListener(int nbStars) {
        return v -> {
            if (!isNoteSet) {
                this.setRating(nbStars);
            } else {
                isNoteSet = false;
                resetRating();
            }
        };
    }

    public void setListenerRating() {
        for (int i = 0; i < this.stars.length; i++) {
            this.stars[i].setOnClickListener(getStarsListener(i + 1));
        }
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
                if (songService.playerIsPlaying()) {
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

    private void installDisplayOfTagWithDeleteButton() {
        if (currentTagReceived != null && currentTagReceived.length() > 0) {
            this.tvTag.setText(currentTagReceived);
            this.ivDeleteTag.setVisibility(View.VISIBLE);
        } else {
            this.ivDeleteTag.setVisibility(View.INVISIBLE);
        }
    }

    private void installOnClickListenerButtonDeleteTag() {
        this.ivDeleteTag.setOnClickListener(v -> {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder
                    .setTitle("Supression").setMessage("Voulez-vous vraiment supprimer ce tag ?")
                    .setPositiveButton("OUI", ((dialog, which) -> {
                        new Thread(()->{
                            this.songDaoInstance.updateSongWithNullTagBySongId(this.songService.getPlaylistSongs().get(songService.getSongIndex()).getSongId());
                            String text = this.songDaoInstance.findTagBySongId(this.songService.getPlaylistSongs().get(songService.getSongIndex()).getSongId());
                            this.runOnUiThread(()->{
                                this.tvTag.setText(text);
                            });
                        }).start();
                        this.ivDeleteTag.setVisibility(View.INVISIBLE);
                    }))
                    .setNegativeButton("NON", (dialog, whichButton) -> dialog.dismiss());
            AlertDialog ad = alertDialogBuilder.create();
            ad.show();
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
            ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
        });


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
        this.setWidgetsValues();
    }

    private void pushPreviousControl() {
        this.songService.playPreviousSong();
        ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        Log.d("PlayerActivity pushPrevious", " " + this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getRating());
        this.clearRating();
        this.setWidgetsValues();
    }

    /**
     * start the shuffle mode
     */
    private void pushShuffleControl() {
        if(this.songService.toShuffle()){
            ivRandomPlayback.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_unshuffle_white));
        }else{
            ivRandomPlayback.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_shuffle_white));
        }


    }
}
