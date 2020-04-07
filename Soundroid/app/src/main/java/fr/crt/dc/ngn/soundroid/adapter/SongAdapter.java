package fr.crt.dc.ngn.soundroid.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.model.Playlist;
import fr.crt.dc.ngn.soundroid.model.Song;

/**
 * Created by CRETE JONATHAN on 03/04/2020.
 */
public class SongAdapter extends ArrayAdapter<Song> {

    private Playlist playlist;
    private LayoutInflater songInflater;


    public SongAdapter(Context context, Playlist playlist){
        super(context, 0, playlist.getSongList());
        // Log.i("LOG", "SONG ADAPTER size = " + listSongs.size());
        this.playlist = playlist;
        this.songInflater = LayoutInflater.from(context);
    }

    /*
    @Override
    public int getCount() {
        return playlist.getSongList().size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ConstraintLayout songRowLayout = (ConstraintLayout) songInflater.inflate(R.layout.song_row, parent, false);

        TextView tv_title = songRowLayout.findViewById(R.id.tv__list_title);
        TextView tv_artist = songRowLayout.findViewById(R.id.tv_list_artist);
        TextView tv_duration  =songRowLayout.findViewById(R.id.tv_list_duration);

        ImageView iv_artwork = songRowLayout.findViewById(R.id.iv_artwork);

        // Récupérer la chanson en utilisant l'index de la position
        Song currentSong  = playlist.getSongList().get(position);

        tv_title.setText(currentSong.getTitle());
        tv_artist.setText(currentSong.getArtist());
        tv_duration.setText(Song.convertDuration(currentSong.getDuration()));

        iv_artwork.setImageBitmap(currentSong.getArtwork());


        //Mettre à jour la position comme tag de position qui lancera la bonne musique lorsque l'utilisateur cliquera sur un item de la liste
        songRowLayout.setTag(position);
        return songRowLayout;
    }
}
