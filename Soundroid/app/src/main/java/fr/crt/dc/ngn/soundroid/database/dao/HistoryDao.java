package fr.crt.dc.ngn.soundroid.database.dao;

import android.util.Log;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

import fr.crt.dc.ngn.soundroid.database.entity.History;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.database.relation.PlaylistWithSongs;
import fr.crt.dc.ngn.soundroid.database.relation.SongWithPlaylists;

/**
 * Created by DA COSTA Mélissa on 24/05/2020.
 */
@Dao
public interface HistoryDao {

    // define the size of the table History
    int MAX_SIZE_HISTORY = 100;
    // define the size of the playlist most played songs
    int MAX_SIZE_MOST_PLAYED = 50;

    @Query("SELECT * FROM History JOIN Song ON FK_songId = songId ORDER BY dateLastPlayed DESC")
    List<Song> getAllHistory();

    @Query("SELECT * FROM History WHERE :songId= FK_songId")
    History getHistoryBySongId(long songId);

    @Query("SELECT * FROM History WHERE dateLastPlayed >= :dateAfter")
    List<History> getAllHistoryAfterDate(long dateAfter);

    @Query("SELECT * FROM History WHERE dateLastPlayed <= :dateBefore")
    List<History> getAllHistoryBeforeDate(long dateBefore);

    @Query("SELECT * FROM History JOIN Song ON FK_songId = songId ORDER BY nbTimesPlayed DESC LIMIT :limit")
    List<Song> getSongsMostPlayed(int limit);

    default List<Song> getSongsMostPlayed(){
        return getSongsMostPlayed(MAX_SIZE_MOST_PLAYED);
    }

    @Query("DELETE FROM History where FK_songId NOT IN (SELECT FK_songId from History ORDER BY dateLastPlayed DESC LIMIT :limit)")
    void limitHistorySize(int limit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(History history);

    default void insertHistory(long idSong){
        // the song has already been inserted in the history
        if(getHistoryBySongId(idSong)!=null){
            // update the history
            updateHistoryBySongId(idSong, System.currentTimeMillis());
            Log.d("HISTORY", " UPDATE HISTORY = " + getHistoryBySongId(idSong).toString());
            return;
        }
        // If the size of the table history is too big
        if(getAllHistory().size() >= MAX_SIZE_HISTORY){
            // delete the oldest songs inserted in the table
            // limit the table to MAX_SIZE_HISTORY -1 rows to allow to add a ong (the row number MAX_SIZE_HISTORY)
            Log.d("HISTORY", " LIMIT HISTORY TABLE");
            limitHistorySize(MAX_SIZE_HISTORY -1);
        }
        History history = new History(idSong);
        insert(history);
    }

    @Query("UPDATE History SET nbTimesPlayed = nbTimesPlayed + 1, dateLastPlayed = :newDate WHERE FK_songId = :songId")
    void updateHistoryBySongId(long songId, long newDate);

    @Delete
    void deleteHistory(History history);

    @Query("DELETE FROM History")
    void deleteAllHistory();

}
