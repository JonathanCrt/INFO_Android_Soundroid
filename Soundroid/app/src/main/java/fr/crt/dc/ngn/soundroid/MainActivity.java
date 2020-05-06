package fr.crt.dc.ngn.soundroid;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase_Impl;
import fr.crt.dc.ngn.soundroid.database.dao.SongDao;
import fr.crt.dc.ngn.soundroid.fragment.AllTracksFragment;
import fr.crt.dc.ngn.soundroid.fragment.PlayerFragment;
import fr.crt.dc.ngn.soundroid.helpers.RootList;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;
import fr.crt.dc.ngn.soundroid.utility.Utility;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;


    public static final int TOOLBAR_CONTROLLER_REQUEST_CODE = 1;
    private boolean isPlayerVisible;

    private static Context context;
    private Intent intent;
    private SongService songService;
    private boolean connectionEstablished;
    private SoundroidDatabase soundroidDatabase;

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.soundroidDatabase = SoundroidDatabase.getInstance(this);

        setContentView(R.layout.activity_main);

        MainActivity.context = getApplicationContext();

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

        Toolbar toolbarPlayer = findViewById(R.id.toolbar_player);

        //launch Player Activity
        toolbarPlayer.setOnClickListener(v -> this.launchPlayerActivity());

        // launch async task
        try {
            //Playlist p = new Playlist("Root");
            ArrayList<Song> listSongs = (ArrayList<Song>) soundroidDatabase.songDao().getAllSongs();
            if(listSongs.isEmpty()){
                // first launch of the app
                Log.i("LOG", "First launch of the app");
            }else{
                /*
               // test to delete all in DB and restart with a new DB clean
                soundroidDatabase.songDao().getAllSongs().forEach(s->{
                    Log.i("LOG", "delete song");
                    soundroidDatabase.songDao().deleteSong(s);
                });
                return;
                */
                Collections.sort(listSongs, (a, b) -> a.getTitle().compareTo(b.getTitle()));
                Log.i("LOG", "already LAUNCHED");
            }
            SongAdapter adapter = new SongAdapter(getAppContext(), listSongs);
            RootList.callAsyncTask(adapter, listSongs);
        } catch (ExecutionException | InterruptedException e) {
            Log.e("MainActivity", Objects.requireNonNull(e.getMessage()));
        }

        //SoundroidDatabase database = SoundroidDatabase.getInstance(this);
        //database.playlistDao().createPlayList(new fr.crt.dc.ngn.soundroid.database.entity.Playlist("poooooooooop"));
        //database.songDao().insertSong(new fr.crt.dc.ngn.soundroid.database.entity.Song(1, "Billie Jean", "Michael Jackson", 2503, "rep/artwork", "pop", "King of pop", "rep/...",  "89+79gs76g"));

        //Log.i("MainActivity SIZE" , "" + database.playlistDao().getAllPlayLists().size());

        /*
        for(int i = 0; i < database.playlistDao().getAllPlayLists().size(); i++) {
            Log.i("increment", " " + i);
            Log.i("MainActivity NAME" , "" + database.playlistDao().getAllPlayLists().get(i).getName());
        }

         */

        //Log.i("MainActivity DB" , "" + database.playlistDao().getAllPlayLists());

    }


    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    public void doBindService() {
        if (intent == null) {
            intent = new Intent(this, SongService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        startService(intent);
    }

    public void doUnbindService() {
        if (connectionEstablished) {
            songService.unbindService(serviceConnection);
            connectionEstablished = false;
        }
        this.stopService(intent);
        this.songService = null;
    }


    private void launchPlayerActivity() {
        Intent it = new Intent(this, PlayerActivity.class);
        it.putExtra("TITLE_SONG", this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getTitle());
        it.putExtra("ARTIST_SONG", this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtist());
        it.putExtra("RATING_SONG", this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getRating());
        it.putExtra("ARTWORK_SONG", Utility.convertByteToBitmap(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork()));
        it.putExtra("DURATION_SONG", this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getDuration());
        startActivity(it);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        item.setOnMenuItemClickListener(l->{
            Toast.makeText(this,"FOUND IT",Toast.LENGTH_SHORT);
            Log.d("SEARCH", "onCreateOptionsMenu: HERE");
            return true;
        });
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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
            //songService.setPlaylistSongs(playlistSongs);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


}
