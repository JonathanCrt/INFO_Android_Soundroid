package fr.crt.dc.ngn.soundroid.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

/**
 * Created by CRETE JONATHAN on 01/05/2020.
 */
@Dao
public interface SongDao {

    @Query("SELECT * FROM Song")
    List<Song> getAllSongs();

    @Query("SELECT * FROM Song WHERE title= :title")
    Song findByTitle(String title);

    @Query("SELECT * FROM Song WHERE id= :id")
    Song findById(long id);

    @Query("SELECT * FROM Song WHERE artist= :artist")
    List<Song> findAllByArtist(String artist);

    @Query("SELECT * FROM Song WHERE style= :style")
    List<Song> findAllByStyle(String style);

    @Query("SELECT * FROM Song WHERE album = :album")
    List<Song> findAllByAlbum(String album);

    @Query("SELECT * FROM Song WHERE rating = :rating")
    List<Song> findAllByRating(int rating);

    @Query("SELECT * FROM Song WHERE tag = :tag")
    Song findByTag(String tag);

    //String findTagOfSong()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSong(Song song);

    @Insert
    void insertAllSongs(Song... songs);

    @Update
    void updateSong(Song song);

    @Query("UPDATE Song SET rating = :rating WHERE id = :id")
    void updateSongRatingById(int rating, long id);

    @Query("UPDATE Song SET rating = :rating WHERE title = :title")
    void updateSongRatingByTitle(int rating, String title);

    @Query("UPDATE Song SET tag = :tag WHERE id = :id")
    void updateSongTagById(String tag, long id);

    @Query("UPDATE Song SET tag = :tag WHERE title = :title")
    void updateSongTagByTitle(String tag, String title);

    @Delete
    void deleteSong(Song song);
}
