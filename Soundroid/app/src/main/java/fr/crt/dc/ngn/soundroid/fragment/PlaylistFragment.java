package fr.crt.dc.ngn.soundroid.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import androidx.fragment.app.FragmentTransaction;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.PlaylistAdapter;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

public class PlaylistFragment extends Fragment {


    private TextView tvPlaylistMostPlayedCounter;

    private TextView tvPlaylistFavouritesSongsCounter;

    private TextView tvPlaylistSongsWithTagCounter;
    private ImageView iv_playlist_favourites_songs;

    private ImageView ivPlaylistfilter;
    private Button btnAddPlaylist;
    private ImageView iv_playlist_songs_with_tag;
    private SoundroidDatabase soundroidDatabaseInstance;
    private static int MAX_SIZE_NAME_PLAYLIST = 45;
    private ArrayList<Playlist> playlists;
    private ListView lvPlayLists;
    private PlaylistAdapter playlistAdapter;
    private ImageView iv_playlist_most_played;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    private void initializeViews(View view) {
        this.tvPlaylistMostPlayedCounter = view.findViewById(R.id.tv_playlist_label_most_played_counter);
        this.tvPlaylistFavouritesSongsCounter = view.findViewById(R.id.tv_playlist_label_favourites_songs_counter);
        this.tvPlaylistSongsWithTagCounter = view.findViewById(R.id.tv_playlist_label_songs_with_tag_counter);
        this.btnAddPlaylist = view.findViewById(R.id.btn_add_playlist);
        this.iv_playlist_songs_with_tag = view.findViewById(R.id.iv_playlist_songs_with_tag);
        this.iv_playlist_favourites_songs = view.findViewById(R.id.iv_playlist_favourites_songs);
        this.iv_playlist_most_played = view.findViewById(R.id.iv_playlist_most_played);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(this.requireContext());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_playlist, container, false);
        this.initializeViews(v);

        this.lvPlayLists = v.findViewById(R.id.list_view_custom_playlists);
        this.installListeners();
        int nbSongsInPlaylistWithTag = 0;
        int nbSongsInPlaylistFavoris = 0;
        int nbSongsInPlaylistMostPlayed = 0;
        try {
            nbSongsInPlaylistWithTag = this.createPlaylistWithTag();
            nbSongsInPlaylistFavoris = this.createPlaylistInFavorites();
            nbSongsInPlaylistMostPlayed = this.createPlaylistMostPlayed();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("PlaylistFragment", Objects.requireNonNull(e.getMessage()));
        }
        // Put the number of songs in textview
        this.tvPlaylistSongsWithTagCounter.setText(nbSongsInPlaylistWithTag + " chansons");
        this.tvPlaylistFavouritesSongsCounter.setText(nbSongsInPlaylistFavoris + " chansons");
        this.tvPlaylistMostPlayedCounter.setText(nbSongsInPlaylistMostPlayed + " chansons");

        Thread t = new Thread(()->{
            // Get the playlist created by the user (not automatic playlist displayed)
            this.playlists = (ArrayList<Playlist>) this.soundroidDatabaseInstance.playlistDao().getAllPlayLists().stream().filter(p->!p.isAutomatic()).collect(Collectors.toList());
            this.playlistAdapter = new PlaylistAdapter(getContext(), playlists);
            this.lvPlayLists.setAdapter(this.playlistAdapter);
        });

        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e("InterruptedException", Objects.requireNonNull(e.getMessage()));
        }
        return v;
    }

    private void installListeners(){
        this.installOnAddPlaylistButtonListener();
        this.installOnLongItemClickListener();
        this.installOnItemClickListener();
        this.installPlaylistTagButtonListener();
        this.installPlaylistFavorisButtonListener();
        this.installPlaylistMostPlayedButtonListener();
    }
    /**
     *
     * @return number of songs in playlist with tag
     */
    private int createPlaylistWithTag() throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            List<Song> songWithTag = this.soundroidDatabaseInstance.songDao().getAllSongsWithTag();
            Playlist playlistWithTag = this.soundroidDatabaseInstance.playlistDao().findByName(getString(R.string.tag));
            // No playlist with tag yet
            if (playlistWithTag == null) {
                playlistWithTag = new Playlist(getString(R.string.tag), true);
                long playlistId = this.soundroidDatabaseInstance.playlistDao().insertPlayList(playlistWithTag);
                playlistWithTag.setPlaylistId(playlistId);
            }

            long playlistWithTagId = playlistWithTag.getPlaylistId();
            List<Song> songInPlaylistWithTag = this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistWithTagId);
            for (Song s : songWithTag) {
                // Song not in the playlist with tag yet
                if (!songInPlaylistWithTag.contains(s)) {
                    this.soundroidDatabaseInstance.junctionDAO().insertSongIntoPlayList(s.getSongId(), playlistWithTagId);
                }
            }
            return soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistWithTagId).size();
        }).get();
    }

    private void installPlaylistTagButtonListener(){
        iv_playlist_songs_with_tag.setOnClickListener(v->{
            Log.d("PLAYLIST", "WITH TAG");
            Bundle arguments = new Bundle();
            arguments.putString("name of playlist", getString(R.string.tag));
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
            Playlist playlistFavoris = this.soundroidDatabaseInstance.playlistDao().findByName(getString(R.string.favoris));
            // No favoris playlist yet
            if(playlistFavoris == null) {
                playlistFavoris = new Playlist(getString(R.string.favoris), true);
                long playlistId = this.soundroidDatabaseInstance.playlistDao().insertPlayList(playlistFavoris);
                playlistFavoris.setPlaylistId(playlistId);
            }

            long playlistFavorisId = playlistFavoris.getPlaylistId();
            List<Song> songInFavorites = this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistFavorisId);
            for (Song s : songFavorites) {
                // Song not in the playlist favoris yet
                if(!songInFavorites.contains(s)){
                    this.soundroidDatabaseInstance.junctionDAO().insertSongIntoPlayList(s.getSongId(), playlistFavorisId);
                }
            }
            return soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistFavorisId).size();
        }).get();
    }

    private void installPlaylistFavorisButtonListener(){
        iv_playlist_favourites_songs.setOnClickListener(v->{
            Bundle arguments = new Bundle();
            arguments.putString("name of playlist", getString(R.string.favoris));
            PlaylistFragmentDetail playlistFragmentDetail = new PlaylistFragmentDetail();
            playlistFragmentDetail.setArguments(arguments);
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction
                    .replace(R.id.nav_host_fragment, playlistFragmentDetail)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void installPlaylistMostPlayedButtonListener(){
        iv_playlist_most_played.setOnClickListener(v->{
            Log.d("PLAYLIST", "MOST PLAYED");
            Bundle arguments = new Bundle();
            arguments.putString("name of playlist", getString(R.string.most_played));
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
    private int createPlaylistMostPlayed() throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            List<Song> songsMostPlayed = this.soundroidDatabaseInstance.historyDao().getSongsMostPlayed();
            Playlist playlistMostPlayed = this.soundroidDatabaseInstance.playlistDao().findByName(getString(R.string.most_played));
            // No most_played playlist yet
            if(playlistMostPlayed == null) {
                playlistMostPlayed = new Playlist(getString(R.string.most_played), true);
                long playlistId = this.soundroidDatabaseInstance.playlistDao().insertPlayList(playlistMostPlayed);
                playlistMostPlayed.setPlaylistId(playlistId);
            }

            long playlistMostPlayedId = playlistMostPlayed.getPlaylistId();
            List<Song> songInMostPlayedPlaylist= this.soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistMostPlayedId);
            for (Song s : songsMostPlayed) {
                // Song not in the playlist most_played yet
                if(!songInMostPlayedPlaylist.contains(s)){
                    this.soundroidDatabaseInstance.junctionDAO().insertSongIntoPlayList(s.getSongId(), playlistMostPlayedId);
                }
            }
            return soundroidDatabaseInstance.junctionDAO().findAllSongsByPlaylistId(playlistMostPlayedId).size();
        }).get();
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
                        String correctFormatPlaylistName = playlistName.substring(0, 1).toUpperCase() + playlistName.substring(1);
                        this.soundroidDatabaseInstance.playlistDao().insertPlayList(new Playlist(correctFormatPlaylistName, false));
                        Log.d("PlaylistFragment add playlist", this.soundroidDatabaseInstance.playlistDao().getAllPlayLists().toString());
                        Log.d("PlaylistFragment songs into Playlist", this.soundroidDatabaseInstance.junctionDAO().getPlaylistsWithSongs().toString());
                        this.playlists.clear();
                        this.playlists.addAll(this.soundroidDatabaseInstance.playlistDao().getAllPlayLists().stream().filter(p->!p.isAutomatic()).collect(Collectors.toList()));
                        requireActivity().runOnUiThread(()-> this.playlistAdapter.notifyDataSetChanged());
                    }).start();
                }
            }).create();
            AlertDialog ad = alertDialogBuilder.create();
            ad.show();
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
        });
    }

    /**
     * When the user long click on a plylist to delete it
     */
    private void installOnLongItemClickListener() {
        this.lvPlayLists.setOnItemLongClickListener((arg0, arg1, pos, id) -> {
            Playlist playlistClicked = (Playlist) lvPlayLists.getItemAtPosition(pos);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder
                    .setTitle("Suppression").setMessage("Voulez-vous vraiment supprimer cette playlist ?")
                    .setPositiveButton("OUI", ((dialog, which) -> this.deletePlaylist(playlistClicked)))
                    .setNegativeButton("NON", (dialog, whichButton) -> dialog.dismiss());
            AlertDialog ad = alertDialogBuilder.create();
            ad.show();
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
            ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
            return true;
        });
    }

    /**
     * Remove the playlist clicked
     * @param playlistClicked Playlist object
     */
    private void deletePlaylist(Playlist playlistClicked){
        new Thread(() -> {
            long playlistDeletedId = this.soundroidDatabaseInstance.playlistDao().deletePlaylist(playlistClicked);
            this.soundroidDatabaseInstance.junctionDAO().deleteSongsInPlaylistId(playlistDeletedId);
            this.playlists.remove(playlistClicked);
            requireActivity().runOnUiThread(()-> this.playlistAdapter.notifyDataSetChanged());
        }).start();
    }

    private void installOnItemClickListener() {
        this.lvPlayLists.setOnItemClickListener((arg0, arg1, pos, id) -> {
            Playlist playlistSelected = (Playlist) lvPlayLists.getItemAtPosition(pos);
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
