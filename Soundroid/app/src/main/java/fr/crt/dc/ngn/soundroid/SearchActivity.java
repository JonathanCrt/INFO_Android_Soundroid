package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import fr.crt.dc.ngn.soundroid.adapter.SearchAdapter;
import fr.crt.dc.ngn.soundroid.model.Song;
import fr.crt.dc.ngn.soundroid.utility.StorageContainer;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ListView list = (ListView)findViewById(R.id.list_results_search);
        //List<Integer> list2 = (List<Integer>) StorageContainer.getInstance().get(getIntent().getLongExtra("album",-1));
        //Song song = (Song) StorageContainer.getInstance().get(getIntent().getLongExtra("title",-1));
        List<Integer> list3 = (List<Integer>) StorageContainer.getInstance().get(getIntent().getLongExtra("artist",-1));

        Log.i("INTENT", "LIST3:  " + list3);
        //if(list2 !=null)
          //  Log.i("INTENT", "LIST2:  " + list2);
       // if(song != null)
         //   Log.i("INTENT", "SONG:  " + song);
        //else{
          //  Log.i("INTENT", "can' find any song");
        //}

        //creation adapter
        //SearchAdapter adapter = new SearchAdapter(this, R.layout.activity_search,)
        //on set adapter

    }
}
