package fr.crt.dc.ngn.soundroid.model;

import android.util.Log;

import java.util.ArrayList;

public class Playlist {

    private final String name;
    private ArrayList<Song> songList;
    private Search search;
    private boolean isAutomatic;

    public Playlist(String name) {
        this.name = name;
        this.songList = new ArrayList<>();
        this.isAutomatic = false;   // playlist made with a search
    }

    public Playlist(String name, Search search) {
        this.name = name;
        this.songList = new ArrayList<>();
        this.search = search;
        this.isAutomatic = true;
    }


    public String getName() {
        return name;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public Search getSearch() {
        return search;
    }

    public boolean isAutomatic() {
        return isAutomatic;
    }

    public void setSongList(ArrayList<Song> songList) {
        Log.i("LOG", "PLAYLIST size = " + songList.size());
        this.songList = songList;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public void setAutomatic(boolean automatic) {
        isAutomatic = automatic;
    }
}
