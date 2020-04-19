package fr.crt.dc.ngn.soundroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.fragment.AllTracksFragment;
import fr.crt.dc.ngn.soundroid.fragment.PlayerFragment;
import fr.crt.dc.ngn.soundroid.model.Song;
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

    private Button btnPanelPause;
    private Button btnPanelPlay;
    private Button btnPanelNext;
    private Button btnPanelPrevious;

    public static final int TOOLBAR_CONTROLLER_REQUEST_CODE = 1;
    private boolean isPlayerVisible;

    private ArrayList<Song> rootList;

    public void initializeViews() {
        this.toolbar = findViewById(R.id.toolbar_player);
        this.artwork = findViewById(R.id.iv_list_artwork);
        this.titleSong = findViewById(R.id.tv_toolbar_title);
        this.artistSong = findViewById(R.id.tv_toolbar_artist);
        this.ivPlayControl = findViewById(R.id.iv_control_play);
        this.ivNextControl = findViewById(R.id.iv_control_player_next);
        this.ivPrevControl = findViewById(R.id.iv_control_player_previous);

        this.btnPanelPlay = findViewById(R.id.btn_panel_play);
        this.btnPanelPause = findViewById(R.id.btn_panel_pause);
        this.btnPanelNext = findViewById(R.id.btn_panel_next);
        this.btnPanelPrevious = findViewById(R.id.btn_panel_previous);
    }

    private static Context context;


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

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        PlayerFragment playerFragment = new PlayerFragment();
        toolbarPlayer.setOnClickListener(view -> {
            if (!isPlayerVisible) {
                fragmentTransaction.replace(R.id.nav_host_fragment, playerFragment).commit();
                fragmentTransaction.addToBackStack(null);
                //this.switchToPlayer();
                this.isPlayerVisible = true;
            } else {
                isPlayerVisible = false;
            }
            Log.i("MainActivity", "Player is Visible ? " + isPlayerVisible);

        });
        this.initializeViews();

        RootList rl = new RootList();
        ArrayList<Song> rootList = null;
        try {
            rootList = rl.callAsyncTask();
        } catch (ExecutionException e) {
            Log.e("MainActivity", e.getMessage());
        } catch (InterruptedException e) {
            Log.e("MainActivity", e.getMessage());
        }

        RootList.setRootList(rootList);

    }

    private void switchToPlayer() {
        PlayerFragment playerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentByTag("PLAYER_FRAG");
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.detach(Objects.requireNonNull(getSupportFragmentManager().findFragmentByTag("ALL_TRACKS")));
        assert playerFragment != null;
        fragmentTransaction.attach(playerFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();

    }

    /*
    public void toogleFragments() {
        AllTracksFragment allTracksFragment = (AllTracksFragment) getSupportFragmentManager().findFragmentById(R.id.nav_all_tracks);
        PlayerFragment playerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.nav_player_song);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(!isPlayerVisible) {
            fragmentTransaction.detach(allTracksFragment);
            fragmentTransaction.attach(playerFragment);
            fragmentTransaction.addToBackStack(null);
            this.isPlayerVisible = true;
        } else {
            fragmentTransaction.detach(playerFragment);
            fragmentTransaction.attach(allTracksFragment);
            this.isPlayerVisible = false;
        }

        fragmentTransaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();

    }
     */


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
