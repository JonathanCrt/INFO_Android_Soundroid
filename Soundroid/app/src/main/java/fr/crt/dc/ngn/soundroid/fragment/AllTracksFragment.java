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
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    private File songFolder;
    private ArrayList<Song> playlistSongs;
    private ContentResolver contentResolver;
    private Bitmap defaultBitmap;
    private SongService songService;
    private Intent intent;
    private boolean connectionEstablished;
    private boolean isPlaying;
    private boolean isOnBackground;
    private Toolbar toolbar;


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
        this.songFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        this.connectionEstablished = false;
        this.isPlaying = false;
        this.isOnBackground = false;


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


        listViewSongs.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(getContext(), playlistSongs.get(position) + "", Toast.LENGTH_SHORT).show();
            Log.i("click on music", playlistSongs.get(position) + "");
        });


        return v;
    }

    /**
     * on démarre l'instance du service
     * lorsque le fragment commence
     */
    @Override
    public void onStart() {
        Log.d("cycle life of fragment", "i'm inside onStart");
        super.onStart();

    }

    /**
     * permet dès que l'utilisateur revient à l'application après l'avoir mis en background
     * d'interargir avec les commandes,
     * lorsque la lecture elle-même est en pause
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
     * lorsque l'activité n'est plus présentée à l'utilisateur
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
    public void songOnTap() {
        //ConstraintLayout line = v.findViewById(R.id.ctrLay_list);
        //line.setOnClickListener(view -> {
        songService.setCurrentSong(Integer.parseInt(getTag()));
        songService.playOneSong();
        //});
    }

    /**
     * Connexion au service
     * ServiceConnection =  Interface pour gérer l'etat du service
     * Ces méthodes de rappel informeront la classe lorsque l'instance du fragment
     * est connecté avec succès à l'instance du service
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

            //lorsque l'instance débute le fragment,
            // nous créeons l'objet intent, qui si n'existe pas encore, se lie au fragment et démarre
            if (intent == null) {
                intent = new Intent(getContext(), SongService.class);
                songService.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                songService.startService(intent); //demarrage du service;
            }

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

            assert cursor != null;
            while (cursor.moveToNext()) {
                String artist = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String title = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                // all the path of the song : /././.title.mp3
                String data = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                /*
                String style = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Audio.Genres.NAME));
                 */
                long albumId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                int ID = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

                int duration = cursor.getInt(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);

                Log.i("LOG", "album = " + albumArtUri.toString());
                //Log.i("LOG", "style = " + style);

                Bitmap bitmap = null;
                try {
                    //TODO: stock bitmap : if already run through an album can get the existing bitmap and not recalculate
                    bitmap = getBitmapFromURI(albumArtUri);

                } catch (FileNotFoundException e) {
                    Log.i("AllTracksFragment", "No album art");
                    // put the default artwork
                    bitmap = this.defaultBitmap;

                } catch (IOException e) {
                    Log.e("AllTracksFragment", "IOException", e);
                }
                Song song = new Song(ID, title, artist, Long.parseLong(String.valueOf(duration)), bitmap, null, album);
                this.playlistSongs.add(song);
            }
        }
    }
}
