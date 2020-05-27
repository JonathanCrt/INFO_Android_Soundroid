package fr.crt.dc.ngn.soundroid.database.relation;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;
import fr.crt.dc.ngn.soundroid.database.entity.JunctionPlaylistSong;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

/**
 * Created by CRETE JONATHAN on 13/05/2020.
 */
public class SongWithPlaylists {
    @Embedded
    public Song song;
    @Relation(
            parentColumn = "songId",
            entityColumn = "playlistId",
            associateBy = @Junction(JunctionPlaylistSong.class)
    )
    public List<Playlist> playlists;

    @Override
    public String toString() {
        return "SongWithPlaylists{" +
                "song=" + song +
                ", playlists=" + playlists +
                '}';
    }
}
