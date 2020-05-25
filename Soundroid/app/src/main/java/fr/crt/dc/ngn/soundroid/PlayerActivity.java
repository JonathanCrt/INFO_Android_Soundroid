package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;


public class PlayerActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, SensorEventListener {

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
    private ImageView ivControlVolume;

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
    private String currentTagOfSong = null;
    private AudioManager audioManager;
    private enum PlayerState {INIT, PLAYING, PAUSE};
    private PlayerState playerState = PlayerState.INIT;

    //private SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    //private SharedPreferences.Editor editor = sharedPreferences.edit();

    private SongDao songDaoInstance;
    public static int MAX_SIZE_TAG = 25;
    private ImageView[] stars;
    private GestureDetectorCompat mDetector;
    private static final int SWIPE_MIN_DISTANCE = 400;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 1000;

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
        this.ivControlVolume = findViewById(R.id.iv_player_control_volume);
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
        this.mDetector = new GestureDetectorCompat(this, this);
        this.mDetector.setOnDoubleTapListener(this);
        this.audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        this.installOnClickListenerButtonControlVolume();
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert senSensorManager != null;
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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
    protected void onPause() {
        this.senSensorManager.unregisterListener(this, this.senAccelerometer);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mHandler.removeCallbacks(mRunnable);
        this.doUnbindService();
    }

    @Override
    protected void onResume() {
        this.senSensorManager.registerListener(this, this.senAccelerometer, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
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
        if (tag != null && tag.length() > 0) {
            this.ivDeleteTag.setVisibility(View.VISIBLE);
        } else {
            this.ivDeleteTag.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * set properties of textView, ImageView...
     */
    public void setWidgetsValues() {
        new Thread(() -> {
            Song currentSong = this.songService.getPlaylistSongs().get(this.songService.getSongIndex());
            this.currentTagOfSong = this.songDaoInstance.findTagBySongId(currentSong.getSongId());
            int currentRating = this.songDaoInstance.findRatingBySongId(currentSong.getSongId());
            this.runOnUiThread(() -> {
                setWidgetsValues(currentSong.getTitle(), currentSong.getArtist(), currentRating, Utility.convertByteToBitmap(currentSong.getArtwork()),
                        currentSong.getDuration(), this.currentTagOfSong);
                this.editSharedPreferences(currentSong.getSongId());
            });
        }).start();
    }

    /**
     * to allow scroll textView
     */
    private void setTextViewSelected() {
        this.tvTitleSong.setSelected(true);
        this.tvArtistSong.setSelected(true);
    }

    /**
     * install onClick listeners on playback control buttons
     */
    private void installListener() {
        this.ivControlPlaySong.setOnClickListener(v -> pushPlayControl());
        this.ivControlNextSong.setOnClickListener(v -> pushNextControl());
        this.ivControlPreviousSong.setOnClickListener((v -> pushPreviousControl()));
        this.ivRandomPlayback.setOnClickListener(v -> pushShuffleControl());
        this.ivAddTag.setOnClickListener(v -> openAlertDialogToAddTag());
    }

    /**
     * open alert dialog when the user wants to add a new playlist
     */
    private void openAlertDialogToAddTag() {
        new Thread(() -> {
            long currentSongId = this.songService.getPlaylistSongs().get(songService.getSongIndex()).getSongId();
            this.currentTagOfSong = this.songDaoInstance.findTagBySongId(currentSongId);
            EditText editTextTag = new EditText(this);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            this.runOnUiThread(() -> {
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
                        new Thread(() -> this.songDaoInstance.updateSongTagById(this.currentTagOfSong, currentSongId)).start();
                        this.runOnUiThread(() -> {
                            this.tvTag.setText(currentTagOfSong);
                            this.ivDeleteTag.setVisibility(View.VISIBLE);
                        });
                    }

                }).setNegativeButton("ANNULER", (dialog, whichButton) -> dialog.dismiss())
                        .create();
                AlertDialog ad = alertDialogBuilder.create();
                ad.show();
                ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
                ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
            });
        }).start();
    }

    /**
     * Return to MainActivity by clicking on the Imageview ivBack
     */
    public void setIvBackListener() {
        this.ivBack.setOnClickListener(v -> finish());
    }

    /**
     * Reset rating to 0, and set outlines stars
     */
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
        new Thread(() -> {
            this.songDaoInstance.updateSongRatingById(nbStars, this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getSongId());
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

    /**
     * run on UI Thread runnable to display seekbar progression
     */
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

    /**
     * install onclick listener when user wants to delete tag
     */
    private void installOnClickListenerButtonDeleteTag() {
        this.ivDeleteTag.setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder
                    .setTitle("Suppression").setMessage("Voulez-vous vraiment supprimer ce tag ?")
                    .setPositiveButton("OUI", ((dialog, which) -> {
                        new Thread(() -> {
                            this.songDaoInstance.updateSongWithNullTagBySongId(this.songService.getPlaylistSongs().get(songService.getSongIndex()).getSongId());
                            String text = this.songDaoInstance.findTagBySongId(this.songService.getPlaylistSongs().get(songService.getSongIndex()).getSongId());
                            this.runOnUiThread(() -> this.tvTag.setText(text));
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
            this.playerState = PlayerState.PAUSE;
            Toast.makeText(this, "State : " + this.playerState.name(), Toast.LENGTH_SHORT).show();
            this.ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_white_2x));
            this.mHandler.removeCallbacks(mRunnable);
        } else {
            this.playerState = PlayerState.PLAYING;
            long id = getIntent().getLongExtra("ID_SONG", 0L);
            Log.i("ID", "id_song: " + id) ;
            new Thread(() -> {
                Song song = this.songDaoInstance.findById(id);
                boolean b = this.songService.getPlaylistSongs().contains(song);
                Log.i("player activity", "search song : " + b);
            }).start();
            Toast.makeText(this, "State : " + this.playerState.name(), Toast.LENGTH_SHORT).show();
            this.ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
            this.runUIThreadToSetProgressSeekBar();
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("playback_state", this.playerState.name()).apply();
    }

    private void pushNextControl() {
        this.songService.playNextSong();
        ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        this.clearRating();
        this.setWidgetsValues();
        this.setRandomBackgroundRedRange();
    }

    private void pushPreviousControl() {
        this.songService.playPreviousSong();
        ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white_2x));
        this.clearRating();
        this.setWidgetsValues();
        this.setRandomBackgroundRedRange();
    }

    /**
     * start the shuffle mode
     */
    private void pushShuffleControl() {
        if (this.songService.toShuffle()) {
            ivRandomPlayback.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_unshuffle_white));
        } else {
            ivRandomPlayback.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_shuffle_white));
        }
    }

    private void shakeAnimateOnArtworkImageView() {
        RotateAnimation rotate = new RotateAnimation(-5, 5, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(200);
        rotate.setStartOffset(50);
        rotate.setRepeatMode(Animation.REVERSE);
        rotate.setInterpolator(new CycleInterpolator(5));
        this.ivArtworkSong.startAnimation(rotate);
    }

    private void setRandomBackgroundRedRange() {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(20), rnd.nextInt(20));
        findViewById(R.id.crtLay_player).setBackgroundColor(color);
    }

    private void installOnClickListenerButtonControlVolume() {
        this.ivControlVolume.setOnClickListener(v -> this.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI));
    }

    private void editSharedPreferences(long songId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("current_song_title", this.tvTitleSong.getText().toString());
        editor.putString("current_song_artist", this.tvArtistSong.getText().toString());
        editor.putLong("current_song_id", songId);
        editor.putString("playback_state", this.playerState.name());
        editor.apply();
    }

    // Touch and gestures events
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        this.openAlertDialogToAddTag();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            // bottom to top swipe
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                //this.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return false;
            }

            // top to bottom swipe
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                //this.audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return false;
            }

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                this.pushPreviousControl();
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                this.pushNextControl();
            }

        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        this.pushPlayControl();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    /***************SensorEventListener*****************/
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // retrieve sensor values
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
                if (speed > SHAKE_THRESHOLD) {
                    Random random = new Random();
                    int randomSong = random.nextInt(this.songService.getPlaylistSongs().size());
                    this.songService.setCurrentSong(randomSong);
                    this.songService.playOrPauseSong();
                    this.setWidgetsValues();
                    this.shakeAnimateOnArtworkImageView();
                    this.setRandomBackgroundRedRange();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
