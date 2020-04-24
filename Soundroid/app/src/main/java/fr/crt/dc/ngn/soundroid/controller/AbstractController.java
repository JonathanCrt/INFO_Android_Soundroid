package fr.crt.dc.ngn.soundroid.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.service.SongService;

/**
 * Created by CRETE JONATHAN on 24/04/2020.
 */
public class AbstractController implements Controller {

    @Override
    public void setImagePlay(ImageView iv, Context context) {
        iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_white));
    }

    @Override
    public void setImagePause(ImageView iv, Context context) {
        iv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause_white));
    }

    @Override
    public void setTextSongInformation(String info, TextView tv) {
        tv.setText(info);
    }

    @Override
    public void setArtworkSong(Bitmap artwork, ImageView iv) {
        iv.setImageBitmap(artwork);
    }

}
