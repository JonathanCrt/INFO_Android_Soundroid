package fr.crt.dc.ngn.soundroid.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import fr.crt.dc.ngn.soundroid.PlayerActivity;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.service.SongService;

/**
 * Created by CRETE JONATHAN on 23/04/2020.
 */
public class PlayerController extends AbstractController {

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

    private boolean isNoteSet;
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


    public PlayerController(Context context, ConstraintLayout layoutMainActivity){
        this.context = context;
        this.constraintLayout = layoutMainActivity;
        this.initializeViews();
        Log.i("PlayerController", "I'm into constructor");
        intent = new Intent(this.context, SongService.class);

        // Permet de récupérer le service

        this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        Objects.requireNonNull(this.context).startService(intent); //demarrage du service;
        installListener();
    }

    private void installListener(){
        this.ivControlPlaySong.setOnClickListener(v->pushPlayControl());
        this.ivControlNextSong.setOnClickListener(v->pushNextControl());
        this.ivControlPreviousSong.setOnClickListener((v->pushPreviousControl()));
        this.ivRandomPlayback.setOnClickListener(v->pushShuffleControl());
    }

    private void resetRating() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(0);
    }



    private void setRatingToOne() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(1);
    }

    private void setRatingToTwo() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(2);
    }

    private void setRatingToThree() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(3);
    }

    private void setRatingToFour() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(4);
    }

    private void setRatingToFive() {
        ivNoteStarOne.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarTwo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarThree.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarFour.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        ivNoteStarFive.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_filled_star_note));
        this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).setRating(5);
    }


    public void setListenerRating() {
        this.ivNoteStarOne.setOnClickListener(v -> {
            if(!isNoteSet) {
                this.setRatingToOne();
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarTwo.setOnClickListener(v -> {
            if(!isNoteSet) {
                this.setRatingToTwo();
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarThree.setOnClickListener(v -> {
            if(!isNoteSet) {
                this.setRatingToThree();
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarFour.setOnClickListener(v -> {
            if(!isNoteSet) {
                this.setRatingToFour();
            } else {
                isNoteSet = false;
                resetRating();
            }
        });

        this.ivNoteStarFive.setOnClickListener(v -> {
            if(!isNoteSet) {
                this.setRatingToFive();
            } else {
                isNoteSet = false;
                resetRating();
            }
        });
    }

    public void unbindService() {
        if(serviceConnection != null) {
            this.songService.unbindService(serviceConnection);
        }
    }


    public void setWidgetsValues(String title, String artist, int rating) {
        setTextSongInformation(title, this.tvTitleSong);
        setTextSongInformation(artist, this.tvArtistSong);
        //setArtworkSong(this.songService.getPlaylistSongs().get(this.songService.getSongIndex()).getArtwork(), this.ivArtworkSong);
        switch (rating) {
            case 1 :
                setRatingToOne();
                break;
            case 2:
                setRatingToTwo();
                break;
            case 3:
                setRatingToThree();
                break;
            case 4:
                setRatingToFour();
                break;
            case 5:
                setRatingToFive();
                break;
        }
    }

    public void changeSeekBar() {
        seekBarPlayback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                songService.handleSeekBar(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void pushPlayControl() {
        this.songService.setToolbarPushed(true);
        if(!songService.playOrPauseSong()) {
            Toast.makeText(this.context, "State : Pause", Toast.LENGTH_SHORT).show();
            setImagePlay(ivControlPlaySong, this.context);
        } else {
            Toast.makeText(this.context, "State : Play", Toast.LENGTH_SHORT).show();
            setImagePause(ivControlPlaySong, context);
            //setWidgetsValues();
        }
    }

    private void pushNextControl() {
        this.songService.playNextSong();
        setImagePause(ivControlPlaySong, context);
        //setWidgetsValues();
    }

    private void pushPreviousControl() {
        this.songService.playPreviousSong();
        setImagePause(ivControlPlaySong, context);
        //setWidgetsValues();
    }

    private void pushShuffleControl(){
        //appelle fonction shuffle dans songSERvice
        this.songService.shuffleSongList();
        //setWidgetsValues();
    }

}
