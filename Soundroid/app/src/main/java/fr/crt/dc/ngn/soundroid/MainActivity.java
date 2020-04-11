package fr.crt.dc.ngn.soundroid;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.service.SongService;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private Toolbar toolbar;
    private ImageView artwork;
    private TextView titleSong;
    private TextView artistSong;
    private ImageView ivPlayControl;
    private ImageView ivNextControl;
    private ImageView ivPrevControl;
    private boolean connectionEstablished;
    private SongService songService;
    private Intent intent;
    public static final int TOOLBAR_CONTROLLER_REQUEST_CODE = 1;



    public void initializeViews () {
        this.toolbar = findViewById(R.id.toolbar_player);
        this.artwork = findViewById(R.id.iv_artwork);
        this.titleSong = findViewById(R.id.tv_title_track);
        this.artistSong = findViewById(R.id.tv_artist);
        this.ivPlayControl = findViewById(R.id.iv_control_play);
        this.ivNextControl = findViewById(R.id.iv_control_player_next);
        this.ivPrevControl = findViewById(R.id.iv_control_player_previous);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbarHead = findViewById(R.id.toolbar);
        setSupportActionBar(toolbarHead);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_all_tracks, R.id.nav_playlists, R.id.nav_history, R.id.nav_share, R.id.nav_export, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        Toolbar toolbarPlayer = findViewById(R.id.inc_toolbar_player);
        toolbarPlayer.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "Click on toolbar", Toast.LENGTH_SHORT).show();
            Log.i("MainActivity", "Click on toolbar");
        });
        this.initializeViews();

        this.ivPlayControl.setOnClickListener(v -> {

            Intent toolbarController = new Intent(MainActivity.this, ToolbarController.class);
            startActivityForResult(toolbarController, TOOLBAR_CONTROLLER_REQUEST_CODE);
           // Toast.makeText(getApplicationContext(), "Click on start button", Toast.LENGTH_SHORT).show();
          //  this.pushPlayControl();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Manage play control
     */
    public void pushPlayControl() {
        this.songService.setToolbarPushed(true);
        if(!songService.playOrPauseSong()) {
            Toast.makeText(getApplicationContext(), "State : Pause", Toast.LENGTH_SHORT).show();
            this.ivPlayControl.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_white));
        } else {
            Toast.makeText(getApplicationContext(), "State : Play", Toast.LENGTH_SHORT).show();
            this.ivPlayControl.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white));
        }
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
            connectionEstablished = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectionEstablished = false;
        }
    };


}
