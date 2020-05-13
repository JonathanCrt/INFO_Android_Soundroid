package fr.crt.dc.ngn.soundroid.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.model.Search;

public class SearchAdapter extends ArrayAdapter<Song> {

    private Context context;

    public SearchAdapter(@NonNull Context context, int resource, ArrayList<Song> objects) {
        super(context, resource, objects);
    }
}
