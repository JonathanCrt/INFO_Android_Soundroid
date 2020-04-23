package fr.crt.dc.ngn.soundroid.helpers;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import fr.crt.dc.ngn.soundroid.MainActivity;
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
            AsyncTask<Void, Song, ArrayList<Song>> task = new CursorAsyncTask(MainActivity.getAppContext(), rootSongs, new CursorAsyncTask.AsyncResponse() {
                @Override
                public ArrayList<Song> processFinish(ArrayList<Song> rootPlaylist) {
                    Log.i("TASK", "ROOOOT  SSIZE " + rootPlaylist.size());
                    rootList = rootPlaylist;
                    RootList.setRootList(rootPlaylist);
                    Log.i("TASK", "ROOOOT  SSIZE " + rootList.size());
                    return rootPlaylist;
                }
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
