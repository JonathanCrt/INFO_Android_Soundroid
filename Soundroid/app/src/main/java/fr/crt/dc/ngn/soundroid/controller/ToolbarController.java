package fr.crt.dc.ngn.soundroid.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.Layout;
import android.util.Log;
import android.view.View;
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

/**
 * Created by CRETE JONATHAN on 09/04/2020.
 */
public class ToolbarController  {

    private Toolbar toolbar;
    private ImageView artwork;
    private TextView titleSong;
    private TextView artistSong;
    private ImageView ivPlayControl;
    private ImageView ivNextControl;
    private ImageView ivPrevControl;
    private boolean connectionEstablished;
    private ArrayList<Song> playlistSongs;
    private SongService songService;
    private Intent intent;
    public static final int RESULT_OK = 1;
    public static final int TOOLBAR_CONTROLLER_REQUEST_CODE = 1;
    private Context context;
    private ConstraintLayout constraintLayout;

    private void initialization(){
        this.ivPlayControl = (ImageView) constraintLayout.getViewById(R.id.iv_control_play);
        this.ivPrevControl = (ImageView) constraintLayout.getViewById(R.id.iv_control_skip_previous);
        this.ivNextControl = (ImageView) constraintLayout.getViewById(R.id.iv_control_skip_next);
    }

    public ToolbarController(Context context, ConstraintLayout mainActivity){
        this.context = context;
        this.constraintLayout = mainActivity;
        this.initialization();
        intent = new Intent(this.context, SongService.class);
        /**
         * Connection to service
         * ServiceConnection =  interface to manage the state of the service
         * These callback methods notify the class when the instance of the fragment
         * is successfully connected to the service instance
         */
        // Permet de récupérer le service
        // Permet de passer au service l'ArrayList
        ServiceConnection serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SongService.SongBinder songBinder = (SongService.SongBinder) service;
                // Permet de récupérer le service
                songService = songBinder.getService();
                // Permet de passer au service l'ArrayList
                songService.setPlaylistSongs(playlistSongs);
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

    private void installListener(){
        this.ivPlayControl.setOnClickListener(v->{
            pushPlayControl();
        });
        this.ivPrevControl.setOnClickListener(v->{
            pushPreviousControl();
        });
        this.ivNextControl.setOnClickListener(v->{
            pushNextControl();
        });
    }

    public void setImagePlay(){
        this.ivPlayControl.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_play_white));

    }

    public void setImagePause(){
        this.ivPlayControl.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_pause_white));
    }

    /**
     * Manage play control
     */
    private void pushPlayControl() {
        this.songService.setToolbarPushed(true);
        if(!songService.playOrPauseSong()) {
            Toast.makeText(this.context, "State : Pause", Toast.LENGTH_SHORT).show();
            setImagePlay();
        } else {
            Toast.makeText(this.context, "State : Play", Toast.LENGTH_SHORT).show();
            setImagePause();
        }
    }

    private void pushNextControl() {
        this.songService.playNextSong();
    }

    private void pushPreviousControl() {
        this.songService.playPreviousSong();
    }
}
