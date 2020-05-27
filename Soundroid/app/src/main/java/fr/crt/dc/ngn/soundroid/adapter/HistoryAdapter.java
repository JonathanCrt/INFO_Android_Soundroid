package fr.crt.dc.ngn.soundroid.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.Nullable;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.utility.Utility;

/**
 * Created by CRETE JONATHAN on 25/05/2020.
 */
public class HistoryAdapter extends ArrayAdapter<Song> {

    private LayoutInflater historyInflater;
    private ArrayList<Song> songs;
    private SoundroidDatabase soundroidDatabaseInstance;

    public HistoryAdapter(Context context, List<Song> songs) {
        super(context, 0);
        this.songs = (ArrayList<Song>) songs;
        this.historyInflater = LayoutInflater.from(context);
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(context);
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvArtist;
        TextView tvCounterPlayback;
        ImageView ivArtwork;
    }

    /**
     * get the view that display our listView
     * @param position position of item
     * @param convertView reused View
     * @param parent viewGroup parent
     * @return new View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // holder of the views to be reused.
        ViewHolder mViewHolder;
        if (convertView == null) {
            // create the container ViewHolder
            mViewHolder = new ViewHolder();

            // inflate the views from layout for the new row
            historyInflater = LayoutInflater.from(parent.getContext());
            convertView = historyInflater.inflate(R.layout.history_row, parent, false);

            // set the view to the ViewHolder.
            mViewHolder.tvTitle = convertView.findViewById(R.id.tv_history_title);
            mViewHolder.tvArtist = convertView.findViewById(R.id.tv_history_artist);
            mViewHolder.tvCounterPlayback = convertView.findViewById(R.id.tv_history_counter);
            mViewHolder.ivArtwork = convertView.findViewById(R.id.iv_history_artwork);
            // save the viewHolder to be reused later.
            convertView.setTag(mViewHolder);
        } else {
            // there is already ViewHolder, reuse it.
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        // Retrieve song using position index
        Song currentSong = songs.get(position);

        // now we can set populate the data via the ViewHolder into views
        mViewHolder.tvTitle.setText(currentSong.getTitle());
        mViewHolder.tvArtist.setText(currentSong.getArtist());
        mViewHolder.tvCounterPlayback.setText(String.valueOf(getCountSongPlayed(currentSong.getSongId())));
        mViewHolder.ivArtwork.setImageBitmap(Utility.convertByteToBitmap(currentSong.getArtwork()));

        // Update position as position tag that will start the right song when the user clicks a list item
        return convertView;
    }

    /**
     * Get the number of times a songs has been played
     * @param idSong ID of song
     * @return number of times a songs has been played
     */
    private int getCountSongPlayed(long idSong){
        AtomicInteger countSongPlayed = new AtomicInteger();
        Thread t = new Thread(()-> countSongPlayed.set(this.soundroidDatabaseInstance.historyDao().getTimesPlayedBySongId(idSong)));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e("HistoryAdapter", e.getMessage());
        }
        return countSongPlayed.get();
    }

    /**
     * get number of songs into list
     * @return number of songs
     */
    @Override
    public int getCount() {
        return songs.size();
    }

    /**
     * add a song to the list
     * @param object a song object
     */
    @Override
    public void add(@Nullable Song object) {
        super.add(object);
        songs.add(object);
    }

    /**
     * get an specific item as song at position into list
     * @param position given position
     * @return Song a specific song
     */
    @Nullable
    @Override
    public Song getItem(int position) {
        if (songs.isEmpty() || position > songs.size()) {
            Log.w("HistoryAdapter", "List contains 0 elements or position > size list");
            return null;
        }
        return songs.get(position);
    }
}

