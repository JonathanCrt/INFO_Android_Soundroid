package fr.crt.dc.ngn.soundroid.async;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.dao.PlaylistDao;
import fr.crt.dc.ngn.soundroid.database.dao.SongDao;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.fragment.AllTracksFragment;

public class DBAsyncTask extends AsyncTask<Void, Void, PlaylistDao> {

    // you may separate this or combined to caller class.
    public interface AsyncResponse {
        PlaylistDao processFinish(PlaylistDao playlistDao);
    }
    public AsyncResponse delegate = null;

    private final PlaylistDao playlistDao;
    private final SongDao songDao;

    public DBAsyncTask(SoundroidDatabase instance, AsyncResponse delegate) {
        playlistDao = instance.playlistDao();
        songDao = instance.songDao();
        this.delegate = delegate;
    }

    @Override
    protected PlaylistDao doInBackground(Void... voids) {
        playlistDao.deleteAll();
        songDao.deleteAll();
        Playlist playlist = new Playlist("root");
        Log.d("LOG", "playlist ID = " + playlist.getPlaylistId());
        playlistDao.insertPlayList(playlist);
        Log.d("LOG SIZE", String.valueOf(playlistDao.getAllPlayLists().size()));
        return playlistDao;
    }

    @Override
    protected void onPostExecute(PlaylistDao playlistDao) {
        delegate.processFinish(playlistDao);
        Log.i("LOG", "DB END");
    }
}
