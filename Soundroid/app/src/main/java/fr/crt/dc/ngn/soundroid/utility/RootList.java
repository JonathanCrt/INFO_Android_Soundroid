package fr.crt.dc.ngn.soundroid.utility;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import fr.crt.dc.ngn.soundroid.MainActivity;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.async.CursorAsyncTask;
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
            new CursorAsyncTask(MainActivity.getAppContext(), rootSongs, rootPlaylist -> {
                // Sort the list of songs by alphabetical order
                Collections.sort(rootPlaylist, (a, b) -> a.getTitle().compareTo(b.getTitle()));
                RootList.setRootList(rootPlaylist);
                Log.i("TASK", "END ASYNC TASK");
                // update the adapter
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
