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
import io.reactivex.Completable;
import io.reactivex.Maybe;

/**
 * Created by CRETE JONATHAN on 13/05/2020.
 * represents a junction table
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
            "JOIN History " +
            "   ON History.FK_songId = JunctionPlayListSong.songId " +
            "WHERE Playlist.playlistId = :playlistId " +
            "ORDER BY nbTimesPlayed DESC")
    List<Song> findAllSongsByPlaylistId(long playlistId);

    @Query("SELECT COUNT(*) FROM Song " +
            "JOIN JunctionPlayListSong " +
            "   ON JunctionPlayListSong.songId = Song.songId " +
            "JOIN Playlist " +
            "   ON Playlist.playlistId = JunctionPlayListSong.playlistId " +
            "WHERE Playlist.name = :name " +
            "ORDER BY Song.songId")
    int countNumberOfSongsByName(String name);

    @Query("DELETE FROM JunctionPlayListSong WHERE playlistId=:playlistId")
    void deleteSongsInPlaylistId(long playlistId);

}
