package fr.crt.dc.ngn.soundroid.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

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
        }
        //this.getMetaData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Playlist playlist = new Playlist("Root");
        this.playlistSongs = playlist.getSongList();
        playlist.setSongList(this.playlistSongs);
        this.songFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        this.getMetaDataWithResolver();
        //this.getMetaData();

        Log.i("LOG", "size = " + this.playlistSongs.size());

        // Permet de trier les données afin que les pistes soient listées par ordre alphabétique
        Collections.sort(this.playlistSongs, (a, b) -> { // new Comparator<Song> compare()

            return a.getTitle().compareTo(b.getTitle());
        });
        // create personal adapter
        SongAdapter adapter = new SongAdapter(this.getContext(), playlist);

        View v = inflater.inflate(R.layout.fragment_all_tracks, container, false);
        ListView listViewSongs = v.findViewById(R.id.list_songs);
        listViewSongs.setAdapter(adapter);
        //this.listViewSongs.setOnClickListener(this);

        // Inflate the layout for this fragment
        return v;
    }

    private void getMetaDataWithResolver(){
        ContentResolver contentResolver = Objects.requireNonNull(this.getContext()).getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
            assert cursor != null;
            Log.i("LOG", "cursor = " + cursor.getCount());
            int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artiste = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);


            //Long albumId = Long.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
            //Cursor cursorAlbum = getContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            //      new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
            //    MediaStore.Audio.Albums._ID + "=" + albumId, null, null);

            // int artwork = cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM.)
            //int style = cursor.getColumnIndex(MediaStore.Audio.Media.);

            while (cursor.moveToNext()) {
                String myTitle = cursor.getString(title);
                String myArtiste = cursor.getString(artiste);
                String myDuration = cursor.getString(duration);

                Song song = new Song(myTitle, myArtiste, Long.parseLong(myDuration), null, null, null);
                this.playlistSongs.add(song);
            }
        }




    }

    private void getMetaData() {

        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;


        Context ctx = getContext();
        Log.i("IN FOR", "BEFORE FOR");
        // for each music file
        Log.i("IN FOR", "SONFOLDER = " + Arrays.toString(songFolder.listFiles()));


        File[] files = songFolder.listFiles();

        if (files != null) {
            for (File songFile : files) {
                Log.i("IN FOR", "IN FOR");
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(songFolder + "/" + songFile.getName());

                // get song's data
                String songTitle = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String songArtist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String songDuration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String songAlbum = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                String songStyle = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

                long songLongDuration = Long.parseLong(songDuration);

                //byte[] arrayBytesImg = mediaMetadataRetriever.getEmbeddedPicture();
                //Bitmap songArtwork = BitmapFactory.decodeByteArray(arrayBytesImg, 0, arrayBytesImg.length);

                Song song = new Song(songTitle, songArtist, songLongDuration, null, songAlbum, songStyle);

                // fill playlist
                this.playlistSongs.add(song);
            }

        }

    }
}
