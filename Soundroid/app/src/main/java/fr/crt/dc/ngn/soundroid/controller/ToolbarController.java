package fr.crt.dc.ngn.soundroid.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Objects;

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

    public ToolbarController(Context context, ImageView img){
        this.context = context;
        this.ivPlayControl = img;
        //  if (intent == null) {
        intent = new Intent(this.context, SongService.class);
        Log.i("intent value: ", "" + intent);
        Log.i("serviceCon value: ", "" + serviceConnection);
        this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Objects.requireNonNull(this.context).startService(intent); //demarrage du service;

        installListener();
        //songService.startService(intent);
    }

    private void installListener(){
        this.ivPlayControl.setOnClickListener(v->{
            pushPlayControl();
        });
    }

    /**
     * Manage play control
     */
    public void pushPlayControl() {
        this.songService.setToolbarPushed(true);
        if(!songService.playOrPauseSong()) {
            Toast.makeText(this.context, "State : Pause", Toast.LENGTH_SHORT).show();
            this.ivPlayControl.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_play_white));
        } else {
            Toast.makeText(this.context, "State : Play", Toast.LENGTH_SHORT).show();
            this.ivPlayControl.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_pause_white));
        }
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
