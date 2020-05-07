package fr.crt.dc.ngn.soundroid.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.utility.RootList;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

/**
 * Classe représentant le fragment contenant toutes les pistes
 */
public class AllTracksFragment extends Fragment {

    private ArrayList<Song> playlistSongs;
    private SongService songService;
    private Intent intent;
    private boolean connectionEstablished;
    private boolean isOnBackground;
    private TextView tvNumberOfSongs;
    private ListView lv;
    private ConstraintLayout constraintLayout;
    private ToolbarController toolbarController;
    private Button shuffleButton;
    private ImageView ivButtonFilter;
    private SongAdapter adapter;
    private View vSearchButton;

    private static AllTracksFragment context;

    public AllTracksFragment() {// Required empty public constructor
    }


    public static AllTracksFragment getAppContext() {
        return AllTracksFragment.context;
    }

    /**
     * initialize the fields
     */
    private void initialization() {
        this.connectionEstablished = false;
        this.isOnBackground = false;
        this.context = this;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialization();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Objects.requireNonNull(this.getContext()), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            if (checkSelfPermission(Objects.requireNonNull(this.getContext()), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.playlistSongs = RootList.getRootList();
        //Playlist playlist = new Playlist("Root");
        // create personal adapter
        //playlist.setSongList(this.playlistSongs);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_all_tracks, container, false);
        this.tvNumberOfSongs = v.findViewById(R.id.tv_list_number_songs);

        lv = v.findViewById(R.id.list_songs);
        adapter = RootList.getSongAdapter();
        lv.setAdapter(adapter);
        //Log.i("LOG", "" + adapter.getCount());

        this.shuffleButton = v.findViewById(R.id.button2);
        this.toShuffle();

        this.constraintLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.crt_layout);
        this.toolbarController = new ToolbarController(getActivity(), constraintLayout);
        int sizeAdapter = this.lv.getAdapter().getCount();
        this.installOnItemClickListener();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.ivButtonFilter = view.findViewById(R.id.iv_list_filter);
        ivButtonFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this.getContext(), ivButtonFilter);
            popup.getMenuInflater().inflate(R.menu.popup_filter, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                Toast.makeText(this.getContext(), "You clicked on : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                Log.d("AllTracksFragment item", " " + item.getTitle());
                adapter.getFilter().filter(item.getTitle());
                return true;
            });
            popup.show(); //showing popup menu

        });

    }

    private void installOnItemClickListener () {

        lv.setOnItemClickListener((parent, view, position, id) -> {
            this.songService.setCurrentSong(position);
            this.songService.playOrPauseSong();
            this.toolbarController.setImagePauseFromFragment();
            this.toolbarController.setWidgetsValues();
        });
    }


    /**
     * Allow initialization of service's intance when fragment begin
     */
    @Override
    public void onStart() {
        Log.d("cycle life of fragment", "i'm inside onStart");
        super.onStart();
        Log.i("intent value: ", "" + intent);
        this.doBindService();
        Toast.makeText(this.getContext(), "PlayList size: " + RootList.getRootList().size(), Toast.LENGTH_SHORT).show();
        //this.tvNumberOfSongs.setText(String.valueOf(RootList.getRootList().size()));

    }

    /**
     * Allow once the user returns to the application after you set the background
     * to interact with the controls when the reading itself is paused
     */
    @Override
    public void onPause() {
        Log.d("cycle life of fragment", "i'm inside onPause");
        super.onPause();
        this.isOnBackground = true;
    }

    @Override
    public void onResume() {
        Log.d("cycle life of fragment", "i'm inside onResume");
        super.onResume();
        if (isOnBackground)
            isOnBackground = false;
    }

    /**
     * When the activity is not presented to the user
     */
    @Override
    public void onStop() {
        Log.d("cycle life of fragment", "i'm inside onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d("cycle life of fragment", "i'm inside onDestroy");
        this.toolbarController = null;
        this.doUnbindService();
        super.onDestroy();
    }


    private void doBindService() {
        if(intent == null) {
            intent = new Intent(getContext(), SongService.class);
            Objects.requireNonNull(getContext()).bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        Objects.requireNonNull(getActivity()).startService(intent);
    }

    private void doUnbindService() {
        if (connectionEstablished) {
            songService.unbindService(serviceConnection);
            connectionEstablished = false;
        }
        this.songService.stopService(intent);
        this.songService = null;
    }

    public boolean isPlaying() {
        if (songService != null && connectionEstablished) {
            return songService.playerIsPlaying();
        }
        return false;
    }

    private void playNext() {
        songService.playNextSong();
    }

    private void playPrevious() {
        songService.playPreviousSong();
    }

    private void toShuffle(){
        this.shuffleButton.setOnClickListener(e->{
            this.songService.toShuffle();
            this.songService.playOrPauseSong();
            this.toolbarController.setWidgetsValues();
        });
    }
    /**
     * set the song position
     * as a flag for each element of view from the list
     * it is associated with the tag onclick from the layout
     */


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