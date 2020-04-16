package fr.crt.dc.ngn.soundroid;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import fr.crt.dc.ngn.soundroid.model.Song;

public class RootList {
    private static ArrayList<Song> rootList = null;

    public static ArrayList<Song> callAsyncTask() throws ExecutionException, InterruptedException {
        CursorAsyncTask c = (CursorAsyncTask) new CursorAsyncTask(MainActivity.getAppContext(), (rootPlaylist) -> {
            Log.i("TASK", "ROOOOT  SSIZE " + rootPlaylist.size());
            rootList = rootPlaylist;
            Log.i("TASK", "ROOOOT  SSIZE " + rootList.size());
            return rootPlaylist;
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return c.get();
    }


    public static ArrayList<Song> getRootList() {
        return rootList;
    }

    public static void setRootList(ArrayList<Song> rootList) {
        RootList.rootList = rootList;
    }
}
