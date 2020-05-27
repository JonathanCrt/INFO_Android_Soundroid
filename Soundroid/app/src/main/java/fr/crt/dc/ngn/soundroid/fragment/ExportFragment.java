package fr.crt.dc.ngn.soundroid.fragment;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.database.relation.PlaylistWithSongs;

public class ExportFragment extends Fragment {

    private Button btnExport;
    private SoundroidDatabase soundroidDatabaseInstance;
    private List<Song> responseSongsList;
    private List<Playlist> responsePlaylistList;
    private List<PlaylistWithSongs> responsePlaylistWithSongsList;
    private List<Song> responseHistoryList;
    private String songsJsonData;
    private String playlistsJsonData;
    private String playlistsWithSongsJsonData;
    private String historyJsonData;

    public ExportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(this.requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_export, container, false);
        this.btnExport = v.findViewById(R.id.btn_export_database);
        this.generateJSONFileAndOpenFileManager();
        return v;
    }

    /**
     * helper method to concatenate json arrays
     *
     * @param arrs varags of jsonArray
     * @return resulting jsonArray
     */
    private JsonArray concatJsonArray(JsonArray... arrs) {
        JsonArray result = new JsonArray();
        for (JsonArray arr : arrs) {
            for (int i = 0; i < arr.size(); i++) {
                result.add(arr.get(i));
            }
        }
        return result;
    }


    /**
     * Serialize and generates a new json file with all database entities
     * open also file explorer of android system
     */
    private void generateJSONFileAndOpenFileManager() {
        this.btnExport.setOnClickListener(v -> new Thread(() -> {
            // get content of all entities
            this.responseSongsList = this.soundroidDatabaseInstance.songDao().getAllSongs();
            this.responsePlaylistList = this.soundroidDatabaseInstance.playlistDao().getAllPlayLists();
            this.responsePlaylistWithSongsList = this.soundroidDatabaseInstance.junctionDAO().getPlaylistsWithSongs();
            this.responseHistoryList = this.soundroidDatabaseInstance.historyDao().getAllHistory();


            // serialize objects to json
            Gson gson = new Gson();
            this.songsJsonData = gson.toJson(this.responseSongsList);
            this.playlistsJsonData = gson.toJson(this.responsePlaylistList);
            this.playlistsWithSongsJsonData = gson.toJson(this.responsePlaylistWithSongsList);
            this.historyJsonData = gson.toJson(this.responseHistoryList);

            JsonParser jsonParser = new JsonParser();
            JsonArray jsonElements = this.concatJsonArray(
                    jsonParser.parse(this.songsJsonData).getAsJsonArray(),
                    jsonParser.parse(this.playlistsJsonData).getAsJsonArray(),
                    jsonParser.parse(this.playlistsWithSongsJsonData).getAsJsonArray(),
                    jsonParser.parse(this.historyJsonData).getAsJsonArray()
            );
            try {
                Writer writerOutput;
                // set format date
                DateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yy_HHmmss", Locale.getDefault());
                // get current string date for name of json file
                String sDate = simpleDateFormat.format(new Date());

                String pathDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Soundroid" + sDate + ".json";
                writerOutput = new BufferedWriter(new FileWriter(new File(pathDownload)));
                writerOutput.write(jsonElements.toString());
                writerOutput.close();
                this.requireActivity().runOnUiThread(() -> Toast.makeText(this.getContext(), "Sauvegarde effectuée !", Toast.LENGTH_LONG).show());
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
            } catch (IOException e) {
                Log.e("ExportFragment", "Error during writing json file" + e.getMessage());
            }
        }).start());
    }


}
