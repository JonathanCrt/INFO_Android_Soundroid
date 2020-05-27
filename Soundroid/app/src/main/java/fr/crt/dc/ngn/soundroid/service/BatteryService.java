package fr.crt.dc.ngn.soundroid.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

/**
 * Created by CRETE JONATHAN on 27/05/2020.
 */
public class BatteryService extends Service {

    private static final int CHECK_BATTERY_INTERVAL = 5000;
    private double batteryLevel;
    private Handler handler;

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent batteryIntent) {
            int rawlevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            // get battery in pourcent
            if (rawlevel >= 0 && scale > 0) {
                batteryLevel = (rawlevel * 100.0) / scale;
            }

            Log.i("Niveau de batterie", "Batterie Réception intent : " + batteryLevel + " %");

        }
    };

    private Runnable checkBatteryStatusRunnable = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean autoPauseBatterySetting  = sharedPreferences.getBoolean("AutoPauseBatterySetting", false);
            if(autoPauseBatterySetting) {
                // if battery level is lower than 10%
                if (batteryLevel < 10) {
                    if (SongService.getSongService().playerIsPlaying()) {
                        Log.i("Pause automatique", "Arrêt de la lecture par contrainte de batterie");
                        Toast.makeText(getBaseContext(), "Arrêt de la lecture par contrainte de batterie !", Toast.LENGTH_LONG).show();
                        SongService.getSongService().getPlayer().pause();
                    }
                }
            }


            // schedule next battery check
            handler.postDelayed(checkBatteryStatusRunnable, CHECK_BATTERY_INTERVAL);
            Log.d("Niveau de batterie", "Batterie vérification il reste " + batteryLevel + " %");
        }
    };


    @Override
    public void onCreate() {
        this.handler = new Handler();
        this.handler.postDelayed(checkBatteryStatusRunnable, CHECK_BATTERY_INTERVAL);
        this.registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(batteryInfoReceiver);
        this.handler.removeCallbacks(checkBatteryStatusRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

}
