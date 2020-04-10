package fr.crt.dc.ngn.soundroid.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

import fr.crt.dc.ngn.soundroid.MainActivity;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.model.Playlist;
import fr.crt.dc.ngn.soundroid.model.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

/**
 * Classe représentant le fragment contenant toutes les pistes
 */
public class AllTracksFragment extends Fragment {

    private ArrayList<Song> playlistSongs;
    private ContentResolver contentResolver;
    private Bitmap defaultBitmap;
    private SongService songService;
    private Intent intent;
    private boolean connectionEstablished;
    private boolean isOnBackground;
    private Toolbar toolbar;
    private ListView lv;
    private HashMap<Long, Bitmap> artworkMap;



    private static final int MAX_ARTWORK_SIZE = 100;


    public AllTracksFragment() {// Required empty public constructor
    }

    /**
     * initialize the fields
     */
    private void initialization() {
        // get the default artwork one time
        Bitmap tmp = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.artwork_default);
        this.defaultBitmap = Bitmap.createScaledBitmap(tmp, MAX_ARTWORK_SIZE, MAX_ARTWORK_SIZE, false);
        this.playlistSongs = new ArrayList<>();
        this.connectionEstablished = false;
        this.isOnBackground = false;
        this.artworkMap = new HashMap<>();
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialization();

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
        // TODO : call this method when the app is launched
        this.getMetaDataWithResolver();

        // will sort the data so that the tracks are listed in alphabetical order
        Collections.sort(this.playlistSongs, (a, b) -> { // new Comparator<Song> compare()

            return a.getTitle().compareTo(b.getTitle());
        });
        // create personal adapter
        playlist.setSongList(this.playlistSongs);
        SongAdapter adapter = new SongAdapter(this.getContext(), playlist);

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_all_tracks, container, false);

        lv = v.findViewById(R.id.list_songs);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(getContext(), playlistSongs.get(position) + "", Toast.LENGTH_SHORT).show();
            Log.i("position : ", "" + position);

            this.songService.setCurrentSong(position);
            this.songService.playOrPauseSong();
        });

        return v;
    }

    /**
     * Allow initialization of service's intance when fragment begin
     */
    @Override
    public void onStart() {
        Log.d("cycle life of fragment", "i'm inside onStart");
        super.onStart();
        Log.i("intent value: ", "" + intent);

        if (intent == null) {
            intent = new Intent(getActivity(), SongService.class);
            Log.i("intent value: ", "" + intent);
            Log.i("serviceCon value: ", "" + serviceConnection);
            getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            Objects.requireNonNull(getActivity()).startService(intent); //demarrage du service;
            //songService.startService(intent);
        }
    }

    /**
     * Allow once the user returns to the application after you set the background
     * to interact with the controls when the reading itself is paused
     */
    @Override
    public void onPause() {
        Log.d("cycle life of fragment", "i'm inside onPause");
        super.onPause();
        this.isOnBackground = true;
    }

    @Override
    public void onResume() {
        Log.d("cycle life of fragment", "i'm inside onResume");
        super.onResume();
        if (isOnBackground)
            isOnBackground = false;
    }

    /**
     * When the activity is not presented to the user
     */
    @Override
    public void onStop() {
        Log.d("cycle life of fragment", "i'm inside onStop");
        super.onStop();

    }

    @Override
    public void onDestroy() {
        Log.d("cycle life of fragment", "i'm inside onDestroy");
        super.onDestroy();
        if (connectionEstablished) {
            songService.unbindService(serviceConnection); //destruction connexion
        }
        this.songService.stopService(intent);
        this.songService = null;

    }

    public boolean isPlaying() {
        if (songService != null && connectionEstablished) {
            return songService.playerIsPlaying();
        }
        return false;
    }

    private void playNext() {
        songService.playNextSong();
    }

    private void playPrevious() {
        songService.playPreviousSong();
    }


    /**
     * set the song position
     * as a flag for each element of view from the list
     * it is associated with the tag onclick from the layout
     */


    /**
     * Connection to service
     * ServiceConnection =  interface to manage the state of the service
     * These callback methods notify the class when the instance of the fragment
     * is successfully connected to the service instance
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongService.SongBinder songBinder = (SongService.SongBinder) service;
            // Permet de récupérer le service
            songService = songBinder.getService();
            // Permet de passer au service l'ArrayList
            songService.setPlaylistSongs(playlistSongs);
            connectionEstablished = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectionEstablished = false;
        }
    };


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

    /**
     * get all the song's metadata
     * create a cursor instance with contentResolver instance
     * in order to obtain information from audio files
     */
    private void getMetaDataWithResolver() {
        this.contentResolver = Objects.requireNonNull(this.getContext()).getContentResolver();
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
                    Log.i("LOG", "ID SONG " + thisId);
                    //long idSong = cursor.getLong(idColumn);
                    String titleSong = cursor.getString(titleColumn);
                    String artistSong = cursor.getString(artistColumn);
                    long albumId = cursor.getLong(albumIdColumn);
                    String albumSong = cursor.getString(albumColumn);
                    int durationSong = cursor.getInt(durationColumn);
                    String songLink = Uri.parse(cursor.getString(linkColumn)).toString();

                    Bitmap bitmap = null;
                    try {
                        // artwork bitmap already get
                        if(artworkMap.containsKey(albumId)){
                            bitmap = artworkMap.get(albumId);
                        }else{
                            // get the bitmap
                            Uri sArtworkUri = Uri
                                    .parse("content://media/external/audio/albumart");
                            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
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

                    Song song = new Song(idSong, titleSong, artistSong, Long.parseLong(String.valueOf(durationSong)), bitmap, null, albumSong, songLink);
                    this.playlistSongs.add(song);
                    idSong++;
                    //Log.i("PlaylistSongs", "" + playlistSongs);
                    Log.i("Cursor", "" + cursor.getString(0));
                }
                while (cursor.moveToNext());
            }
        }
    }


}


