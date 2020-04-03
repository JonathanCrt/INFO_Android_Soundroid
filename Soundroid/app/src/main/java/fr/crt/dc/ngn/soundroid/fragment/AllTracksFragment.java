package fr.crt.dc.ngn.soundroid.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
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
    private Playlist playlist;
    private ArrayList<Song> playlistSongs;
    private ListView listViewSongs;

    public AllTracksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllTracksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllTracksFragment newInstance(String param1, String param2) {
        AllTracksFragment fragment = new AllTracksFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(this.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                return;
            }
        }
        this.playlist = new Playlist("Root");
        this.playlistSongs = this.playlist.getSongList();
        this.songFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        this.getMetaData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // create personal adapter
        SongAdapter adapter = new SongAdapter(this.getContext(),this.playlistSongs);
        View v = inflater.inflate(R.layout.fragment_all_tracks, container, false);
        this.listViewSongs = v.findViewById(R.id.list_songs);
        this.listViewSongs.setAdapter(adapter);
        //this.listViewSongs.setOnClickListener(this);

        // Inflate the layout for this fragment
        return v;
    }



    public void getMetaData () {

        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Context ctx = getContext();
        Log.i("IN FOR", "BEFORE FOR");
        // for each music file
        Log.i("IN FOR", "SONFOLDER = " + songFolder.listFiles());
        for(File songFile: songFolder.listFiles()) {
            Log.i("IN FOR", "IN FOR");
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(songFolder + "/" + songFile.getName());

            // get song's data
            String songTitle = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String songArtist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String songDuration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String songAlbum = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String songStyle = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

            long songLongDuration= Long.parseLong(songDuration);

            byte[] arrayBytesImg = mediaMetadataRetriever.getEmbeddedPicture();
            Bitmap songArtwork = BitmapFactory.decodeByteArray(arrayBytesImg, 0, arrayBytesImg.length);

            Song song = new Song(songTitle, songArtist, songLongDuration, songArtwork, songAlbum, songStyle);

            // fill playlist
            this.playlistSongs.add(song);

        }
    }





}
