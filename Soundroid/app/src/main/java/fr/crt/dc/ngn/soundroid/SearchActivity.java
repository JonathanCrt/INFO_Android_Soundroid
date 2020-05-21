package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
        Bundle bundle = this.getIntent().getExtras();

        int flag = getIntent().getIntExtra("flag", -1);
        Log.i("tag", "tag" + flag);
        List<Integer> resultList = null;
        switch (flag){
            case 0:
                resultList = (List<Integer>) StorageContainer.getInstance().get(getIntent().getLongExtra("title",-1));
                break;
            case 1:
                resultList = (List<Integer>) StorageContainer.getInstance().get(getIntent().getLongExtra("artist",-1));
                break;
            case 2:
                resultList = (List<Integer>) StorageContainer.getInstance().get(getIntent().getLongExtra("album",-1));
                break;
        }

        Log.i("INTENT", "resultList:  " + resultList);

        //creation adapter
        //SearchAdapter adapter = new SearchAdapter(this, R.layout.activity_search,)
        //on set adapter

    }
}
