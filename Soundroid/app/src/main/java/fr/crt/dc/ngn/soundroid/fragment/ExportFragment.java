package fr.crt.dc.ngn.soundroid.fragment;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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

    private void generateJSONFileAndOpenFileManager() {
        this.btnExport.setOnClickListener(v -> {
            new Thread(() -> {
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
                    this.getActivity().runOnUiThread(() -> {
                        Toast.makeText(this.getContext(), "Sauvegarde effectuée !", Toast.LENGTH_LONG).show();
                    });
                    startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                } catch (IOException e) {
                    Log.e("ExportFragment", "Error during writing json file" + e.getMessage());
                }
            }).start();

        });
    }


}
