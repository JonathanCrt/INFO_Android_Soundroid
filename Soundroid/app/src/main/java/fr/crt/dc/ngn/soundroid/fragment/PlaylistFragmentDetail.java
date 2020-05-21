package fr.crt.dc.ngn.soundroid.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.PlaylistAdapter;
import fr.crt.dc.ngn.soundroid.adapter.PlaylistDetailAdapter;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;
import fr.crt.dc.ngn.soundroid.utility.RootList;

public class PlaylistFragmentDetail extends Fragment {

    private SoundroidDatabase soundroidDatabaseInstance;
    private String namePlaylist;
    private FloatingActionButton floatingActionButton;
    private SongService songService;
    private Intent intent;
    private ArrayList<Song> songs;
    private boolean connectionEstablished;
    private ToolbarController toolbarController;
    private ListView lvPlaylistDetail;

    public PlaylistFragmentDetail() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(this.requireContext());
        assert getArguments() != null;
        this.namePlaylist = getArguments().getString("name of playlist");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragmentapp
        View v = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        Thread t = new Thread(()-> {
            Toolbar toolbar = v.findViewById(R.id.detail_toolbar);
            // set the name of the playlist
            toolbar.setTitle(namePlaylist);
            this.songs = (ArrayList<Song>) this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(this.soundroidDatabaseInstance.playlistDao().findPlaylistIdByName(namePlaylist));
            PlaylistDetailAdapter playlistDetailAdapter = new PlaylistDetailAdapter(getContext(), songs);
            this.lvPlaylistDetail = v.findViewById(R.id.list_playlist_detail);
            lvPlaylistDetail.setAdapter(playlistDetailAdapter);
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e("InterruptedException", e.getMessage());
        }

        Toolbar toolbar = v.findViewById(R.id.detail_toolbar);

        this.floatingActionButton = v.findViewById(R.id.fabPlaylistDetailPlayback);
        this.toolbarController = new ToolbarController(getActivity(), requireActivity().findViewById(R.id.crt_layout));

        // set the name of the playlist
        toolbar.setTitle(namePlaylist);

        if(this.songs.size() == 0) {
            Toast.makeText(getContext(), "Cette liste de lecture est vide", Toast.LENGTH_LONG).show();
        }

        // listeners
        this.onClickOnFloatingButton();
        this.installOnItemClickListener();

        return v;
    }

    /**
     * Allow initialization of service's intance when fragment begin
     */
    @Override
    public void onStart() {
        super.onStart();
        this.doBindService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    private void onClickOnFloatingButton() {
        this.floatingActionButton.setOnClickListener(v -> {
            this.setSongServiceAndToolbar(0);
        });
    }

    private void installOnItemClickListener() {
        this.lvPlaylistDetail.setOnItemClickListener((parent, view, position, id) -> {
            this.setSongServiceAndToolbar(position);
        });
    }

    private void setSongServiceAndToolbar(int position) {
        this.songService.setCurrentSong(position);
        this.songService.playOrPauseSong();
        this.toolbarController.setImagePauseFromFragment();
        this.toolbarController.setWidgetsValues();
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
            // Retrieves service
            songService = songBinder.getService();
            // Toggles serving the arraylist
            songService.setPlaylistSongs(songs);
            connectionEstablished = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectionEstablished = false;
        }
    };


    private void doBindService() {
        if (intent == null) {
            intent = new Intent(getContext(), SongService.class);
            requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        requireActivity().startService(intent);
    }

    public void doUnbindService() {
        if (connectionEstablished) {
            requireContext().unbindService(serviceConnection);
            connectionEstablished = false;
        }
        this.requireContext().stopService(intent);
        this.songService = null;
    }
}