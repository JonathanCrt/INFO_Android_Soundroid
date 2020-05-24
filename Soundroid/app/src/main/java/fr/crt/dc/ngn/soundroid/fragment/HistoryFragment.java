package fr.crt.dc.ngn.soundroid.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.History;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

public class HistoryFragment extends Fragment {

    private SoundroidDatabase soundroidDatabaseInstance;

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
        this.launchPlaylistFragmentDetail();

    }

    private void launchPlaylistFragmentDetail(){
        Log.d("PLAYLIST", "HISTORY");
        Bundle arguments = new Bundle();
        arguments.putString("name of playlist", "history");
        PlaylistFragmentDetail playlistFragmentDetail = new PlaylistFragmentDetail();
        playlistFragmentDetail.setArguments(arguments);
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(R.id.nav_host_fragment, playlistFragmentDetail)
                .addToBackStack(null)
                .commit();
    }

    /**
     *
     * @return number of songs in favorite playlist
     */
    private int createPlaylistHistory() throws ExecutionException, InterruptedException {
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

            List<Song> songInHistoryPlaylist= this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistHistoryId);
            for (Song s : songsHistory) {
                Log.i("PLAYLIST History song  ", s.toString());
                this.soundroidDatabaseInstance.junctionDAO().insertSongIntoPlayList(s.getSongId(), playlistHistoryId);
            }
            Log.i("PLAYLIST HISTORY ", soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistHistoryId).toString());
            return soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistHistoryId).size();
        }).get();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }
}
