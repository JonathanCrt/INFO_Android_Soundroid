package fr.crt.dc.ngn.soundroid.fragment;

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
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.HistoryAdapter;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.History;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;

public class HistoryFragment extends Fragment {

    private SoundroidDatabase soundroidDatabaseInstance;
    private FloatingActionButton floatingActionButton;
    private SongService songService;
    private Intent intent;
    private ArrayList<Song> historySongs;
    private boolean connectionEstablished;
    private ToolbarController toolbarController;
    private ListView lvHistory;
    private HistoryAdapter historyAdapter;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(this.getContext());

        try {
            this.createPlaylistHistory();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("PlaylistFragment", e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment app
        View v = inflater.inflate(R.layout.fragment_history, container, false);
        Thread t = new Thread(()-> {
            Toolbar toolbar = v.findViewById(R.id.detail_toolbar);
            // set the name of the playlist
            toolbar.setTitle("Historique");
            try {
                this.historySongs = (ArrayList<Song>) this.createPlaylistHistory();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("HistoryFragment", e.getMessage());
            }
            this.historyAdapter = new HistoryAdapter(getContext(), historySongs);
            this.lvHistory = v.findViewById(R.id.list_history);
            lvHistory.setAdapter(historyAdapter);
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e("HistoryFragment", e.getMessage());
        }
        this.floatingActionButton = v.findViewById(R.id.fabHistoryPlayback);
        this.toolbarController = new ToolbarController(getActivity(), requireActivity().findViewById(R.id.crt_layout));

        if(this.historySongs.isEmpty()) {
            Toast.makeText(getContext(), "Cette liste de lecture est vide", Toast.LENGTH_LONG).show();
            return v;   // return, the user cannot do anything
        }

        // listeners
        this.onClickOnFloatingButton();
        this.installOnItemClickListener();
        return v;
    }

    /**
     *
     * @return number of songs in favorite playlist
     */
    private List<Song> createPlaylistHistory() throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {

            long historyPlaylistPreviousId = this.soundroidDatabaseInstance.playlistDao().deletePlaylist("history");
            this.soundroidDatabaseInstance.junctionDAO().deleteSongsInPlaylistId(historyPlaylistPreviousId);

            List<History> h =  this.soundroidDatabaseInstance.historyDao().getAllHistoryH();
            Log.i("PLAYLIST History H= ", h.toString());

            List<Song> songsHistory = this.soundroidDatabaseInstance.historyDao().getAllHistory();
            Log.i("PLAYLIST History ", songsHistory.toString());
            Playlist playlistHistory = new Playlist("history", true);
            long playlistHistoryId = this.soundroidDatabaseInstance.playlistDao().insertPlayList(playlistHistory);
            playlistHistory.setPlaylistId(playlistHistoryId);
            for (Song s : songsHistory) {
                Log.i("PLAYLIST History song  ", s.toString());
                this.soundroidDatabaseInstance.junctionDAO().insertSongIntoPlayList(s.getSongId(), playlistHistoryId);
            }
            Log.i("PLAYLIST HISTORY ", soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistHistoryId).toString());
            return songsHistory;
        }).get();
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
        this.lvHistory.setOnItemClickListener((parent, view, position, id) -> {
            this.setSongServiceAndToolbar(position);
            getActivity().runOnUiThread(()->this.historyAdapter.notifyDataSetChanged());
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
            songService.setPlaylistSongs(historySongs);
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
