package fr.crt.dc.ngn.soundroid.model;

import java.util.ArrayList;

public class Playlist {

    private final long idPlaylist;
    private final String name;
    private ArrayList<Song> songList;
    private Search search;
    private boolean isAutomatic;

    public Playlist(long idPlaylist, String name) {
        this.idPlaylist = idPlaylist;
        this.name = name;
        this.songList = new ArrayList<>();
        this.isAutomatic = false;
    }

    public Playlist(long idPlaylist, String name, Search search) {
        this.idPlaylist = idPlaylist;
        this.name = name;
        this.songList = new ArrayList<>();
        this.search = search;
        this.isAutomatic = true;
    }

    public long getIdPlaylist() {
        return idPlaylist;
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
        this.songList = songList;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public void setAutomatic(boolean automatic) {
        isAutomatic = automatic;
    }
}
