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
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

public class PlaylistFragmentDetail extends Fragment {

    private ListView lvPlaylistDetail;
    private ArrayList<Song> songs;
    private SoundroidDatabase soundroidDatabaseInstance;
    private SongAdapter songAdapter;
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
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        CollapsingToolbarLayout appBarLayout = v.findViewById(R.id.toolbar_layout);
        Toolbar toolbar = v.findViewById(R.id.detail_toolbar);
        appBarLayout.setTitle(namePlaylist);
        appBarLayout.setTitleEnabled(true);
        appBarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.colorAccent));
        appBarLayout.setExpandedTitleColor(getResources().getColor(R.color.colorAccent));

        //toolbar.setTitle("title");
        //toolbar.setTitleTextColor(getResources().getColor(R.color.colorAccent));

        this.lvPlaylistDetail = v.findViewById(R.id.list_playlist_detail);
        this.songs = (ArrayList<Song>) this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(this.soundroidDatabaseInstance.playlistDao().findPlaylistIdByName(namePlaylist));
        Toast.makeText(getContext(), "" + this.songs, Toast.LENGTH_LONG).show();
        this.songAdapter = new SongAdapter(getContext(), songs);
        this.lvPlaylistDetail.setAdapter(songAdapter);
        return v;
    }
}