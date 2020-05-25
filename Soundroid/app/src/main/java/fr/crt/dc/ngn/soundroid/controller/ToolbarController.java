package fr.crt.dc.ngn.soundroid.controller;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Objects;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.model.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;
import fr.crt.dc.ngn.soundroid.utility.Utility;

/**
 * Created by CRETE JONATHAN on 09/04/2020.
 */
public class ToolbarController extends AbstractController {

    private Toolbar toolbar;
    private ImageView artwork;
    private TextView tvTitleSong;
    private TextView tvArtistSong;
    private TextView tvDuration;
    private ImageView ivPlayControl;
    private ImageView ivNextControl;
    private ImageView ivPrevControl;
    private boolean connectionEstablished;
    private SongService songService;
    private Intent intent;
    public static final int RESULT_OK = 1;
    public static final int TOOLBAR_CONTROLLER_REQUEST_CODE = 1;
    private Context context;
    private ConstraintLayout constraintLayout;

    private static ToolbarController toolbarControllerInstance;

    public ToolbarController getInstanceToolbar() {
        return this;
    }


    public ToolbarController() {
        toolbarControllerInstance = this;
    }

    public static ToolbarController getToolbarControllerInstance() {
        return toolbarControllerInstance;
    }

    private void initialization() {
        this.tvTitleSong = (TextView) constraintLayout.getViewById(R.id.tv_toolbar_title);
        this.tvArtistSong = (TextView) constraintLayout.getViewById(R.id.tv_toolbar_artist);
        this.ivPlayControl = (ImageView) constraintLayout.getViewById(R.id.iv_control_play);
        this.ivPrevControl = (ImageView) constraintLayout.getViewById(R.id.iv_control_skip_previous);
        this.ivNextControl = (ImageView) constraintLayout.getViewById(R.id.iv_control_skip_next);
        this.artwork = (ImageView) constraintLayout.getViewById(R.id.iv_toolbar_artwork);
    }

    public ToolbarController(Context context, ConstraintLayout mainActivity) {
        this.context = context;
        this.constraintLayout = mainActivity;
        this.initialization();
        intent = new Intent(this.context, SongService.class);

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
        this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Objects.requireNonNull(this.context).startService(intent); //demarrage du service;

        installListener();
    }

    private void installListener() {
        this.ivPlayControl.setOnClickListener(v -> pushPlayControl());
        this.ivPrevControl.setOnClickListener(v -> pushPreviousControl());
        this.ivNextControl.setOnClickListener(v -> pushNextControl());
    }

    public void setTextViewSelected() {
        this.tvTitleSong.setSelected(true);
        this.tvArtistSong.setSelected(true);
    }

    public void setImagePauseFromFragment() {
        setImagePause(ivPlayControl, context);
    }

    public void setWidgetsValues() {
        setTextSongInformation(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getTitle(), this.tvTitleSong);
        setTextSongInformation(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtist(), this.tvArtistSong);
        setArtworkSong(Utility.convertByteToBitmap(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork()), this.artwork);
        Log.i("PlayerActivity", "sharedPrefs edited ! ");
        Log.i("PlayerActivity", "sharedPrefs values " + this.tvTitleSong.getText().toString() + " " + this.tvArtistSong.getText().toString());
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
            Toast.makeText(this.context, "State : Pause", Toast.LENGTH_SHORT).show();
            this.setImagePlay(ivPlayControl, this.context);
        } else {
            Toast.makeText(this.context, "State : Play", Toast.LENGTH_SHORT).show();
            this.setImagePause(ivPlayControl, this.context);
            this.setWidgetsValues();
        }
    }

    private void pushNextControl() {
        this.songService.playNextSong();
        this.setImagePause(ivPlayControl, context);
        this.setWidgetsValues();
    }

    private void pushPreviousControl() {
        this.songService.playPreviousSong();
        this.setImagePause(ivPlayControl, context);
        this.setWidgetsValues();
    }

    public void setPlayImageOnPushed() {
        this.ivPlayControl.setImageDrawable(context.getDrawable(R.drawable.ic_pause_white));
    }
}


