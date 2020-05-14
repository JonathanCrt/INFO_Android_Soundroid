package fr.crt.dc.ngn.soundroid.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import androidx.annotation.Nullable;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;

/**
 * Created by CRETE JONATHAN on 11/05/2020.
 */
public class PlaylistAdapter extends ArrayAdapter<Playlist> {

    private LayoutInflater playlistInflater;
    ArrayList<Playlist> playlists;
    ArrayList<Playlist> filteredPlayList;
    SoundroidDatabase soundroidDatabaseInstance = SoundroidDatabase.getInstance(this.getContext());

    public PlaylistAdapter(Context context, List<Playlist> playlists) {
        super(context, 0);
        this.playlists = (ArrayList<Playlist>) playlists;
        this.playlistInflater = LayoutInflater.from(context);
    }

    static class ViewHolder {
        TextView tvPlayListName;
        TextView tvPlaylistCounterSongs;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // holder of the views to be reused.
        ViewHolder mViewHolder;

        if (convertView == null) {
            // create the container ViewHolder
            mViewHolder = new ViewHolder();

            // inflate the views from layout for the new row
            playlistInflater = LayoutInflater.from(parent.getContext());
            convertView = playlistInflater.inflate(R.layout.playlist_row, parent, false);

            // set the view to the ViewHolder.
            mViewHolder.tvPlayListName = convertView.findViewById(R.id.tv_list_playlist_name);
            mViewHolder.tvPlaylistCounterSongs = convertView.findViewById(R.id.tv_list_playlist_songs_counter);
            // save the viewHolder to be reused later.
            convertView.setTag(mViewHolder);
        } else {
            // there is already ViewHolder, reuse it.
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        // Retrieve playlist using position index
        Playlist currentPlaylist = playlists.get(position);

        // now we can set populate the data via the ViewHolder into views
        mViewHolder.tvPlayListName.setText(currentPlaylist.getName());
        Log.d("PlaylistAdapter count", ""  + this.soundroidDatabaseInstance.junctionDAO().countNumberOfSongsByName(currentPlaylist.getName()));
        String numberSongs = String.valueOf(this.soundroidDatabaseInstance.junctionDAO().countNumberOfSongsByName(currentPlaylist.getName()));
        mViewHolder.tvPlaylistCounterSongs.setText(numberSongs + " chansons");

        // Update position as position tag that will start the right song when the user clicks a list item
        return convertView;
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public void add(@Nullable Playlist object) {
        super.add(object);
        playlists.add(object);
    }

    @Nullable
    @Override
    public Playlist getItem(int position) {
        if(playlists.isEmpty() || position > playlists.size()){
            Log.w("PlaylistsAdapter", "List contains 0 elements or position > size list");
            return null;
        }
        return playlists.get(position);
    }

}
