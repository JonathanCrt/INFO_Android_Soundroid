package fr.crt.dc.ngn.soundroid.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.model.Song;

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

    private ToolbarController toolbarController;

    // We can put constants to represent actions

    /**
     * Classe qui permet le lien entre le Fragment et le service (instance)
     */
    public class SongBinder extends Binder {
        public SongService getService() {
            return SongService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.songIndex = 0;
        this.player = new MediaPlayer();
        this.player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return songBinder;
    }

    // Appeler lorsque le service est appelée --> startService(...) à chaque fois, on reçoit l'Intent
    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
     */

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
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "Une erreur est survenue lors de la lecture", Toast.LENGTH_SHORT).show();
        this.player.reset();
        return false;
    }

    private void initializeSong() {
        this.player.reset();
        Song currentSong = playlistSongs.get(songIndex);
        long currentSongID = currentSong.getID();
        Log.i("SongService", "Current song to play = " + currentSong);
        Uri songUri = Uri.parse(currentSong.getLink());

        Log.i("SongService", "isPlayByToolbar" + this.isToolbarPushed);
        Log.i("SongService", "mp isPlaying" + this.player.isPlaying());

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
     * Permet de gérer le contrôle suivant
     * On incrémente  l'index de la chanson,
     * vérifier que nous ne sommes pas allés en dehors de la plage de la liste,
     * et on appelle la méthode playSong()
     */
    public void playNextSong() {
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


    @Override
    public void onDestroy() {
        /*
        this.player.stop();
        this.player.release();
         */
        this.stopForeground(true);
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

}
