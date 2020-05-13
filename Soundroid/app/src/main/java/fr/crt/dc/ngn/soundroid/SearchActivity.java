package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import fr.crt.dc.ngn.soundroid.adapter.SearchAdapter;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ListView list = (ListView)findViewById(R.id.list_results_search);



        //creation adapter
        //SearchAdapter adapter = new SearchAdapter(this, R.layout.activity_search,)
        //on set adapter

    }
}
