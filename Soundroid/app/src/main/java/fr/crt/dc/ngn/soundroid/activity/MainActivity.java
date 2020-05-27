package fr.crt.dc.ngn.soundroid.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.service.BatteryService;
import fr.crt.dc.ngn.soundroid.tts.Speaker;
import fr.crt.dc.ngn.soundroid.utility.RootList;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;
import fr.crt.dc.ngn.soundroid.utility.StorageContainer;
import fr.crt.dc.ngn.soundroid.utility.Utility;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private static String TAG = "MainActivity";

    private static Context context;
    private Intent intent;
    private SongService songService;
    private boolean connectionEstablished;
    private SoundroidDatabase soundroidDatabase;
    private String[] listCriteria;
    private boolean[] checkedItems;
    private int selectedCriteria = 0;
    private ListView searchList;
    private TextView tvToolbarArtistSong;
    private TextView tvToolbarTitleSong;
    private ImageView ivToolbarArtworkSong;
    private Bitmap currentBitmapSong;
    private ImageView ivToolbarControlPlay;
    private Toolbar toolbarPlayer;
    private AudioManager audioManager;

    //TextToSpeech API
    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 10000;
    private final int SHORT_DURATION = 2000;
    private Speaker speaker;

    int PERMISSION_ALL = 1;
    // array of all permissions needed
    String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_CONTACTS,
    };


    private BroadcastReceiver smsReceiver;

    public static Context getAppContext() {
        return MainActivity.context;
    }


    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        this.soundroidDatabase = SoundroidDatabase.getInstance(getApplicationContext());
        Log.i("LOG", this.soundroidDatabase.toString());

        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();
        Toolbar toolbarHead = findViewById(R.id.toolbar);
        setSupportActionBar(toolbarHead);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_all_tracks, R.id.nav_playlists, R.id.nav_history, R.id.nav_share, R.id.nav_export, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        this.toolbarPlayer = findViewById(R.id.toolbar_player);

        //launch Player Activity
        this.toolbarPlayer.setOnClickListener(v -> this.launchPlayerActivity());

        AtomicReference<ArrayList<Song>> listSongs = new AtomicReference<>();
        Thread t = new Thread(() -> {
            listSongs.set((ArrayList<Song>) soundroidDatabase.songDao().getAllSongs());
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (listSongs.get().isEmpty()) {
            // first launch of the app
            Log.i("LOG", "First launch of the app");

        } else {
                /*
                Log.i("LOG", "already LAUNCHED");
                // test to delete all in DB and restart with a new DB clean

                this.deleteDatabase("Soundroid.db_");
                soundroidDatabase.clearAllTables();
                soundroidDatabase.songDao().getAllSongs().forEach(s -> {
                    Log.i("LOG", "delete song id : " + s.getSongId());
                    soundroidDatabase.songDao().deleteSong(s);
                });
                return;

                 */

            //Collections.sort(listSongs, (a, b) -> a.getTitle().compareTo(b.getTitle()));
        }

        try {
            SongAdapter adapter = new SongAdapter(getAppContext(), listSongs.get());
            // launch async task
            RootList.callAsyncTask(adapter, listSongs.get());
        } catch (ExecutionException | InterruptedException e) {
            Log.e("MainActivity", Objects.requireNonNull(e.getMessage()));
        }
        this.listCriteria = getResources().getStringArray(R.array.search_criteria);
        this.checkedItems = new boolean[listCriteria.length];
        this.searchList = findViewById(R.id.list_songs);
        this.tvToolbarArtistSong = findViewById(R.id.tv_toolbar_artist);
        this.tvToolbarTitleSong = findViewById(R.id.tv_toolbar_title);
        this.ivToolbarArtworkSong = findViewById(R.id.iv_toolbar_artwork);
        this.ivToolbarControlPlay = findViewById(R.id.iv_control_play);
        this.checkTTS();
        this.initializeSMSReceiver();
        this.registerSMSReceiver();
        this.audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        startService(new Intent(this, BatteryService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentTitleReceived = sharedPreferences.getString("current_song_title", "Titre");
        String currentArtisteReceived = sharedPreferences.getString("current_song_artist", "Artiste");
        this.tvToolbarArtistSong.setText(currentArtisteReceived);
        this.tvToolbarTitleSong.setText(currentTitleReceived);
        new Thread(() -> {
            byte[] currentByteArtwork = this.soundroidDatabase.songDao().findArtworkBySongId(sharedPreferences.getLong("current_song_id", 0L));
            if (currentByteArtwork != null) {
                this.currentBitmapSong = Utility.convertByteToBitmap(currentByteArtwork);
                this.runOnUiThread(() -> this.ivToolbarArtworkSong.setImageBitmap(this.currentBitmapSong));
            }
        }).start();
        String statePlayer = sharedPreferences.getString("playback_state", "");
        this.runOnUiThread(() -> {
            switch (statePlayer) {
                case "PLAYING":
                    this.ivToolbarControlPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_white));
                    break;
                case "PAUSE":
                    this.ivToolbarControlPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_white));
                    break;
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        unregisterReceiver(this.smsReceiver);
        this.speaker.shutdownTTS();
    }

    public void doBindService() {
        if (intent == null) {
            intent = new Intent(this, SongService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        startService(intent);
    }

    public void doUnbindService() {
        if (connectionEstablished) {
            songService.unbindService(serviceConnection);
            connectionEstablished = false;
        }
        this.stopService(intent);
        this.songService = null;
    }

    /**
     *  launch the PlayerActivity with the current song's metadata
     */
    private void launchPlayerActivity() {
        new Thread(() -> {
            Intent currentSongIntent = new Intent(this, PlayerActivity.class);
            Song currentSong = this.songService.getPlaylistSongs().get(this.songService.getSongIndex());
            currentSongIntent
                    .putExtra("TITLE_SONG", currentSong.getTitle())
                    .putExtra("ARTIST_SONG", currentSong.getArtist())
                    .putExtra("RATING_SONG", currentSong.getRating())
                    .putExtra("ARTWORK_SONG", Utility.convertByteToBitmap(currentSong.getArtwork()))
                    .putExtra("DURATION_SONG", currentSong.getDuration())
                    .putExtra("TAG_SONG", currentSong.getTag());
            startActivity(currentSongIntent);
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        monoSearch(item);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * @param context     context of mainActivity
     * @param permissions array of permissions
     * @return boolean indicates if user have permissions
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Connection to service
     * ServiceConnection =  interface to manage the state of the service
     * These callback methods notify the class when the instance of the fragment
     * is successfully connected to the service instance
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongService.SongBinder songBinder = (SongService.SongBinder) service;
            // Permet de récupérer le service
            songService = songBinder.getService();
            // Permet de passer au service l'ArrayList
            //songService.setPlaylistSongs(playlistSongs);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * open alertDialog for searhcing a song with one criteria
     * @param item item from the checkbox
     */
    public void monoSearch(MenuItem item) {
        item = item.setOnMenuItemClickListener(l -> {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this, R.style.MyCheckBox);
            mBuilder.setTitle("Chercher une musique");

            final EditText input = new EditText(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            input.setHint("Saisir la recherche");
            mBuilder.setView(input);


            mBuilder.setMultiChoiceItems(this.listCriteria, this.checkedItems, (dialog, position, isChecked) -> {
                if (isChecked) {
                    selectedCriteria = position;
                }
            });

            mBuilder.setPositiveButton("OK", (dialog, which) -> {
                String userInput = input.getText().toString();
                requestToDatabase(selectedCriteria, userInput);
            });

            AlertDialog dialog = mBuilder.create();
            dialog.show();
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
            return true;
        });

    }

    /**
     *  request to the database and start new intent
     *
     * @param position position is the checkbox clicked
     * @param userInput userInput is the input written by user
     */
    public void requestToDatabase(int position, String userInput) {
        SearchActivity.Criteria flag = null;
        Intent intent = new Intent(this, SearchActivity.class);
        class Box {
            private List<Song> resultList = null;
        }
        Runnable run = null;
        Box box = new Box();

        switch (position) {
            case 0:
                run = () -> {
                    box.resultList = this.soundroidDatabase.songDao().findByTitle(userInput);
                };
                flag = SearchActivity.Criteria.TITLE;
                break;
            case 1:
                run = () -> box.resultList = this.soundroidDatabase.songDao().findAllByArtist(userInput);
                flag = SearchActivity.Criteria.ARTIST;
                break;
            case 2:
                run = () -> box.resultList = this.soundroidDatabase.songDao().findAllByAlbum(userInput);
                flag = SearchActivity.Criteria.ALBUM;
                break;
        }
        Thread td = new Thread(run);
        td.start();
        try {
            td.join();
        } catch (InterruptedException e) {
            return;
        }
        intent.putExtra("input", userInput);
        intent.putExtra("flag", flag);
        intent.putExtra("storageID", StorageContainer.getInstance().add(box.resultList));
        startActivity(intent);
    }


    private void checkTTS() {
        Intent intentCheck = new Intent();
        intentCheck.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intentCheck, CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d("Mainctivity tts", "onActivityResult");
        if (requestCode == CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //Log.d("Mainctivity tts", "new Speaker");
                this.speaker = new Speaker(this);
            } else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
    }

    private void initializeSMSReceiver() {
        this.smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean smsSpeakerSetting = sharedPreferences.getBoolean("SMSSpeakerSetting", false);
                if (smsSpeakerSetting) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        assert pdus != null;
                        for (Object o : pdus) {
                            byte[] pdu = (byte[]) o;
                            SmsMessage message = SmsMessage.createFromPdu(pdu);
                            String text = message.getDisplayMessageBody();
                            String sender = getContactName(message.getOriginatingAddress());

                            SongService.getSongService().pausePlayback();
                            speaker.pause(SHORT_DURATION);
                            speaker.speakText("Vous avez reçu un message de " + sender + "!");
                            speaker.pause(SHORT_DURATION);
                            speaker.speakText(text);
                            ivToolbarControlPlay.setImageDrawable(getDrawable(R.drawable.ic_play_white));

                        }
                    }
                }


            }
        };

    }

    private String getContactName(String phone) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        String[] projection = new String[]{ContactsContract.Data.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        } else {
            return "numéro inconnu";
        }
    }

    private void registerSMSReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }

}
