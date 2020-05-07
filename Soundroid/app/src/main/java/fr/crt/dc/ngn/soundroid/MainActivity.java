package fr.crt.dc.ngn.soundroid;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.utility.RootList;
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
    private String[] listCriteria;
    private boolean[] checkedItems;
    private ArrayList<Integer> userItems = new ArrayList<>();

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.soundroidDatabase = SoundroidDatabase.getInstance(getApplicationContext());
        Log.i("LOG", this.soundroidDatabase.toString());

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
        //toolbarPlayer.setOnClickListener(v -> this.launchPlayerActivity());

        // launch async task
        try {
            //Playlist p = new Playlist("Root");
            ArrayList<Song> listSongs = (ArrayList<Song>) soundroidDatabase.songDao().getAllSongs();
            Log.d("LOG", String.valueOf(soundroidDatabase.playlistDao().getAllPlayLists().size()));
            if (listSongs.isEmpty()) {
                // first launch of the app
                Log.i("LOG", "First launch of the app");

            } else{
                Log.i("LOG", "already LAUNCHED");
                // test to delete all in DB and restart with a new DB clean
                /*
                this.deleteDatabase("Soundroid.db_");
                soundroidDatabase.clearAllTables();
                soundroidDatabase.songDao().getAllSongs().forEach(s -> {
                    Log.i("LOG", "delete song id : " + s.getId());
                    soundroidDatabase.songDao().deleteSong(s);
                });
                return;
                */
                Collections.sort(listSongs, (a, b) -> a.getTitle().compareTo(b.getTitle()));
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



        this.listCriteria = getResources().getStringArray(R.array.search_criteria);
        this.checkedItems = new boolean[listCriteria.length];

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
        monoSearch(item);
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


    public void monoSearch(MenuItem item){
        item = item.setOnMenuItemClickListener(l->{
            Log.d("SEARCH", "onCreateOptionsMenu: HERE");
            AlertDialog.Builder mBuilder =  new AlertDialog.Builder(this);
            mBuilder.setTitle("search a song");

            final EditText input = new EditText(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            mBuilder.setView(input);
            //String userInput = input.getText().toString();

            mBuilder.setMultiChoiceItems(this.listCriteria, this.checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                    Toast.makeText(MainActivity.this, "criteria selected", Toast.LENGTH_SHORT).show();
                }
            }).show();
            return true;
        });

    }
}
