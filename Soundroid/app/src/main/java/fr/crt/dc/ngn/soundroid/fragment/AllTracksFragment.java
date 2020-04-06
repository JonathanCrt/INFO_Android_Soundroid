package fr.crt.dc.ngn.soundroid.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Logger;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.model.Playlist;
import fr.crt.dc.ngn.soundroid.model.Song;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

/**
 * Classe représentant le fragment contenant toutes les pistes
 */
public class AllTracksFragment extends Fragment {

    private File songFolder;
    private ArrayList<Song> playlistSongs;
    private ContentResolver contentResolver;

    public AllTracksFragment() {
        // Required empty public constructor
    }


    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Objects.requireNonNull(this.getContext()), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            if (checkSelfPermission(Objects.requireNonNull(this.getContext()), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Playlist playlist = new Playlist("Root");
        this.playlistSongs = new ArrayList<>();
        this.songFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        this.getMetaDataWithResolver();

        // Permet de trier les données afin que les pistes soient listées par ordre alphabétique
        Collections.sort(this.playlistSongs, (a, b) -> { // new Comparator<Song> compare()

            return a.getTitle().compareTo(b.getTitle());
        });
        // create personal adapter
        playlist.setSongList(this.playlistSongs);
        SongAdapter adapter = new SongAdapter(this.getContext(), playlist);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_all_tracks, container, false);
        ListView listViewSongs = v.findViewById(R.id.list_songs);
        listViewSongs.setAdapter(adapter);
        //this.listViewSongs.setOnClickListener(this);

        return v;
    }

    private Bitmap getBitmapFromURI(Uri albumArtUri) throws IOException {
        Bitmap myBitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver, albumArtUri);
        //bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, false);
        int maxSize = 960;
        int outWidth;
        int outHeight;
        int inWidth = myBitmap.getWidth();
        int inHeight = myBitmap.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        return Bitmap.createScaledBitmap(myBitmap, outWidth, outHeight, false);

    }


    private void getMetaDataWithResolver() {
        this.contentResolver = Objects.requireNonNull(this.getContext()).getContentResolver();
        /*
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
            assert cursor != null;
            Log.i("LOG", "cursor = " + cursor.getCount());
            int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artiste = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int ID = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumID = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

            /*
            Cursor cursorArt = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID + "=?",
                    new String[]{String.valueOf(albumID)},
                    null);

            //Bitmap img = contentResolver.loadThumbnail(uri, Size.parseSize(contentResolver.EXTRA_SIZE), null);

            Uri artworkUri  = Uri.parse("content://media/external/audio/album");
            Uri albumArtUri = ContentUris.withAppendedId(artworkUri, albumID);
            Bitmap bitmap;
            Long albumId = Long.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
            Cursor cursorAlbum = getContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                  new String[]{MediaStore.Audio.Albums._ID, ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=" + albumId, null, null);
            assert cursorAlbum != null;

            int artwork = cursorAlbum.getColumnIndex(ALBUM_ART);
            //int style = cursor.getColumnIndex(MediaStore.Audio.Media.);



            while (cursor.moveToNext()) {
                String myTitle = cursor.getString(title);
                String myArtiste = cursor.getString(artiste);
                String myDuration = cursor.getString(duration);

                /*
                Bitmap bm = null;
                if (cursorArt.moveToNext()) {
                    String path = cursorArt.getString(cursorArt.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    bm = BitmapFactory.decodeFile(path);
                }

                Song song = new Song(ID, myTitle, myArtiste, Long.parseLong(myDuration), null, null, null);
                this.playlistSongs.add(song);
            }
        }
                 */

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] cursor_cols = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};
        String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        try (Cursor cursor = contentResolver.query(uri,
                cursor_cols, where, null, null)) {

            assert cursor != null;
            while (cursor.moveToNext()) {
                String artist = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String track = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String data = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                long albumId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                int ID = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

                int duration = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

                Log.i("LOG", "albumur = " + albumArtUri.toString());

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver, albumArtUri);

                } catch (FileNotFoundException e) {
                    Log.e("AllTracksFragment", "Error no album art", e);
                    // put a default artwork
                    //   bitmap = BitmapFactory.decodeResource(getContext().getResources(),
                    //         R.drawable.artwork_default);

                } catch (IOException e) {
                    Log.e("AllTracksFragment", "IOException", e);
                }
                Song song = new Song(ID, track, artist, Long.parseLong(String.valueOf(duration)), bitmap, null, null);
                this.playlistSongs.add(song);
            }

        }
    }
}
