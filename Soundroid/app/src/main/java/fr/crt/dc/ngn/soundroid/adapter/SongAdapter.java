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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.utility.Utility;

/**
 * Created by CRETE JONATHAN on 03/04/2020.
 */
public class SongAdapter extends ArrayAdapter<Song>  implements Filterable {

    private LayoutInflater songInflater;
    private Context context;
    ArrayList<Song> filteredPlayList;
    CustomFilter filter;
    private ArrayList<Song> listSongs;

    public SongAdapter(Context context, List<Song> listSongs) {
        super(context, 0);
        this.listSongs = (ArrayList<Song>) listSongs;
        Log.d("SongAdapter", listSongs.toString());
        this.songInflater = LayoutInflater.from(context);
        this.context = context;
        this.filteredPlayList = (ArrayList<Song>) listSongs;
    }

    static class ViewHolder {
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
            convertView = songInflater.inflate(R.layout.song_row, parent, false);

            // set the view to the ViewHolder.
            mViewHolder.tv_title = convertView.findViewById(R.id.tv_list_title);
            mViewHolder.tv_artist = convertView.findViewById(R.id.tv_list_artist);
            mViewHolder.tv_duration = convertView.findViewById(R.id.tv_list_duration);
            mViewHolder.iv_artwork = convertView.findViewById(R.id.iv_list_artwork);
            // save the viewHolder to be reused later.
            convertView.setTag(mViewHolder);
        } else {
            // there is already ViewHolder, reuse it.
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        // Retrieve song using position index
        Song currentSong = filteredPlayList.get(position);
        //Song currentSong = playlist.getSongList().get(position);

        // now we can set populate the data via the ViewHolder into views
        mViewHolder.tv_title.setText(currentSong.getTitle());
        mViewHolder.tv_artist.setText(currentSong.getArtist());
        mViewHolder.tv_duration.setText(Song.convertDuration(currentSong.getDuration()));
        //mViewHolder.iv_artwork.setImageBitmap(currentSong.getArtwork());
        mViewHolder.iv_artwork.setImageBitmap(Utility.convertByteToBitmap(currentSong.getArtwork()));

        // Update position as position tag that will start the right song when the user clicks a list item
        return convertView;
    }

    @Override
    public int getCount() {
        return listSongs.size();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if(filter == null) {
            filter=new CustomFilter(filteredPlayList,this);
        }
        return filter;
    }

    @Override
    public void add(@Nullable Song object) {
        super.add(object);
        filteredPlayList.add(object);
    }

    @Nullable
    @Override
    public Song getItem(int position) {
        if(filteredPlayList.isEmpty() || position > filteredPlayList.size()){
            Log.w("SongAdapter", "List contains 0 elements or position > size list");
            return null;
        }
        return filteredPlayList.get(position);
    }
}
