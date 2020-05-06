package fr.crt.dc.ngn.soundroid.adapter;

import android.util.Log;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import fr.crt.dc.ngn.soundroid.database.entity.Song;

/**
 * Created by CRETE JONATHAN on 30/04/2020.
 */
public class CustomFilter extends Filter {
    private ArrayList<Song> filteredList;
    private SongAdapter adapter;
    private ArrayList<Song> filteredSongs;

    CustomFilter(ArrayList<Song> filteredList, SongAdapter adapter) {
        this.filteredList = filteredList;
        Log.d("CustomFilter", filteredList.toString());
        this.adapter = adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        // Results
        FilterResults results = new FilterResults();

        // Validation
        if (constraint != null && constraint.length() > 0) {

            // Change to uppercase for consistency
            constraint = constraint.toString().toUpperCase();

            this.filteredSongs = new ArrayList<>();

            // Loop through filter list
            for (int i = 0; i < filteredList.size(); i++) {
                // filter
                filteredSongs.add(filteredList.get(i));
                Log.i("CustomFilter", filteredSongs.toString());
            }
            switch (constraint.toString()) {
                case "TITLE":
                    Collections.sort(filteredSongs, (a, b) -> a.getTitle().compareTo(b.getTitle()));
                    break;
                case "ARTISTE":
                    Collections.sort(filteredSongs, (a, b) -> a.getArtist().compareTo(b.getArtist()));
                    break;
                case "PLUS COURTES":
                    Collections.sort(filteredSongs, (a, b) -> String.valueOf(a.getDuration()).compareTo(String.valueOf(b.getDuration())));
                    break;
                case "PLUS_LONGUES":
                    Collections.sort(filteredSongs, (a, b) -> String.valueOf(b.getDuration()).compareTo(String.valueOf(a.getDuration())));
                    break;

                //if(filteredList.get(i).getTitle().toUpperCase().contains(constraint)) {
                // filteredSongs.add(filteredList.get(i));
                // }
            }

            results.count = filteredSongs.size();
            results.values = filteredSongs;
            Log.i("CustomFilter sorting", " " + filteredSongs.toString());
        } else {
            results.count = filteredList.size();
            results.values = filteredList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        Log.i("CustomFilter pusblishResults", " " + results.values);
        adapter.filteredPlayList = (ArrayList<Song>) results.values;

        adapter.notifyDataSetChanged();
    }
}
