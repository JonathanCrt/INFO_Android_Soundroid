package fr.crt.dc.ngn.soundroid;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import fr.crt.dc.ngn.soundroid.model.Song;

public class RootList {
    public RootList(){

    }

    private static ArrayList<Song> rootList;

    public ArrayList<Song> callAsyncTask() throws ExecutionException, InterruptedException {
        CursorAsyncTask c = (CursorAsyncTask) new CursorAsyncTask(MainActivity.getAppContext(), (rootPlaylist) -> {
            Log.i("TASK", "ROOOOT  SSIZE " + rootPlaylist.size());
            this.rootList = rootPlaylist;
            Log.i("TASK", "ROOOOT  SSIZE " + this.rootList.size());
            return rootPlaylist;
        }).execute();

        return c.get();
    }


    public static ArrayList<Song> getRootList() {
        return rootList;
    }

    public static void setRootList(ArrayList<Song> rootList) {
        RootList.rootList = rootList;
    }
}
