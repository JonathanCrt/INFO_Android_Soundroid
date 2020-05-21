package fr.crt.dc.ngn.soundroid.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Arrays;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.PlaylistAdapter;
import fr.crt.dc.ngn.soundroid.adapter.PlaylistDetailAdapter;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

public class PlaylistFragmentDetail extends Fragment {

    private SoundroidDatabase soundroidDatabaseInstance;
    private String namePlaylist;

    public PlaylistFragmentDetail() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(this.getContext());

        this.namePlaylist = getArguments().getString("name of playlist");
        Log.d("PlaylistFragmentDetail","name Playlist " + this.namePlaylist);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragmentapp
        View v = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        Toolbar toolbar = v.findViewById(R.id.detail_toolbar);
        // set the name of the playlist
        toolbar.setTitle(namePlaylist);
        ListView lvPlaylistDetail = v.findViewById(R.id.list_playlist_detail);
        ArrayList<Song> songs = (ArrayList<Song>) this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(this.soundroidDatabaseInstance.playlistDao().findPlaylistIdByName(namePlaylist));
        PlaylistDetailAdapter playlistDetailAdapter = new PlaylistDetailAdapter(getContext(), songs);
        lvPlaylistDetail.setAdapter(playlistDetailAdapter);
        return v;
    }
}