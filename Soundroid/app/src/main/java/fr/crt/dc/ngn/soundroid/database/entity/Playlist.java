package fr.crt.dc.ngn.soundroid.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by CRETE JONATHAN on 01/05/2020.
 */
@Entity
public class Playlist {

    @PrimaryKey(autoGenerate = true)
    private long playlistId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "isAutomatic")
    private boolean isAutomatic;

    public Playlist() {}

    @Ignore
    public Playlist(String name) {
        this.name = name;
        this.isAutomatic = false;   // playlist made with a search
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAutomatic() {
        return isAutomatic;
    }

    public void setAutomatic(boolean automatic) {
        isAutomatic = automatic;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "playlistId=" + playlistId +
                ", name='" + name + '\'' +
                '}';
    }
}
