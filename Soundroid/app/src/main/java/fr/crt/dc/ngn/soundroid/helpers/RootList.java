package fr.crt.dc.ngn.soundroid.helpers;

import android.Manifest;
import android.app.Activity;
import android.os.AsyncTask;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import fr.crt.dc.ngn.soundroid.MainActivity;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.async.CursorAsyncTask;
import fr.crt.dc.ngn.soundroid.model.Playlist;
import fr.crt.dc.ngn.soundroid.model.Song;

public class RootList {
    private static ArrayList<Song> rootList = null;
    private static final Object MUTEX = new Object();
    private static SongAdapter songAdapter;

    public static SongAdapter getSongAdapter() {
        return songAdapter;
    }

    public static void setSongAdapter(SongAdapter songAdapter) {
        RootList.songAdapter = songAdapter;
    }

    public static void callAsyncTask(SongAdapter songAdapter, ArrayList<Song> rootSongs) throws ExecutionException, InterruptedException {
        RootList.setSongAdapter(songAdapter);
        RootList.setRootList(rootSongs);
        synchronized (MUTEX){
            AsyncTask<Void, Song, ArrayList<Song>> task = new CursorAsyncTask(MainActivity.getAppContext(), rootSongs, rootPlaylist -> {
                // Sort the list of songs by alphabetical order
                Collections.sort(rootPlaylist, (a, b) -> { // new Comparator<Song> compare()
                    return a.getTitle().compareTo(b.getTitle());
                });
                RootList.setRootList(rootPlaylist);
                // put in the adapter all the sorted songs
                songAdapter.clear();
                for(Song song: rootPlaylist){
                    songAdapter.add(song);
                }
                Log.i("TASK", "END ASYNC TASK");
                Toast.makeText(MainActivity.getAppContext(), "End of async task: ", Toast.LENGTH_SHORT).show();
                LayoutInflater li = LayoutInflater.from(MainActivity.getAppContext());
                View theview = li.inflate(R.layout.fragment_all_tracks, null);

                TextView t2 = theview.findViewById(R.id.tv_list_number_songs);

                t2.setText("42");
                Log.i("TASK", "COUCOU");
                Log.i("TASK", (String) t2.getText());
                theview.refreshDrawableState();
                theview.invalidate();
                songAdapter.notifyDataSetChanged();

                return rootPlaylist;
            }).execute();
        }
    }

    public static ArrayList<Song> getRootList() {
        return rootList;
    }

    public static void setRootList(ArrayList<Song> rootList) {
        RootList.rootList = rootList;
    }
}
