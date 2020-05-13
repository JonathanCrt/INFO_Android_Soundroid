package fr.crt.dc.ngn.soundroid.database.entity;

import androidx.room.Entity;

@Entity(primaryKeys = {"playlistId", "songId"})
public class JunctionPlaylistSong {
    public long playlistId;
    public long songId;

}

