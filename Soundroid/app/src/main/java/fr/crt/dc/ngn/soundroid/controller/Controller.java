package fr.crt.dc.ngn.soundroid.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import fr.crt.dc.ngn.soundroid.service.SongService;

/**
 * Created by CRETE JONATHAN on 24/04/2020.
 */
public interface Controller {
    void setImagePlay(ImageView iv, Context context);
    void setImagePause(ImageView iv, Context context);
    void setTextSongInformation(String info, TextView tv);
    void setArtworkSong(Bitmap artwork, ImageView iv);


    /*

    void pushPlayControl(SongService service, Context context, ImageView iv);

    void setWidgetsValues();
    void pushNextControl();
    void pushPreviousControl();

     */


}
