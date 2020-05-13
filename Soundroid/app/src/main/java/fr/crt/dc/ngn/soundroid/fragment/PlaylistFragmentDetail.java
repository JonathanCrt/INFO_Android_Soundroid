package fr.crt.dc.ngn.soundroid.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Arrays;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.PlaylistAdapter;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

public class PlaylistFragmentDetail extends Fragment {

    private ListView lvPlaylistDetail;
    private ArrayList<Song> songs;
    private SoundroidDatabase soundroidDatabaseInstance;
    private SongAdapter songAdapter;

    public PlaylistFragmentDetail() {
        // Required empty public constructor
    }


    public static PlaylistFragmentDetail newInstance(String param1, String param2) {
        PlaylistFragmentDetail fragment = new PlaylistFragmentDetail();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View v = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        CollapsingToolbarLayout appBarLayout = v.findViewById(R.id.toolbar_layout);

        this.lvPlaylistDetail = v.findViewById(R.id.list_playlist_detail);
        this.songs = (ArrayList<Song>) this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(2);
        Toast.makeText(getContext(), "" + this.songs, Toast.LENGTH_LONG).show();
        this.songAdapter = new SongAdapter(getContext(), songs);
        this.lvPlaylistDetail.setAdapter(songAdapter);
        return v;
    }
}
