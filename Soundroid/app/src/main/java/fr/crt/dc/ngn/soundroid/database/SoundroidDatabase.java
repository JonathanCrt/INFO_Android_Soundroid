package fr.crt.dc.ngn.soundroid.database;

import android.content.ContentValues;
import android.content.Context;
import android.telecom.Call;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import fr.crt.dc.ngn.soundroid.database.dao.PlaylistDao;
import fr.crt.dc.ngn.soundroid.database.dao.SongDao;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

/**
 * Created by CRETE JONATHAN on 01/05/2020.
 */
@Database(entities = {Playlist.class, Song.class}, version = 1, exportSchema = false)
public abstract class SoundroidDatabase extends RoomDatabase {

    // Singleton
    private static SoundroidDatabase DB_INSTANCE;

    private static final String DB_NAME = "Soundroid.db";

    // DAO
    public abstract PlaylistDao playlistDao();

    public abstract SongDao songDao();

    private static SoundroidDatabase getInstance(Context context) {
        Log.d("SoundroidDatabase", "getting database instance");
        if (DB_INSTANCE == null) {
            synchronized (SoundroidDatabase.class) {
                buildDatabase(context);
            }
        }
        return DB_INSTANCE;
    }

    private static SoundroidDatabase buildDatabase(final Context context) {
        Log.d("SoundroidDatabase", "Building database");
        return Room.databaseBuilder(context, SoundroidDatabase.class, DB_NAME).addCallback(new Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                Executors.newSingleThreadExecutor().execute(() -> {
                    getInstance(context).songDao().insertAllSongs(Song.populateData());
                });
            }
        }).build();
    }

    /*

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

    public static Callback prePopulateDB() {
        return new Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                Executors.newSingleThreadExecutor().execute(() -> {
                    getInstance()
                });
            }
        };
    }

     */


}
