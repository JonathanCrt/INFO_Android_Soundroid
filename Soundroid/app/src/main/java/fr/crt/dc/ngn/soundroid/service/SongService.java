package fr.crt.dc.ngn.soundroid.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import fr.crt.dc.ngn.soundroid.MainActivity;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.History;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.utility.Utility;

/**
 * Created by CRETE JONATHAN on 05/04/2020.
 */
public class SongService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {


    private MediaPlayer player;
    private int songIndex;
    private final IBinder songBinder = new SongBinder();
    private ArrayList<Song> playlistSongs;
    private boolean isToolbarPushed;
    private boolean initializeSong;
    private Button btnPanelPause;
    private boolean isShuffled = false;
    private Random rand;

    // Arbitrary ID for the notification (with different IDs a service can manage several notifications)
    public static final int NOTIFICATION_ID = 1;

    //Identifier of the channel used for notification (required since API 26)
    public static final String CHANNEL_ID = SongService.class.getName() + ".SOUNDROID_CHANNEL";

    public static final String NOTIFY_PREVIOUS = "fr.crt.dc.ngn.soundroid.PREVIOUS";
    public static final String NOTIFY_PLAY = "fr.crt.dc.ngn.soundroid.PLAY";
    public static final String NOTIFY_PAUSE = "fr.crt.dc.ngn.soundroid.PAUSE";
    public static final String NOTIFY_NEXT = "fr.crt.dc.ngn.soundroid.NEXT";
    public static final String NOTIFY_DELETE = "fr.crt.dc.ngn.soundroid.DELETE";

    private SoundroidDatabase soundroidDatabaseInstance;
    /**
     * Class that returns an instance of service
     */
    public class SongBinder extends Binder {
        public SongService getService() {
            return SongService.this;
        }
    }

    public SongService getSongService() {
        return SongService.this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.songIndex = 0;
        this.rand = new Random();
        this.player = new MediaPlayer();
        this.player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
        this.createNotificationChannel();
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(getApplicationContext());

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return songBinder;
    }

    /**
     * Permet libérer des ressources lorsque l'instance du service est non liée
     *
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        this.player.stop();
        this.player.release();
        return false;
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        this.player.start();
        Log.i("SongService", playlistSongs.get(songIndex).getTitle());

    }

    /**
     * to create a NotificationChannel, but only on API 26+ because
     * the NotificationChannel class is new and not in the support library
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            String description = "Channel for notifications of the song service";

            // Channel which represents notification (see the system notification options)
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Lecture en cours", importance);
            channel.setDescription(description);
            channel.setLightColor(R.color.colorAccent);
            //channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            // Register the channel with the system;
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {


        // Create intent for notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // PendingIntent makes the return of the user when selecting the notification MainActivity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.notification_playback);
        expandedView.setImageViewBitmap(R.id.iv_expanded_panel_artwork, Utility.convertByteToBitmap(playlistSongs.get(songIndex).getArtwork()));
        expandedView.setTextViewText(R.id.tv_expanded_panel_title, playlistSongs.get(songIndex).getTitle());
        expandedView.setTextViewText(R.id.tv_expanded_panel_artist, playlistSongs.get(songIndex).getArtist());


        Intent previousIntent = new Intent(NOTIFY_PREVIOUS);
        Intent nextIntent = new Intent(NOTIFY_NEXT);
        Intent pauseIntent = new Intent(NOTIFY_PAUSE);
        Intent playIntent = new Intent(NOTIFY_PLAY);
        Intent deleteIntent = new Intent(NOTIFY_DELETE);

        Intent testIntent = new Intent("test");

        PendingIntent piPrevious = PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent piNext = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent piPause = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent piPlay = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent piDelete = PendingIntent.getBroadcast(this, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent piTest = PendingIntent.getBroadcast(this, 0, testIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
        expandedView.setPendingIntentTemplate(R.id.btn_panel_previous, piPrevious);
        expandedView.setPendingIntentTemplate(R.id.btn_panel_next, piNext);
        expandedView.setPendingIntentTemplate(R.id.btn_panel_pause, piPause);
        expandedView.setPendingIntentTemplate(R.id.btn_panel_play, piPlay);
        expandedView.setPendingIntentTemplate(R.id.btn_expanded_panel_delete, piDelete);

        expandedView.setOnClickPendingIntent(R.id.btn_panel_play, piTest);

         */

        // build a read notification to display it in the notifications panel
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.soundroid_logo)
                .setAutoCancel(true)
                .setCustomBigContentView(expandedView)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Control Audio")
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE);

        //installListeners(expandedView, getBaseContext());


        return builder.build();
    }

    @Override
    public void onCompletion(MediaPlayer mp) { }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Une erreur est survenue lors de la lecture", Toast.LENGTH_SHORT).show();
        this.player.reset();
        return false;
    }

    private void insertInHistory(long songId){
        new Thread(()->{
            this.soundroidDatabaseInstance.historyDao().insertHistory(songId);
            Log.d("HISTORY", "insert in history song id = " + songId);
        }).start();

    }

    private void initializeSong() {
        this.player.reset();
        Song currentSong = playlistSongs.get(songIndex);
        long currentSongID = currentSong.getSongId();
        Uri songUri = Uri.parse(currentSong.getLink());

        try {
            this.player.setDataSource(getApplicationContext(), songUri);

        } catch (IOException e) {
            Log.e("SongService", "Error to set data source", e);
            e.getMessage();
        }
        try {
            this.player.prepare();
            Log.i("SongService", "play!");
            startPlayBack();
            // Insert the song in the history
            insertInHistory(currentSongID);

        } catch (IOException e) {
            Log.e("SongService", "Error to prepare player", e);
            e.getMessage();
        }

        this.initializeSong = true;
    }

    private boolean endPlayOrPauseSong(boolean isPlaying) {
        this.isToolbarPushed = false;
        return isPlaying;
    }

    public boolean playOrPauseSong() {
        this.startForeground(NOTIFICATION_ID, this.createNotification());
        // music is paused so user want to restart it or to start a new song
        if (!this.player.isPlaying() && this.isToolbarPushed) {
            if (!initializeSong) {  // no music yet
                initializeSong();
            } else {    // already a music so just restart it
                startPlayBack();
            }
            return endPlayOrPauseSong(true); // music is playing
        } else if (this.player.isPlaying() && this.isToolbarPushed) {  // music is playing so user want to pause
            this.player.pause();
            return endPlayOrPauseSong(false); // music is paused
        } else {
            // user click on a music on the list
            initializeSong();
            return endPlayOrPauseSong(true); // music is playing
        }
    }

    /**
     * permet de gerer le mode aleatoire des music a lancer
     * Permet de gérer le contrôle suivant
     * On incrémente  l'index de la chanson,
     * vérifier que nous ne sommes pas allés en dehors de la plage de la liste,
     * et on appelle la méthode playSong()
     */
    public void playNextSong() {

        if (this.isShuffled) {
            int currentSong = this.songIndex;
            while (currentSong == this.songIndex) {
                currentSong = rand.nextInt(playlistSongs.size());
            }
            this.songIndex = currentSong;
        }
        this.songIndex++;
        if (songIndex >= playlistSongs.size()) {
            this.songIndex = 0;
        }
        this.playOrPauseSong();
    }

    /**
     * Permet de gérer le contrôle précédent
     * On décrémente  l'index de la chanson,
     * vérifier que nous ne sommes pas allés en dehors de la plage de la liste,
     * et on appelle la méthode playSong()
     */
    public void playPreviousSong() {
        this.songIndex--;
        if (this.songIndex < 0) {
            this.songIndex = this.playlistSongs.size() - 1;
        }
        this.playOrPauseSong();
    }


    //public void shuffleSongList(){
    //ArrayList<Song> playlistSongToBeShuffled = new ArrayList<>();

    //playlistSongToBeShuffled.addAll(this.playlistSongs); //copy playListSong in it, try to UNSHUFFLE
    //if(this.isShuffled == false){
    //Collections.shuffle(playlistSongToBeShuffled); //shuffle the copy
    //setPlaylistSongs(playlistSongToBeShuffled); //set the copy
    //  this.isShuffled = true;

    //}else{
    //hos to reset the playlistSong
    //  setPlaylistSongs(this.playlistSongs);   //set the initial playListSong to unshuffle
    //    this.isShuffled = false;                //is not shuffled anymore
    //  }

    //}


    public boolean toShuffle() {
        if (this.isShuffled) {
            this.isShuffled = false;
        } else {
            this.isShuffled = true;
        }
        Log.d("SHUFFLE", "to shuffle " + this.isShuffled);
        return this.isShuffled;
    }

    public void handleSeekBar(int progression, boolean isfromUser) {
        if (player != null && isfromUser) {
            player.seekTo(progression * 1000);
        }
    }


    public int getSongIndex() {
        return songIndex;
    }

    /**
     * Set la chanson courante
     *
     * @param songIndex index de la chanson
     */
    public void setCurrentSong(int songIndex) {
        this.songIndex = songIndex;
    }


    public MediaPlayer getPlayer() {
        return player;
    }

    public void setPlayer(MediaPlayer player) {
        this.player = player;
    }

    public IBinder getSongBinder() {
        return songBinder;
    }

    public ArrayList<Song> getPlaylistSongs() {
        return playlistSongs;
    }

    public void setPlaylistSongs(ArrayList<Song> playlistSongs) {
        this.playlistSongs = playlistSongs;
    }

    public void setToolbarPushed(boolean b) {
        this.isToolbarPushed = b;
    }

    public boolean playerIsPlaying() {
        return this.player.isPlaying();
    }

    public void startPlayBack() {
        this.player.start();
    }

    public void pausePlayback() {
        this.player.pause();
    }

    @Override
    public void onDestroy() {
        this.stopForeground(true);
    }

    public long getSongDuration() {
        return playlistSongs.get(songIndex).getDuration();
    }

    public int getCurrentPositionPlayer() {
        return player.getCurrentPosition();
    }

}
