package fr.crt.dc.ngn.soundroid.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.fragment.app.FragmentTransaction;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.PlaylistAdapter;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

public class PlaylistFragment extends Fragment {


    private ImageView ivPlaylistMostPlayed;
    private TextView tvPlaylistMostPlayedCounter;

    private ImageView ivPlaylistFavouritesSongs;
    private TextView tvPlaylistFavouritesSongsCounter;

    private ImageView ivPlaylistSongsWithTag;
    private TextView tvPlaylistSongsWithTagCounter;
    private ImageView iv_playlist_favourites_songs;

    private ImageView ivPlaylistfilter;
    private Button btnAddPlaylist;
    private ImageView iv_playlist_songs_with_tag;
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
        this.iv_playlist_songs_with_tag = view.findViewById(R.id.iv_playlist_songs_with_tag);
        this.iv_playlist_favourites_songs = view.findViewById(R.id.iv_playlist_favourites_songs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(this.getContext());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_playlist, container, false);
        this.initializeViews(v);

        this.lvPlayLists = v.findViewById(R.id.list_view_custom_playlists);
        this.installOnAddPlaylistButtonListener();
        this.installOnLongItemClickListener();
        this.installOnItemClickListener();
        this.installPlaylistTagButtonListener();
        this.installPlaylistFavorisButtonListener();
        int nbSongsInPlaylistWithTag = 0;
        // Put the number of songs in playlistWithTag
        int nbSongsInPlaylistFavoris = 0;
        try {
            nbSongsInPlaylistWithTag = this.createPlaylistWithTag();
            nbSongsInPlaylistFavoris = this.createPlaylistInFavorites();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        // Put the number of songs in FavorisPlaylist
        this.tvPlaylistSongsWithTagCounter.setText(nbSongsInPlaylistWithTag + " chansons");
        this.tvPlaylistFavouritesSongsCounter.setText(nbSongsInPlaylistFavoris + " chansons");

        Thread t = new Thread(()->{
            this.playlists = (ArrayList<Playlist>) this.soundroidDatabaseInstance.playlistDao().getAllPlayLists();
            Log.d("PlaylistFragment all songs", this.soundroidDatabaseInstance.songDao().getAllSongs().toString());
            Log.d("PlaylistFragment all playlists", this.soundroidDatabaseInstance.playlistDao().getAllPlayLists().toString());
            Log.d("PlaylistFragment songs into Playlist", this.soundroidDatabaseInstance.junctionDAO().getPlaylistsWithSongs().toString());
            Log.d("PlaylistFragment songs for playlist n°2", this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(2).toString());
            this.playlistAdapter = new PlaylistAdapter(getContext(), playlists);
            this.lvPlayLists.setAdapter(this.playlistAdapter);

        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e("InterruptedException", e.getMessage());
        }
        return v;
    }

    /**
     *
     * @return number of songs in playlist with tag
     */
    private int createPlaylistWithTag() throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            List<Song> songWithTag = this.soundroidDatabaseInstance.songDao().getAllSongsWithTag();
            Playlist playlistWithTag = this.soundroidDatabaseInstance.playlistDao().findByName("tag");
            // No playlist with tag yet
            if (playlistWithTag == null) {
                playlistWithTag = new Playlist("tag");
                this.soundroidDatabaseInstance.playlistDao().insertPlayList(playlistWithTag);
            }

            long playlistWithTagId = playlistWithTag.getPlaylistId();
            List<Song> songInPlaylistWithTag = this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistWithTagId);
            for (Song s : songWithTag) {
                // Song not in the playlist with tag yet
                if (!songInPlaylistWithTag.contains(s)) {
                    this.soundroidDatabaseInstance.junctionDAO().insertSongIntoPlayList(s.getSongId(), playlistWithTagId);
                }
            }
            Log.i("PLAYLIST TAG ", soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistWithTagId).toString());
            return soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistWithTagId).size();
        }).get();
    }

    private void installPlaylistTagButtonListener(){
        iv_playlist_songs_with_tag.setOnClickListener(v->{
            Log.d("PLAYLIST", "WITH TAG");
            Bundle arguments = new Bundle();
            arguments.putString("name of playlist", "tag");
            PlaylistFragmentDetail playlistFragmentDetail = new PlaylistFragmentDetail();
            playlistFragmentDetail.setArguments(arguments);
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction
                    .replace(R.id.nav_host_fragment, playlistFragmentDetail)
                    .addToBackStack(null)
                    .commit();
        });
    }

    /**
     *
     * @return number of songs in favorite playlist
     */
    private int createPlaylistInFavorites() throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            List<Song> songFavorites = this.soundroidDatabaseInstance.songDao().getAllSongsInFavorites();
            Log.i("PLAYLIST FAV ", songFavorites.toString());
            Playlist playlistFavoris = this.soundroidDatabaseInstance.playlistDao().findByName("favoris");
            // No favoris playlist yet
            if(playlistFavoris == null) {
                playlistFavoris = new Playlist("favoris");
                this.soundroidDatabaseInstance.playlistDao().insertPlayList(playlistFavoris);
            }

            long playlistFavorisId = playlistFavoris.getPlaylistId();
            List<Song> songInFavorites = this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistFavorisId);
            for (Song s : songFavorites) {
                // Song not in the playlist favoris yet
                if(!songInFavorites.contains(s)){
                    this.soundroidDatabaseInstance.junctionDAO().insertSongIntoPlayList(s.getSongId(), playlistFavorisId);
                }
            }
            Log.i("PLAYLIST FAVORIS ", soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistFavorisId).toString());
            return soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistFavorisId).size();
        }).get();

    }

    private void installPlaylistFavorisButtonListener(){
        iv_playlist_favourites_songs.setOnClickListener(v->{
            Log.d("PLAYLIST", "FAVORIS");
            Bundle arguments = new Bundle();
            arguments.putString("name of playlist", "favoris");
            PlaylistFragmentDetail playlistFragmentDetail = new PlaylistFragmentDetail();
            playlistFragmentDetail.setArguments(arguments);
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction
                    .replace(R.id.nav_host_fragment, playlistFragmentDetail)
                    .addToBackStack(null)
                    .commit();
        });
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
                    new Thread(()->{
                        this.soundroidDatabaseInstance.playlistDao().insertPlayList(new Playlist(playlistName));
                        Log.d("PlaylistFragment add playlist", this.soundroidDatabaseInstance.playlistDao().getAllPlayLists().toString());
                        Log.d("PlaylistFragment songs into Playlist", this.soundroidDatabaseInstance.junctionDAO().getPlaylistsWithSongs().toString());
                        this.playlists.clear();
                        this.playlists.addAll(this.soundroidDatabaseInstance.playlistDao().getAllPlayLists());
                        getActivity().runOnUiThread(()->{
                            this.playlistAdapter.notifyDataSetChanged();
                        });
                    }).start();
                }

            }).create();
            AlertDialog ad = alertDialogBuilder.create();
            ad.show();
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));

        });

    }

    private void installOnLongItemClickListener() {

        this.lvPlayLists.setOnItemLongClickListener((arg0, arg1, pos, id) -> {
            Log.d("long clicked pos","pos: " + pos);
            Log.d("long clicked id","id: " + id);
            Log.d("long clicked item","item: " + lvPlayLists.getItemAtPosition(pos));

            return true;
        });
    }

    private void installOnItemClickListener() {
        this.lvPlayLists.setOnItemClickListener((arg0, arg1, pos, id) -> {
            Log.d("long clicked pos","pos: " + pos);
            Log.d("long clicked id","id: " + id);
            Log.d("long clicked item","item: " + lvPlayLists.getItemAtPosition(pos));

            Playlist playlistSelected = (Playlist) lvPlayLists.getItemAtPosition(pos);
            Log.d("long clicked playlist","Playlist name: " + playlistSelected.getName());
            Bundle arguments = new Bundle();
            arguments.putString("name of playlist", playlistSelected.getName());

            PlaylistFragmentDetail playlistFragmentDetail = new PlaylistFragmentDetail();
            playlistFragmentDetail.setArguments(arguments);
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction
                    .replace(R.id.nav_host_fragment, playlistFragmentDetail)
                    .addToBackStack(null)
                    .commit();
        });
    }



}
