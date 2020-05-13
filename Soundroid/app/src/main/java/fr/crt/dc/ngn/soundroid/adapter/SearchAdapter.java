package fr.crt.dc.ngn.soundroid.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.model.Search;

public class SearchAdapter extends ArrayAdapter<Search> {

    private Context context;

    public SearchAdapter(@NonNull Context context, int resource, @NonNull Search[] objects) {
        super(context, resource, objects);
    }
}
