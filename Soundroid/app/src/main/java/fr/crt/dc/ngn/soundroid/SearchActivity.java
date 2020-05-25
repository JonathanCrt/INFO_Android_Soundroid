package fr.crt.dc.ngn.soundroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;
import fr.crt.dc.ngn.soundroid.utility.StorageContainer;
import fr.crt.dc.ngn.soundroid.utility.Utility;

public class SearchActivity extends AppCompatActivity {

    public enum Criteria implements Serializable {
        TITLE, ARTIST, ALBUM
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ListView list = findViewById(R.id.list_results_search);
        Bundle bundle = this.getIntent().getExtras();

        Criteria flag = (Criteria) getIntent().getSerializableExtra("flag");
        Log.i("tag", "tag" + flag);
        List<Song> resultList = null;
        long storageID = getIntent().getLongExtra("storageID",-1);
        Log.i("storage", "storage ID " + storageID);
        Criteria[] authorizedCriteria = new Criteria[]{Criteria.TITLE, Criteria.ALBUM, Criteria.ARTIST};
        if(Arrays.asList(authorizedCriteria).contains(flag)){
            resultList = (List<Song>) StorageContainer.getInstance().get(storageID);
        }else {
            throw new AssertionError("not good flag: " + flag);
        }

        if (resultList != null && resultList.isEmpty()){
            Toast.makeText(getApplicationContext(), "Auncun résultat", Toast.LENGTH_LONG).show();
            this.onBackPressed();
            return;
        }

        SongAdapter songAdapter = new SongAdapter(this,resultList);
        list.setAdapter(songAdapter);

        list.setOnItemClickListener((parent, view, position, id) ->{
            Song song = songAdapter.getItem(position);
            Intent intent = new Intent(this, PlayerActivity.class);
            intent
                    .putExtra("SEARCH", true)
                    .putExtra("ID_SONG", song.getSongId())
                    .putExtra("SONG_POSITION", position)
                    .putExtra("SONG", song)
                    .putExtra("TITLE_SONG", song.getTitle())
                    .putExtra("ARTIST_SONG", song.getArtist())
                    .putExtra("RATING_SONG", song.getRating())
                    .putExtra("ARTWORK_SONG", Utility.convertByteToBitmap(song.getArtwork()))
                    .putExtra("DURATION_SONG", song.getDuration())
                    .putExtra("TAG_SONG", song.getTag());
            startActivity(intent);
        });
    }
}
