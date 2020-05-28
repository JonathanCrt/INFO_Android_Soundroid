package fr.crt.dc.ngn.soundroid.fragment;

import androidx.annotation.NonNull;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.HistoryAdapter;
import fr.crt.dc.ngn.soundroid.adapter.PlaylistDetailAdapter;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;

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
    private ArrayAdapter adapter;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment app
        View v;
        // If the playlist to display is the most played songs, we need another adapter to display number of times the songs has been played
        if(namePlaylist.equals(getString(R.string.most_played))){
            v = inflater.inflate(R.layout.fragment_history, container, false);
        }else{
            v = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        }
        Thread t = new Thread(()-> {
            Toolbar toolbar = v.findViewById(R.id.detail_toolbar);
            // set the name of the playlist
            toolbar.setTitle(namePlaylist);
            this.songs = (ArrayList<Song>) this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(this.soundroidDatabaseInstance.playlistDao().findPlaylistIdByName(namePlaylist));

            // If the playlist to display is the most played songs, we need another adapter to display number of times the songs has been played
            if(namePlaylist.equals(getString(R.string.most_played))){
                this.adapter = new HistoryAdapter(getContext(), songs);
                this.lvPlaylistDetail = v.findViewById(R.id.list_history);
                this.floatingActionButton = v.findViewById(R.id.fabHistoryPlayback);
            }else{
                this.adapter = new PlaylistDetailAdapter(getContext(), songs);
                this.lvPlaylistDetail = v.findViewById(R.id.list_playlist_detail);
                this.floatingActionButton = v.findViewById(R.id.fabPlaylistDetailPlayback);
            }

            lvPlaylistDetail.setAdapter(adapter);
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e("PlaylistFragmentDetail", Objects.requireNonNull(e.getMessage()));
        }

        this.toolbarController = new ToolbarController(getActivity(), requireActivity().findViewById(R.id.crt_layout));

        if(this.songs.isEmpty()) {
            Toast.makeText(getContext(), "Cette liste de lecture est vide", Toast.LENGTH_LONG).show();
            return v;   // return, the user cannot do anything
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
        this.floatingActionButton.setOnClickListener(v -> this.setSongServiceAndToolbar(0));
    }

    private void installOnItemClickListener() {
        this.lvPlaylistDetail.setOnItemClickListener((parent, view, position, id) -> {
            this.setSongServiceAndToolbar(position);
            requireActivity().runOnUiThread(()-> this.adapter.notifyDataSetChanged());
        });
    }

    private void setSongServiceAndToolbar(int position) {
        this.songService.setCurrentSongIndex(position);
        this.songService.playOrPauseSong();
        this.toolbarController.setImagePauseFromFragment();
        this.toolbarController.setWidgetsValuesToolbar();
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

    private void doUnbindService() {
        if (connectionEstablished) {
            requireContext().unbindService(serviceConnection);
            connectionEstablished = false;
        }
        this.requireContext().stopService(intent);
        this.songService = null;
    }
}