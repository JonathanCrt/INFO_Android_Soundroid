package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import fr.crt.dc.ngn.soundroid.adapter.SearchAdapter;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.utility.StorageContainer;

public class SearchActivity extends AppCompatActivity {

    public enum Criteria implements Serializable {
        TITLE, ARTIST, ALBUM
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ListView list = (ListView)findViewById(R.id.list_results_search);
        Bundle bundle = this.getIntent().getExtras();

        Criteria flag = (Criteria) getIntent().getSerializableExtra("flag");
        Log.i("tag", "tag" + flag);
        List<Song> resultList = null;
        long storageID = getIntent().getLongExtra("storageID",-1);
        Log.i("storage", "storage ID " + storageID);
        switch (flag){
            case TITLE:
                resultList = (List<Song>) StorageContainer.getInstance().get(storageID);
                break;

            case ARTIST:
                resultList = (List<Song>) StorageContainer.getInstance().get(storageID);
                break;

            case ALBUM:
                resultList = (List<Song>) StorageContainer.getInstance().get(storageID);
                break;

            default:
                throw new AssertionError("not good flag: " + flag);
        }
        Log.i("INTENT", "resultList:  " + resultList);


        //creation adapter
        //SearchAdapter adapter = new SearchAdapter(this, R.layout.activity_search,)
        //on set adapter

    }
}
