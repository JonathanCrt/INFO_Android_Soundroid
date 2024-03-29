package fr.crt.dc.ngn.soundroid.async;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.fragment.AllTracksFragment;
import fr.crt.dc.ngn.soundroid.utility.RootList;
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
     * Constructor of asyncTask.
     * @param context current context
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected ArrayList<Song> doInBackground(Void... voids) {
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

            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int linkColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.DATA);

                do {

                    int idColumn = cursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Media._ID);
                    long idSong = cursor.getLong(idColumn);
                    if(soundroidDatabase.songDao().findById(idSong) != null){
                        // song already exist in DB
                        Log.i("LOG", "Song with id = " + idSong + " already exist in DB");
                        cursor.moveToNext();
                        continue;
                    }

                    String titleSong = cursor.getString(titleColumn);

                    // Create MD5 Hash
                    MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                    digest.update(titleSong.getBytes());
                    byte[] messageDigest = digest.digest();

                    String footprint = Arrays.toString(messageDigest);
                    if (soundroidDatabase.songDao().findByFootprint(footprint) != null) {
                        // song already exist in DB (no duplicate song check with the footprint
                        Log.i("LOG", "Song with the same footprint already exist in DB");
                        cursor.moveToNext();
                        continue;
                    }

                    String artistSong = cursor.getString(artistColumn);
                    long albumId = cursor.getLong(albumIdColumn);
                    String albumSong = cursor.getString(albumColumn);
                    int durationSong = cursor.getInt(durationColumn);
                    String songLink = Uri.parse(cursor.getString(linkColumn)).toString();

                    Bitmap bitmap = null;
                    Uri albumArtUri;

                    try {
                        // artwork bitmap already get
                        if(artworkMap.containsKey(albumId)){
                            bitmap = artworkMap.get(albumId);
                        }else{
                            // get the bitmap
                            Uri sArtworkUri = Uri
                                    .parse("content://media/external/audio/albumart");
                            albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                            Log.d("LOGIIII", albumArtUri.toString());
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

                    // Convert bitmap to Byte []
                    ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                    // quality 100 means no compress for max visual quality
                    assert bitmap != null;
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                    Song song = new Song(idSong, titleSong, artistSong, Long.parseLong(String.valueOf(durationSong)), bitmapStream.toByteArray(), null, albumSong, songLink, footprint);
                    // insert song in DB
                    soundroidDatabase.songDao().insertSong(song);
                    Log.i("LOG", "Song with id = " + idSong + " inserted in DB");
                    // update adapter
                    publishProgress(song);
                }
                while (cursor.moveToNext());
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e("CursorAsyncTask", Objects.requireNonNull(e.getMessage()));
        }
        return rootSongs;
    }


    @Override
    protected void onProgressUpdate(Song... values) {
        super.onProgressUpdate(values);
        this.songAdapter.add(values[0]);
        this.songAdapter.notifyDataSetChanged();
        // update number of songs as things progress
        if(( AllTracksFragment.getAppContext()).getActivity() != null){
            TextView textView = ( AllTracksFragment.getAppContext()).getActivity().findViewById(R.id.tv_list_number_songs);
            textView.setText(String.valueOf(songAdapter.getCount()));
        }
    }

    /**
     * Get the bitmap from an uri
     *
     * @param albumArtUri uri of albumart
     * @return create a bitmap with an adapted size
     */
    private Bitmap getBitmapFromURI(Uri albumArtUri) throws IOException {
        Log.d("CursorAsyncTask", "getBitmap");
        Bitmap myBitmap;
        if (Build.VERSION.SDK_INT < 28){
            myBitmap = MediaStore.Images.Media.getBitmap(
                    this.contentResolver,
                    albumArtUri
            );
        } else {
            ImageDecoder.Source source  = ImageDecoder.createSource(this.contentResolver, albumArtUri);
            myBitmap = ImageDecoder.decodeBitmap(source);
        }
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
        if(( AllTracksFragment.getAppContext()).getActivity() != null) {
            TextView t = (AllTracksFragment.getAppContext()).getActivity().findViewById(R.id.tv_list_number_songs);
            t.setText(String.valueOf(result.size()));
        }
    }
}
