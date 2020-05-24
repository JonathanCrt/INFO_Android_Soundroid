package fr.crt.dc.ngn.soundroid.database.dao;

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

    @Query("SELECT * FROM History")
    List<Song> getAllHistory();

    @Query("SELECT * FROM History WHERE :songId= FK_songId")
    History getHistorySong(long songId);

    @Query("SELECT * FROM History WHERE dateLastPlayed >= :dateAfter")
    List<History> getAllHistoryAfterDate(Date dateAfter);

    @Query("SELECT * FROM History WHERE dateLastPlayed <= :dateBefore")
    List<History> getAllHistoryBeforeDate(Date dateBefore);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(History history);

    @Update
    void updateHistory(History history);

    @Query("UPDATE History SET nbTimesPlayed = nbTimesPlayed +1 AND dateLastPlayed = :newDate WHERE FK_songId = :songId")
    void updateHistoryBySongId(long songId, Date newDate);

    @Delete
    void deleteHistory(History history);

    @Query("DELETE FROM History")
    void deleteAllHistory();
}
