package fr.crt.dc.ngn.soundroid.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

import fr.crt.dc.ngn.soundroid.service.SongService;

/**
 * Created by CRETE JONATHAN on 20/04/2020.
 */
public class NotificationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("NotificationBroadcadt", "I'm into receiver");


        switch (Objects.requireNonNull(intent.getAction())){
            case "NOTIFY_PLAY":
                Toast.makeText(context, "NOTIFY_PLAY", Toast.LENGTH_SHORT).show();
                Log.i("NotificationBrodcast", "Play");
                break;
            case "NOTIFY_PAUSE":
                Toast.makeText(context, "NOTIFY_PAUSE", Toast.LENGTH_SHORT).show();
                break;
            case "NOTIFY_PREVIOUS":
                Toast.makeText(context, "NOTIFY_PREVIOUS", Toast.LENGTH_SHORT).show();
                break;
            case "NOTIFY_NEXT":
                Toast.makeText(context, "NOTIFY_NEXT", Toast.LENGTH_SHORT).show();
                break;
            case "NOTIFY_DELETE":
                Toast.makeText(context, "NOTIFY_DELETE", Toast.LENGTH_SHORT).show();
                break;
        }


    }
}
