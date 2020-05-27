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
import java.util.List;


import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Song;

public class ExportFragment extends Fragment {

    private Button btnExport;
    private SoundroidDatabase soundroidDatabaseInstance;
    private List<Song> responseSongsList;
    private String jsonData;

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
     * Serialize and generates a new json file with all database entities
     * open also file explorer of android system
     */
    private void generateJSONFileAndOpenFileManager() {
        this.btnExport.setOnClickListener(v -> new Thread(() -> {
            this.responseSongsList = this.soundroidDatabaseInstance.songDao().getAllSongs();
            Gson gson = new Gson();
            this.jsonData = gson.toJson(this.responseSongsList);
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = jsonParser.parse(this.jsonData).getAsJsonArray();

            try {
                Writer writerOutput;
                String pathDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Soundroid.json";
                writerOutput = new BufferedWriter(new FileWriter(new File(pathDownload)));
                writerOutput.write(jsonArray.toString());
                writerOutput.close();
                this.requireActivity().runOnUiThread(() -> Toast.makeText(this.getContext(), "Sauvegarde effectuée !", Toast.LENGTH_LONG).show());
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
            } catch (IOException e) {
                Log.e("ExportFragment", "Error during writing json file" + e.getMessage());
            }
        }).start());
    }


}
