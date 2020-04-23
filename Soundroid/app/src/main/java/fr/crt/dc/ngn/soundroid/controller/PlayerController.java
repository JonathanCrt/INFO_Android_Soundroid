package fr.crt.dc.ngn.soundroid.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.service.SongService;

/**
 * Created by CRETE JONATHAN on 23/04/2020.
 */
public class PlayerController {

    // Views
    private ImageView ivBack;
    private TextView tvTitleSong;
    private TextView tvArtistSong;
    private ImageView ivArtworkSong;
    private ImageView ivRandomPlayback;
    private ImageView ivNoteStarOne;
    private ImageView ivNoteStarTwo;
    private ImageView ivNoteStarThree;
    private ImageView ivNoteStarFour;
    private ImageView ivNoteStarFive;
    private ImageView ivAddTag;
    private SeekBar seekBarPlayback;
    private ImageView ivControlPlaySong;
    private ImageView ivControlNextSong;
    private ImageView ivControlPreviousSong;

    private SongService songService;
    private Intent intent;
    private boolean connectionEstablished;

    private Context context;
    private ConstraintLayout constraintLayout;

    private void initializeViews() {
        this.ivBack = (ImageView) constraintLayout.getViewById(R.id.iv_player_back);
        this.tvTitleSong = (TextView) constraintLayout.getViewById(R.id.tv_player_title);
        this.tvArtistSong = (TextView) constraintLayout.getViewById(R.id.tv_player_artist);
        this.ivArtworkSong = (ImageView) constraintLayout.getViewById(R.id.iv_player_artwork);
        this.ivRandomPlayback = (ImageView) constraintLayout.getViewById(R.id.iv_player_shuffle_playback);

        this.ivNoteStarOne = (ImageView) constraintLayout.getViewById(R.id.iv_player_note_star_1);
        this.ivNoteStarTwo = (ImageView) constraintLayout.getViewById(R.id.iv_player_note_star_2);
        this.ivNoteStarThree = (ImageView) constraintLayout.getViewById(R.id.iv_player_note_star_3);
        this.ivNoteStarFour = (ImageView) constraintLayout.getViewById(R.id.iv_player_note_star_4);
        this.ivNoteStarFive = (ImageView) constraintLayout.getViewById(R.id.iv_player_note_star_5);

        this.ivAddTag = (ImageView) constraintLayout.getViewById(R.id.iv_player_add_tag);
        this.seekBarPlayback = (SeekBar) constraintLayout.getViewById(R.id.seekBar_player);
        this.ivControlPreviousSong = (ImageView) constraintLayout.getViewById(R.id.iv_player_control_previous);
        this.ivControlNextSong = (ImageView) constraintLayout.getViewById(R.id.iv_player_control_next);
        this.ivControlPlaySong = (ImageView) constraintLayout.getViewById(R.id.iv_player_control_play);
    }

    public PlayerController(Context context, ConstraintLayout layoutMainActivity){
        this.context = context;
        this.constraintLayout = layoutMainActivity;
        this.initializeViews();

        intent = new Intent(this.context, SongService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                SongService.SongBinder songBinder = (SongService.SongBinder) service;
                // Permet de récupérer le service
                songService = songBinder.getService();
                connectionEstablished = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                connectionEstablished = false;
            }
        };
        this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Objects.requireNonNull(this.context).startService(intent); //demarrage du service;

        installListener();
    }

    private void installListener(){
        this.ivControlPlaySong.setOnClickListener(v->pushPlayControl());
        this.ivControlNextSong.setOnClickListener(v->pushNextControl());
    }

    public void setImagePlay(){
        this.ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_play_white));

    }

    public void setImagePause(){
        this.ivControlPlaySong.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.ic_pause_white));
    }

    private void pushPlayControl() {
        this.songService.setToolbarPushed(true);
        if(!songService.playOrPauseSong()) {
            Toast.makeText(this.context, "State : Pause", Toast.LENGTH_SHORT).show();
            this.setImagePlay();
        } else {
            Toast.makeText(this.context, "State : Play", Toast.LENGTH_SHORT).show();
            this.setImagePause();
            //this.setWidgetsValues();
        }
    }

    private void pushNextControl() {
        this.songService.playNextSong();
        this.setImagePause();
        //this.setWidgetsValues();
    }


}
