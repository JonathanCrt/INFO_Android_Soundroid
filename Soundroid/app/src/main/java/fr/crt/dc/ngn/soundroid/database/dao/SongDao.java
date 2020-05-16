package fr.crt.dc.ngn.soundroid.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.database.relation.PlaylistWithSongs;
import fr.crt.dc.ngn.soundroid.database.relation.SongWithPlaylists;

/**
 * Created by CRETE JONATHAN on 01/05/2020.
 */
@Dao
public interface SongDao {

    @Query("SELECT * FROM Song")
    List<Song> getAllSongs();

    @Query("SELECT * FROM Song WHERE title LIKE '%'||:title ||'%'")
    Song findByTitle(String title);

    @Query("SELECT * FROM Song WHERE songId= :songId")
    Song findById(long songId);

    @Query("SELECT * FROM Song WHERE artist LIKE '%'||:artist||'%'")
    List<Song> findAllByArtist(String artist);

    @Query("SELECT * FROM Song WHERE style= :style")
    List<Song> findAllByStyle(String style);

    @Query("SELECT * FROM Song WHERE album LIKE '%'||:album ||'%'")
    List<Song> findAllByAlbum(String album);

    @Query("SELECT * FROM Song WHERE rating = :rating")
    List<Song> findAllByRating(int rating);

    @Query("SELECT * FROM Song WHERE tag = :tag")
    Song findByTag(String tag);

    @Query("SELECT * FROM Song WHERE footprint = :footprint")
    Song findByFootprint(String footprint);

    @Query("SELECT rating FROM Song WHERE songId = :songId")
    int findRatingBySongId(long songId);

    @Query("SELECT tag FROM Song WHERE songId = :songId")
    String findTagBySongId(long songId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSong(Song song);

    @Insert
    void insertAllSongs(Song... songs);

    @Update
    void updateSong(Song song);

    @Query("UPDATE Song SET rating = :rating WHERE songId = :songId")
    void updateSongRatingById(int rating, long songId);

    @Query("UPDATE Song SET rating = :rating WHERE title = :title")
    void updateSongRatingByTitle(int rating, String title);

    @Query("UPDATE Song SET tag = :tag WHERE songId = :songId")
    void updateSongTagById(String tag, long songId);

    @Query("UPDATE Song SET tag = :tag WHERE title = :title")
    void updateSongTagByTitle(String tag, String title);

    @Query("UPDATE SONG SET tag = null WHERE songId = :songId")
    void updateSongWithNullTagBySongId(long songId);

    @Delete
    void deleteSong(Song song);

    @Query("DELETE FROM Song")
    void deleteAll();

    @Transaction
    @Query("SELECT * FROM Playlist")
    public List<PlaylistWithSongs> getPlaylistsWithSongs();

    @Transaction
    @Query("SELECT * FROM Song")
    public List<SongWithPlaylists> getSongsWithPlaylists();

}
