package fr.crt.dc.ngn.soundroid.controller;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import androidx.constraintlayout.widget.ConstraintLayout;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.service.SongService;
import fr.crt.dc.ngn.soundroid.utility.Utility;

/**
 * Created by CRETE JONATHAN on 09/04/2020.
 */
public class ToolbarController extends AbstractController {

    private ImageView artwork;
    private TextView tvTitleSong;
    private TextView tvArtistSong;
    private ImageView ivPlayControl;
    private ImageView ivNextControl;
    private ImageView ivPrevControl;
    private boolean connectionEstablished;
    private SongService songService;
    private Context context;
    private ConstraintLayout constraintLayout;

    /**
     * intialize all widgets view of constraint layout
     */
    private void initialization() {
        this.tvTitleSong = (TextView) constraintLayout.getViewById(R.id.tv_toolbar_title);
        this.tvArtistSong = (TextView) constraintLayout.getViewById(R.id.tv_toolbar_artist);
        this.ivPlayControl = (ImageView) constraintLayout.getViewById(R.id.iv_control_play);
        this.ivPrevControl = (ImageView) constraintLayout.getViewById(R.id.iv_control_skip_previous);
        this.ivNextControl = (ImageView) constraintLayout.getViewById(R.id.iv_control_skip_next);
        this.artwork = (ImageView) constraintLayout.getViewById(R.id.iv_toolbar_artwork);
    }

    /**
     * Constructor of ToolbarController
     * @param context current context
     * @param mainActivity Layout of MainActivity
     */
    public ToolbarController(Context context, ConstraintLayout mainActivity) {
        this.context = context;
        this.constraintLayout = mainActivity;
        this.initialization();
        Intent intent = new Intent(this.context, SongService.class);

        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SongService.SongBinder songBinder = (SongService.SongBinder) service;
                // to retrieve service
                songService = songBinder.getService();
                connectionEstablished = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                connectionEstablished = false;
            }
        };
        this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Objects.requireNonNull(this.context).startService(intent); //demarrage du service;
        installListener();
    }

    /**
     * install listeners on push buttons
     */
    private void installListener() {
        this.ivPlayControl.setOnClickListener(v -> pushPlayControl());
        this.ivPrevControl.setOnClickListener(v -> pushPreviousControl());
        this.ivNextControl.setOnClickListener(v -> pushNextControl());
    }


    public void setImagePauseFromFragment() {
        setImagePause(ivPlayControl, context);
    }

    /**
     * set all widgets values (TextView, ImageView...)
     */
    public void setWidgetsValues() {
        setTextSongInformation(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getTitle(), this.tvTitleSong);
        setTextSongInformation(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtist(), this.tvArtistSong);
        setArtworkSong(Utility.convertByteToBitmap(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork()), this.artwork);
        // use SharedPreferences to keep current song
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("current_song_title", this.tvTitleSong.getText().toString());
        editor.putString("current_song_artist", this.tvArtistSong.getText().toString());
        editor.putLong("current_song_id", this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getSongId());
        editor.apply();
    }

    /**
     * Manage play control
     */
    private void pushPlayControl() {
        this.songService.setToolbarPushed(true);
        if (!songService.playOrPauseSong()) {
            this.setImagePlay(ivPlayControl, this.context);
        } else {
            this.setImagePause(ivPlayControl, this.context);
            this.setWidgetsValues();
        }
    }

    /**
     * Manage next song control
     */
    private void pushNextControl() {
        this.songService.playNextSong();
        this.setImagePause(ivPlayControl, context);
        this.setWidgetsValues();
    }

    /**
     * Manage previous song control
     */
    private void pushPreviousControl() {
        this.songService.playPreviousSong();
        this.setImagePause(ivPlayControl, context);
        this.setWidgetsValues();
    }

    /**
     * set play Image  if playback is paused
     */
    public void setPlayImageOnPushed() {
        this.ivPlayControl.setImageDrawable(context.getDrawable(R.drawable.ic_pause_white));
    }
}


