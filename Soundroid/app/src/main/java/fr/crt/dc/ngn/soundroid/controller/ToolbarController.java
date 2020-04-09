package fr.crt.dc.ngn.soundroid.controller;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import fr.crt.dc.ngn.soundroid.R;

/**
 * Created by CRETE JONATHAN on 09/04/2020.
 */
public class ToolbarController extends AppCompatActivity  {

    private Toolbar toolbar;
    private ImageView artwork;
    private TextView titleSong;
    private TextView artistSong;
    private ImageView ivPlayControl;
    private ImageView ivNextControl;
    private ImageView ivPrevControl;
    private boolean connectionEstablished;

    public void initializeViews () {
        this.toolbar = findViewById(R.id.toolbar_player);
        this.artwork = findViewById(R.id.iv_artwork);
        this.titleSong = findViewById(R.id.tv_title_track);
        this.artistSong = findViewById(R.id.tv_artist);
        this.ivPlayControl = findViewById(R.id.iv_control_play);
        this.ivNextControl = findViewById(R.id.iv_control_player_next);
        this.ivPrevControl = findViewById(R.id.iv_control_player_previous);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initializeViews();
        this.connectionEstablished = false;
    }

    /**
     * Manage play control
     */
    public void pushPlayControl() {
        // TODO : code this method
    }

    public void pushNextControl() {
        // TODO : code this method
    }

    public void pushPreviousControl() {
        // TODO : code this method
    }


}
