package fr.crt.dc.ngn.soundroid.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;

import androidx.annotation.Nullable;
import fr.crt.dc.ngn.soundroid.R;

/**
 * Created by CRETE JONATHAN on 27/05/2020.
 */
public class SpeakerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
