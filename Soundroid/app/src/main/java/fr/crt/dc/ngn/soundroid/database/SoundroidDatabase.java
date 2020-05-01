package fr.crt.dc.ngn.soundroid.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import fr.crt.dc.ngn.soundroid.database.dao.PlaylistDao;
import fr.crt.dc.ngn.soundroid.database.dao.SongDao;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

/**
 * Created by CRETE JONATHAN on 01/05/2020.
 */
@Database(entities = {Playlist.class, Song.class}, version = 1,  exportSchema = false)
public abstract class SoundroidDatabase extends RoomDatabase {

    // Singleton
    private static SoundroidDatabase DB_INSTANCE;

    private static final String DB_NAME = "Soundroid.db";

    // DAO
    public abstract PlaylistDao playlistDao();
    public abstract SongDao songDao();

    public static SoundroidDatabase getInstance(Context context) {
        if(DB_INSTANCE == null) {
            synchronized (SoundroidDatabase.class) {
                if(DB_INSTANCE == null) {
                    DB_INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SoundroidDatabase.class, DB_NAME).build();
                }
            }
        }
        return DB_INSTANCE;
    }

}
