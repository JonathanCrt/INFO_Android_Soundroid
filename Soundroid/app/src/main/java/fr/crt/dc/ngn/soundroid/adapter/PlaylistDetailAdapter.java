package fr.crt.dc.ngn.soundroid.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.utility.Utility;

/**
 * Created by CRETE JONATHAN on 19/05/2020.
 */
public class PlaylistDetailAdapter extends ArrayAdapter<Song>  {

    private LayoutInflater songInflater;
    private Context context;
    CustomFilter filter;
    private ArrayList<Song> listSongs;

    public PlaylistDetailAdapter(Context context, List<Song> listSongs) {
        super(context, 0);
        this.listSongs = (ArrayList<Song>) listSongs;
        Log.d("SongAdapter", listSongs.toString());
        this.songInflater = LayoutInflater.from(context);
        this.context = context;
    }

    static class ViewHolder {
        TextView tv_index;
        TextView tv_title;
        TextView tv_artist;
        TextView tv_duration;
        ImageView iv_artwork;
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
            convertView = songInflater.inflate(R.layout.playlist_detail_row, parent, false);

            // set the view to the ViewHolder.
            mViewHolder.tv_index = convertView.findViewById(R.id.tv_playlist_detail_index);
            mViewHolder.tv_title = convertView.findViewById(R.id.tv_playlist_detail_title);
            mViewHolder.tv_artist = convertView.findViewById(R.id.tv_playlist_detail_artist);
            mViewHolder.tv_duration = convertView.findViewById(R.id.tv_playlist_detail_duration);
            mViewHolder.iv_artwork = convertView.findViewById(R.id.iv_playlist_detail_artwork);
            // save the viewHolder to be reused later.
            convertView.setTag(mViewHolder);
        } else {
            // there is already ViewHolder, reuse it.
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        // Retrieve song using position index
        Song currentSong = listSongs.get(position);
        //Song currentSong = playlist.getSongList().get(position);

        // now we can set populate the data via the ViewHolder into views
        //int currentPosition = getPosition(currentSong);
        //Log.d("PlaylistDetailAdapter", "crr pos" + currentPosition);
        mViewHolder.tv_index.setText(String.valueOf(position + 1));
        mViewHolder.tv_title.setText(currentSong.getTitle());
        mViewHolder.tv_artist.setText(currentSong.getArtist());
        mViewHolder.tv_duration.setText(Song.convertDuration(currentSong.getDuration()));
        mViewHolder.iv_artwork.setImageBitmap(Utility.convertByteToBitmap(currentSong.getArtwork()));

        // Update position as position tag that will start the right song when the user clicks a list item
        return convertView;
    }

    @Override
    public int getCount() {
        return listSongs.size();
    }


    @Override
    public void add(@Nullable Song object) {
        super.add(object);
       listSongs.add(object);
    }

    @Nullable
    @Override
    public Song getItem(int position) {
        if(listSongs.isEmpty() || position > listSongs.size()){
            Log.w("PlaylistAdapter", "List contains 0 elements or position > size list");
            return null;
        }
        return listSongs.get(position);
    }

}
