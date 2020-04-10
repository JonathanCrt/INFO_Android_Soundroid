package fr.crt.dc.ngn.soundroid.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.model.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;

/**
 * Created by CRETE JONATHAN on 09/04/2020.
 */
public class ToolbarController extends AppCompatActivity  {

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



    public void initializeViews () {
        this.toolbar = findViewById(R.id.toolbar_player);
        this.artwork = findViewById(R.id.iv_artwork);
        this.titleSong = findViewById(R.id.tv_title_track);
        this.artistSong = findViewById(R.id.tv_artist);
        this.ivPlayControl = findViewById(R.id.iv_control_play);
        this.ivNextControl = findViewById(R.id.iv_control_player_next);
        this.ivPrevControl = findViewById(R.id.iv_control_player_previous);
        this.playlistSongs = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initializeViews();
        this.connectionEstablished = false;

        this.ivPlayControl.setOnClickListener (v -> {
            this.pushPlayControl();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (intent == null) {
            intent = new Intent(this, SongService.class);
            Log.i("intent value: ", "" + intent);
            Log.i("serviceCon value: ", "" + serviceConnection);
            this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            Objects.requireNonNull(this).startService(intent); //demarrage du service;
            //songService.startService(intent);
        }
    }

    /**
     * Manage play control
     */
    public void pushPlayControl() {
        //this.songService.playOneSong();
    }

    public void pushNextControl() {
        // TODO : code this method
    }

    public void pushPreviousControl() {
        // TODO : code this method
    }

    /**
     * Connection to service
     * ServiceConnection =  interface to manage the state of the service
     * These callback methods notify the class when the instance of the fragment
     * is successfully connected to the service instance
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

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


}
