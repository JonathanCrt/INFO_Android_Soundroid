package fr.crt.dc.ngn.soundroid.async;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.fragment.AllTracksFragment;
import fr.crt.dc.ngn.soundroid.helpers.RootList;
//import fr.crt.dc.ngn.soundroid.model.Song;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

public class CursorAsyncTask extends AsyncTask<Void, Song, ArrayList<Song>> {

    // you may separate this or combined to caller class.
    public interface AsyncResponse {
        ArrayList<Song> processFinish(ArrayList<Song> rootPlaylist);
    }

    private ContentResolver contentResolver;

    private Bitmap defaultBitmap;
    private HashMap<Long, Bitmap> artworkMap;
    private static final int MAX_ARTWORK_SIZE = 200;

    public AsyncResponse delegate = null;

    private SongAdapter songAdapter;
    private ArrayList<Song> rootSongs;
    private SoundroidDatabase soundroidDatabase;

    /**
     * Constructeur de l'asyncTask.
     * @param context
     */
    public CursorAsyncTask(Context context,  ArrayList<Song> rootSongs, AsyncResponse delegate) {
        this.rootSongs = rootSongs;
        this.songAdapter= RootList.getSongAdapter();
        this.contentResolver = context.getContentResolver();
        // get the default artwork one time
        Bitmap tmp = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.artwork_default);
        this.defaultBitmap = Bitmap.createScaledBitmap(tmp, MAX_ARTWORK_SIZE, MAX_ARTWORK_SIZE, false);
        this.delegate = delegate;
        this.artworkMap = new HashMap<>();
        this.soundroidDatabase = SoundroidDatabase.getInstance(context);

    }

    @Override
    protected ArrayList<Song> doInBackground(Void... voids) {
        // add songs in this treeset to guarantee unicity of songs
        TreeSet<Song> treeSetSong = new TreeSet<>((song1, song2) -> song1.getFootprint().toString().compareTo(song2.getFootprint().toString()));

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // TODO: uri to get the genre
        //Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
        String[] cursor_cols = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                //   MediaStore.Audio.Genres.NAME,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};
        String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        try (Cursor cursor = contentResolver.query(uri,
                cursor_cols, where, null, null)) {
            long idSong = 0L;

            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                int idColumn = cursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media._ID);

                int linkColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.DATA);
                /*
                String style = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Genres.NAME));
                 */
                //Log.i("LOG", "style = " + style);

                do {
                    long thisId = cursor.getLong(idColumn);
                    //Log.i("LOG", "ID SONG " + thisId);
                    //long idSong = cursor.getLong(idColumn);
                    String titleSong = cursor.getString(titleColumn);
                    String artistSong = cursor.getString(artistColumn);
                    long albumId = cursor.getLong(albumIdColumn);
                    String albumSong = cursor.getString(albumColumn);
                    int durationSong = cursor.getInt(durationColumn);
                    String songLink = Uri.parse(cursor.getString(linkColumn)).toString();

                    Bitmap bitmap = null;
                    Uri albumArtUri = null;
                    try {
                        // artwork bitmap already get
                        if(artworkMap.containsKey(albumId)){
                            bitmap = artworkMap.get(albumId);
                        }else{
                            // get the bitmap
                            Uri sArtworkUri = Uri
                                    .parse("content://media/external/audio/albumart");
                            albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                            bitmap = getBitmapFromURI(albumArtUri);
                            this.artworkMap.put(albumId, bitmap);
                        }
                    } catch (FileNotFoundException e) {
                        Log.i("AllTracksFragment", "No album art");
                        // put the default artwork
                        bitmap = this.defaultBitmap;

                    } catch (IOException e) {
                        Log.e("AllTracksFragment", "IOException", e);
                    }

                    try {
                        // Create MD5 Hash
                        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                        digest.update(titleSong.getBytes());
                        byte[] messageDigest = digest.digest();
                        //Song song = new Song(idSong, titleSong, artistSong, Long.parseLong(String.valueOf(durationSong)), albumArtUri.toString(), null, albumSong, songLink, messageDigest.toString());
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        Song song = new Song(idSong, titleSong, artistSong, Long.parseLong(String.valueOf(durationSong)), byteArray, null, albumSong, songLink, messageDigest.toString());
                        Log.i("CursorAsyncTask song:", song.toString());
                        // add in treeset
                        treeSetSong.add(song);
                        // add in rootSongs values of treeset continually to assures that there are unique songs
                        rootSongs.clear();
                        rootSongs.addAll(treeSetSong);
                        idSong++;

                        soundroidDatabase.songDao().insertSong(song);

                        // update adapter
                        publishProgress(song);

                    } catch (NoSuchAlgorithmException e) {
                        e.getMessage();
                    }
                }
                while (cursor.moveToNext());
            }
        }
        return rootSongs;
    }


    @Override
    protected void onProgressUpdate(Song... values) {
        super.onProgressUpdate(values);
        this.songAdapter.add(values[0]);
        Log.i("ASYNC", values[0].toString());
        this.songAdapter.notifyDataSetChanged();
        // update number of songs as things progress
        TextView textView = ( AllTracksFragment.getAppContext()).getActivity().findViewById(R.id.tv_list_number_songs);
        textView.setText(String.valueOf(songAdapter.getCount()));
    }

    /**
     * Get the bitmap from an uri
     *
     * @param albumArtUri
     * @return create a bitmap with an adapted size
     * @throws IOException
     */
    private Bitmap getBitmapFromURI(Uri albumArtUri) throws IOException {
        Bitmap myBitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver, albumArtUri);
        int outWidth;
        int outHeight;
        int inWidth = myBitmap.getWidth();
        int inHeight = myBitmap.getHeight();
        if (inWidth > inHeight) {
            outWidth = MAX_ARTWORK_SIZE;
            outHeight = (inHeight * MAX_ARTWORK_SIZE) / inWidth;
        } else {
            outHeight = MAX_ARTWORK_SIZE;
            outWidth = (inWidth * MAX_ARTWORK_SIZE) / inHeight;
        }
        // filter = false to privilege the performance and not the quality of the image
        return Bitmap.createScaledBitmap(myBitmap, outWidth, outHeight, false);
    }

    @Override
    protected void onPostExecute(ArrayList<Song> result) {
        this.songAdapter.notifyDataSetChanged();
        delegate.processFinish(result);
        // update number of songs at the end
        TextView t = ( AllTracksFragment.getAppContext()).getActivity().findViewById(R.id.tv_list_number_songs);
        t.setText(String.valueOf(result.size()));
    }
}
