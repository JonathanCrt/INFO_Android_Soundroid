package fr.crt.dc.ngn.soundroid.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toolbar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.RootList;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.model.Playlist;
import fr.crt.dc.ngn.soundroid.model.Song;
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
    private Toolbar toolbar;
    private ListView lv;

    public AllTracksFragment() {// Required empty public constructor
    }

    /**
     * initialize the fields
     */
    private void initialization() {
        this.connectionEstablished = false;
        this.isOnBackground = false;
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
        Log.i("TASK", "rootlist = " + RootList.getRootList());
        this.playlistSongs = RootList.getRootList();
        Log.i("TASK", "size " +this.playlistSongs.size());
        Playlist playlist = new Playlist("Root");
        // TODO : call this method when the app is launched

        //    this.getMetaDataWithResolver();
        // will sort the data so that the tracks are listed in alphabetical order
        Collections.sort(this.playlistSongs, (a, b) -> { // new Comparator<Song> compare()

            return a.getTitle().compareTo(b.getTitle());
        });
        // create personal adapter
        playlist.setSongList(this.playlistSongs);
        SongAdapter adapter = new SongAdapter(this.getContext(), playlist);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_all_tracks, container, false);

        lv = v.findViewById(R.id.list_songs);
        lv.setAdapter(adapter);

        this.installOnItemClickListener();

        return v;
    }

    private void installOnItemClickListener () {
        ConstraintLayout constraintLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.crt_layout);
        ToolbarController toolbarController = new ToolbarController(getActivity(), constraintLayout);
        lv.setOnItemClickListener((parent, view, position, id) -> {
            this.songService.setCurrentSong(position);
            this.songService.playOrPauseSong();
            toolbarController.setImagePause();
            toolbarController.setWidgetsValues();
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

        if (intent == null) {
            intent = new Intent(getActivity(), SongService.class);
            getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            Objects.requireNonNull(getActivity()).startService(intent); //demarrage du service;
            //songService.startService(intent);
        }
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

        if (connectionEstablished) {
            songService.unbindService(serviceConnection); //destruction connexion
        }
        this.songService.stopService(intent);
        this.songService = null;
        super.onDestroy();
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