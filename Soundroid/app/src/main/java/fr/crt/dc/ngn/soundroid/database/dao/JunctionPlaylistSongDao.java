package fr.crt.dc.ngn.soundroid.database.dao;

import com.google.android.material.circularreveal.CircularRevealHelper;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import fr.crt.dc.ngn.soundroid.database.entity.JunctionPlaylistSong;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.database.relation.PlaylistWithSongs;
import fr.crt.dc.ngn.soundroid.database.relation.SongWithPlaylists;

/**
 * Created by CRETE JONATHAN on 13/05/2020.
 */
@Dao
public interface JunctionPlaylistSongDao {

    @Transaction
    @Query("SELECT * FROM Playlist")
    List<PlaylistWithSongs> getPlaylistsWithSongs();

    @Transaction
    @Query("SELECT * FROM Song")
    List<SongWithPlaylists> getSongsWithPlaylists();

    @Query("INSERT INTO JunctionPlaylistSong(playlistId, songId) VALUES (:playlistId, :songId)")
    void insertSongIntoPlayList(long songId, long playlistId);


    @Query("SELECT * FROM Song " +
            "JOIN JunctionPlayListSong " +
            "   ON JunctionPlayListSong.songId = Song.songId " +
            "JOIN Playlist " +
            "   ON Playlist.playlistId = JunctionPlayListSong.playlistId " +
            "WHERE Playlist.playlistId = :playlistId")
    List<Song> findAllSongsByPlaylistId(long playlistId);
}
