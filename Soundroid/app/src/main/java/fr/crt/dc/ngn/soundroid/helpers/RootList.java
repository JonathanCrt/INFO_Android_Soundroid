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

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import fr.crt.dc.ngn.soundroid.MainActivity;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.async.CursorAsyncTask;
import fr.crt.dc.ngn.soundroid.fragment.AllTracksFragment;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

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
                //Log.d("Rootlist before sorting", rootList.toString());
                Collections.sort(rootPlaylist, (a, b) -> a.getTitle().compareTo(b.getTitle()));
                //Log.d("Rootlist after sorting", rootList.toString());
                RootList.setRootList(rootPlaylist);
                // put in the adapter all the sorted songs
                /*
                songAdapter.clear();
                for(Song song: rootPlaylist){
                    songAdapter.add(song);
                }
                 */
                Log.i("TASK", "END ASYNC TASK");
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
