package fr.crt.dc.ngn.soundroid.database.entity;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

import fr.crt.dc.ngn.soundroid.database.entity.JunctionPlaylistSong;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;


public class PlaylistWithSongs {
    @Embedded
    public Playlist playlist;
    @Relation(
            parentColumn = "playlistId",
            entityColumn = "songId",
            associateBy = @Junction(JunctionPlaylistSong.class)
    )
    public List<Song> songs;
}
