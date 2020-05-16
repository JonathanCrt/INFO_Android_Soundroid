package fr.crt.dc.ngn.soundroid.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import fr.crt.dc.ngn.soundroid.async.DBAsyncTask;
import fr.crt.dc.ngn.soundroid.database.dao.JunctionPlaylistSongDao;
import fr.crt.dc.ngn.soundroid.database.dao.PlaylistDao;
import fr.crt.dc.ngn.soundroid.database.dao.SongDao;
import fr.crt.dc.ngn.soundroid.database.entity.JunctionPlaylistSong;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

/**
 * Created by CRETE JONATHAN on 01/05/2020.
 */
@Database(entities = {Playlist.class, Song.class, JunctionPlaylistSong.class}, version = 1)
public abstract class SoundroidDatabase extends RoomDatabase {

    // Singleton
    private static SoundroidDatabase DB_INSTANCE;

    private static final String DB_NAME = "Soundroid.db";

    // DAO
    public abstract PlaylistDao playlistDao();
    public abstract SongDao songDao();
    public abstract JunctionPlaylistSongDao junctionDAO();

    private static final Object MUTEX = new Object();
    private static final Object MUTEX2 = new Object();

    public static SoundroidDatabase getInstance(Context context) {
        Log.d("SoundroidDatabase", "getting database instance");
        DB_INSTANCE = null;
        if (DB_INSTANCE == null) {
            synchronized (SoundroidDatabase.class) {
                if(DB_INSTANCE == null){
                    DB_INSTANCE = buildDatabase(context);
                }
            }
        }
        Log.d("SoundroidDatabase", "" + DB_INSTANCE);
        return DB_INSTANCE;
    }

    private static SoundroidDatabase buildDatabase(final Context context) {
        Log.d("SoundroidDatabase", "Building database");
        synchronized (MUTEX) {
            DB_INSTANCE = Room.databaseBuilder(context, SoundroidDatabase.class, DB_NAME).addCallback(new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);
                    synchronized (MUTEX2) {
                        new DBAsyncTask(DB_INSTANCE, playlistDao -> {
                            Log.d("LOG", "DB OVER");
                            Log.d("LOG DAO SIZE ", String.valueOf(playlistDao.getAllPlayLists().size()));

                            return playlistDao;
                        }).execute();
                    }
                }
            }).allowMainThreadQueries().build();
        }
        return DB_INSTANCE;
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
