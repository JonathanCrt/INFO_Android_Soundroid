package fr.crt.dc.ngn.soundroid.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;

/**
 * Created by CRETE JONATHAN on 01/05/2020.
 */
@Dao
public interface PlaylistDao {

    @Query("SELECT * FROM Playlist")
    List<Playlist> getAllPlayLists();

    @Query("SELECT * FROM Playlist WHERE name = :name")
    Playlist findByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createPlayList(Playlist playlist);

    @Update
    void updatePlayList(Playlist playlist);

    @Delete
    void deletePlaylist(Playlist playlist);



}
