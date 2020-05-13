package fr.crt.dc.ngn.soundroid.fragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.PlaylistAdapter;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;

public class PlaylistFragment extends Fragment {


    private ImageView ivPlaylistMostPlayed;
    private TextView tvPlaylistMostPlayedCounter;

    private ImageView ivPlaylistFavouritesSongs;
    private TextView tvPlaylistFavouritesSongsCounter;

    private ImageView ivPlaylistSongsWithTag;
    private TextView tvPlaylistSongsWithTagCounter;

    private ImageView ivPlaylistfilter;
    private Button btnAddPlaylist;
    private SoundroidDatabase soundroidDatabaseInstance;
    public static int MAX_SIZE_NAME_PLAYLIST = 45;
    ArrayList<Playlist> playlists;
    private ListView lvPlayLists;
    private PlaylistAdapter playlistAdapter;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    private void initializeViews(View view) {
        this.ivPlaylistMostPlayed = view.findViewById(R.id.iv_playlist_most_played);
        this.tvPlaylistMostPlayedCounter = view.findViewById(R.id.tv_playlist_label_most_played_counter);
        this.ivPlaylistFavouritesSongs = view.findViewById(R.id.iv_playlist_favourites_songs);
        this.tvPlaylistFavouritesSongsCounter = view.findViewById(R.id.tv_playlist_label_favourites_songs_counter);
        this.ivPlaylistSongsWithTag = view.findViewById(R.id.iv_playlist_songs_with_tag);
        this.tvPlaylistSongsWithTagCounter = view.findViewById(R.id.tv_playlist_label_songs_with_tag_counter);
        this.ivPlaylistfilter = view.findViewById(R.id.iv_playlist_filter);
        this.btnAddPlaylist = view.findViewById(R.id.btn_add_playlist);
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
        View v = inflater.inflate(R.layout.fragment_playlist, container, false);
        this.initializeViews(v);

        this.lvPlayLists = v.findViewById(R.id.list_view_custom_playlists);
        this.playlists = (ArrayList<Playlist>) this.soundroidDatabaseInstance.playlistDao().getAllPlayLists();
        this.playlistAdapter = new PlaylistAdapter(getContext(), playlists);
        this.lvPlayLists.setAdapter(this.playlistAdapter);
        this.installOnAddPlaylistButtonListener();
        this.installOnLongItemClickListener();
        return v;
    }

    private void installOnAddPlaylistButtonListener() {
        this.btnAddPlaylist.setOnClickListener(v -> {
            EditText editTextNamePlayList = new EditText(this.getContext());
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getContext());

            alertDialogBuilder.setView(editTextNamePlayList);
            alertDialogBuilder.setIcon(R.drawable.ic_menu_playlists);
            alertDialogBuilder.setTitle("Ajout d'une nouvelle liste de lecture");

            alertDialogBuilder.setPositiveButton("OK", (dialog, whichButton) -> {
                String playlistName = editTextNamePlayList.getText().toString();
                if (playlistName.length() > MAX_SIZE_NAME_PLAYLIST) {
                    new AlertDialog.Builder(this.getContext())
                            .setTitle("Erreur lors de l'ajout de la liste de lecture")
                            .setMessage("Le nom de la liste de lecture est supérieur à 45 caractères ! ")
                            .show();
                } else {
                    this.soundroidDatabaseInstance.playlistDao().insertPlayList(new Playlist(playlistName));
                    Log.d("PlaylistFragment add playlist", this.soundroidDatabaseInstance.playlistDao().getAllPlayLists().toString());
                    this.playlists.clear();
                    this.playlists.addAll(this.soundroidDatabaseInstance.playlistDao().getAllPlayLists());
                    this.playlistAdapter.notifyDataSetChanged();
                }

            }).create();
            AlertDialog ad = alertDialogBuilder.create();
            ad.show();
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));

        });

    }

    private void installOnLongItemClickListener() {

        this.lvPlayLists.setOnItemLongClickListener((arg0, arg1, pos, id) -> {
            Log.d("long clicked","pos: " + pos);
            String res = "long clicked" + " pos: " + pos;
            return true;
        });
    }
}
