package fr.crt.dc.ngn.soundroid.database.dao;

import java.util.List;

import androidx.room.Dao;
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

    @Query("SELECT playlistId FROM Playlist WHERE name = :name")
    Long findPlaylistIdByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPlayList(Playlist playlist);

    @Update
    void updatePlayList(Playlist playlist);

    @Query("SELECT playlistId FROM PLAYLIST WHERE name = :name")
    long getIdPlaylistByName(String name);

    @Query("DELETE FROM Playlist WHERE name = :name")
    void deleteOnePlaylistByName(String name);

    default long deletePlaylist(String name){
        long id = getIdPlaylistByName(name);
        deleteOnePlaylistByName(name);
        return id;
    }

    default long deletePlaylist(Playlist playlist){
        long id = getIdPlaylistByName(playlist.getName());
        deleteOnePlaylistByName(playlist.getName());
        return id;
    }

    @Query("DELETE FROM Playlist")
    void deleteAll();


}
