package fr.crt.dc.ngn.soundroid.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import fr.crt.dc.ngn.soundroid.MainActivity;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.model.Playlist;
import fr.crt.dc.ngn.soundroid.model.Song;

/**
 * Created by CRETE JONATHAN on 03/04/2020.
 */
public class SongAdapter extends ArrayAdapter<Song> {

    private Playlist playlist;
    private LayoutInflater songInflater;
    private Context context;

    public SongAdapter(Context context, Playlist playlist) {
        super(context, 0, playlist.getSongList());
        this.playlist = playlist;
        this.songInflater = LayoutInflater.from(context);
        this.context = context;
    }

    static class ViewHolder {
        TextView tv_title;
        TextView tv_artist;
        TextView tv_duration;
        ImageView iv_artwork;

        TextView tv_nbSongs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // holder of the views to be reused.
        ViewHolder mViewHolder;

        if (convertView == null) {
            // create the container ViewHolder
            mViewHolder = new ViewHolder();

            // inflate the views from layout for the new row
            songInflater = LayoutInflater.from(parent.getContext());
            convertView = songInflater.inflate(R.layout.song_row, parent, false);

            // set the view to the ViewHolder.
            mViewHolder.tv_title = convertView.findViewById(R.id.tv_list_title);
            mViewHolder.tv_artist = convertView.findViewById(R.id.tv_list_artist);
            mViewHolder.tv_duration = convertView.findViewById(R.id.tv_list_duration);
            mViewHolder.iv_artwork = convertView.findViewById(R.id.iv_list_artwork);


            //mViewHolder.tv_nbSongs = convertView.findViewById(R.id.tv_list_number_songs);

            //mViewHolder.tv_nbSongs = (TextView) ((Activity)context.getApplicationContext()).findViewById(R.id.text);


            // save the viewHolder to be reused later.
            convertView.setTag(mViewHolder);
        } else {
            // there is already ViewHolder, reuse it.
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        // Retrieve song using position index
        Song currentSong = playlist.getSongList().get(position);

        // now we can set populate the data via the ViewHolder into views
        mViewHolder.tv_title.setText(currentSong.getTitle());
        mViewHolder.tv_artist.setText(currentSong.getArtist());
        mViewHolder.tv_duration.setText(Song.convertDuration(currentSong.getDuration()));
        mViewHolder.iv_artwork.setImageBitmap(currentSong.getArtwork());

        //mViewHolder.tv_nbSongs.setText(playlist.getSongList().size());

        // Update position as position tag that will start the right song when the user clicks a list item
        return convertView;
    }

    @Override
    public int getCount() {
        Log.i("SongAdapter lv size", ""  + playlist.getSongList().size());
        return playlist.getSongList().size();
    }
}
