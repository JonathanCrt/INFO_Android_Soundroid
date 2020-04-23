package fr.crt.dc.ngn.soundroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
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
import fr.crt.dc.ngn.soundroid.fragment.PlayerFragment;
import fr.crt.dc.ngn.soundroid.helpers.RootList;
import fr.crt.dc.ngn.soundroid.model.Playlist;
import fr.crt.dc.ngn.soundroid.model.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private Button btnPanelPause;
    private Button btnPanelPlay;
    private Button btnPanelNext;
    private Button btnPanelPrevious;

    public static final int TOOLBAR_CONTROLLER_REQUEST_CODE = 1;
    private boolean isPlayerVisible;

    private static Context context;

    public void initializeViews() {
        this.btnPanelPlay = findViewById(R.id.btn_panel_play);
        this.btnPanelPause = findViewById(R.id.btn_panel_pause);
        this.btnPanelNext = findViewById(R.id.btn_panel_next);
        this.btnPanelPrevious = findViewById(R.id.btn_panel_previous);
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        Toolbar toolbarPlayer = findViewById(R.id.inc_toolbar_player);

        toolbarPlayer.setOnClickListener(v -> this.launchPlayerActivity());
        this.initializeViews();

        // launch async task
        try {
            Playlist p = new Playlist("Root");
            SongAdapter adapter = new SongAdapter(getAppContext(), p);
            //p.setSongList(RootList.getRootList());
            RootList.callAsyncTask(adapter, new ArrayList<Song>());

            //Log.i("TASK", "ROOOOT  SSIZE " + RootList.getRootList().size());
        } catch (ExecutionException | InterruptedException e) {
            Log.e("MainActivity", Objects.requireNonNull(e.getMessage()));
        }

    }

    private void launchPlayerActivity() {
        Intent it = new Intent(this, PlayerActivity.class);
        startActivity(it);
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
}
