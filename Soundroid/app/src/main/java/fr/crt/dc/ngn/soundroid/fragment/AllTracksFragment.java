package fr.crt.dc.ngn.soundroid.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.fragment.app.FragmentTransaction;
import fr.crt.dc.ngn.soundroid.R;
import fr.crt.dc.ngn.soundroid.database.SoundroidDatabase;
import fr.crt.dc.ngn.soundroid.database.entity.Playlist;
import fr.crt.dc.ngn.soundroid.utility.RootList;
import fr.crt.dc.ngn.soundroid.adapter.SongAdapter;
import fr.crt.dc.ngn.soundroid.controller.ToolbarController;
import fr.crt.dc.ngn.soundroid.database.entity.Song;
import fr.crt.dc.ngn.soundroid.service.SongService;

/**
 * Classe représentant le fragment contenant toutes les pistes
 */
public class AllTracksFragment extends Fragment {

    private ArrayList<Song> playlistSongs;
    private SongService songService;
    private Intent intent;
    private boolean connectionEstablished;
    private boolean isOnBackground;
    private ListView lv;
    private ToolbarController toolbarController;
    private Button shuffleButton;
    private ImageView ivButtonFilter;
    private SongAdapter songAdapter;
    private View vSearchButton;
    private ImageView ivButtonAccessHistory;
    private ImageView ivButtonAccessPlaylists;
    private int currentNumbersOfSongs;
    private TextView tvNumberOfSongs;
    private ImageView ivButtonAccessFavorites;

    private static AllTracksFragment context;

    public AllTracksFragment() {// Required empty public constructor
    }


    public static AllTracksFragment getAppContext() {
        return AllTracksFragment.context;
    }

    private SoundroidDatabase soundroidDatabaseInstance;

    /**
     * initialize the fields
     */
    private void initialization() {
        this.connectionEstablished = false;
        this.isOnBackground = false;
        this.context = this;
        this.soundroidDatabaseInstance = SoundroidDatabase.getInstance(this.requireContext());
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialization();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.playlistSongs = RootList.getRootList();

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_all_tracks, container, false);

        lv = v.findViewById(R.id.list_songs);
        // create personal adapter
        songAdapter = RootList.getSongAdapter();
        lv.setAdapter(songAdapter);

        this.shuffleButton = v.findViewById(R.id.button2);
        this.toShuffle();

        this.tvNumberOfSongs = v.findViewById(R.id.tv_list_number_songs);
        this.ivButtonAccessHistory = v.findViewById(R.id.iv_list_go_history);
        this.ivButtonAccessPlaylists = v.findViewById(R.id.iv_list_go_playlists);
        this.ivButtonAccessFavorites = v.findViewById(R.id.iv_list_go_favoris);
        ConstraintLayout constraintLayout = requireActivity().findViewById(R.id.crt_layout);
        this.toolbarController = new ToolbarController(getActivity(), constraintLayout);

        this.installOnItemClickListener();
        this.installOnLongItemClickListener();
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(R.id.nav_host_fragment, this)
                .addToBackStack(null)
                .commit();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.ivButtonFilter = view.findViewById(R.id.iv_list_filter);
        ivButtonFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this.getContext(), ivButtonFilter);
            popup.getMenuInflater().inflate(R.menu.popup_filter, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                Toast.makeText(this.getContext(), "You clicked on : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                songAdapter.getFilter().filter(item.getTitle());
                this.songAdapter.notifyDataSetChanged();
                this.songService.setPlaylistSongs(songAdapter.getFilteredPlayList());
                return true;
            });
            popup.show(); //showing popup menu
        });
    }

    /**
     * install onClick listeners for buttons
     */
    private void installOnItemClickListener() {

        lv.setOnItemClickListener((parent, view, position, id) -> {
            this.songService.setCurrentSongIndex(position);
            this.songService.playOrPauseSong();
            this.toolbarController.setImagePauseFromFragment();
            this.toolbarController.setWidgetsValuesToolbar();
        });

        // listeners for quick access
        //---------------------------
        this.ivButtonAccessHistory.setOnClickListener(v -> {
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment, new HistoryFragment());
            fragmentTransaction.commit();
        });

        this.ivButtonAccessPlaylists.setOnClickListener(v -> {
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment, new PlaylistFragment());
            fragmentTransaction.commit();
        });

        this.ivButtonAccessFavorites.setOnClickListener(v -> {
            Bundle arguments = new Bundle();
            arguments.putString("name of playlist", getString(R.string.favoris));
            PlaylistFragmentDetail playlistFragmentDetail = new PlaylistFragmentDetail();
            playlistFragmentDetail.setArguments(arguments);
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction
                    .replace(R.id.nav_host_fragment, playlistFragmentDetail)
                    .addToBackStack(null)
                    .commit();
        });
    }

    /**
     * When the user click long on a song to add it to a playlist
     */
    @SuppressLint({"ClickableViewAccessibility", "CheckResult"})
    private void installOnLongItemClickListener() {
        this.lv.setOnItemLongClickListener((arg0, arg1, pos, id) -> {
            new Thread(() -> {
                Song songPressed = songAdapter.getItem(pos);
                assert songPressed != null;
                Log.d("long clicked", "song " + songPressed.toString());
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(this.requireContext());
                alertDialogBuilder.setTitle("Ajouter à une playlist");

                // Array adapter to show a list of playlist created by the user
                // The user don't have the possibility to add a song in an automatic playlist(favorite, most played, history)
                List<Playlist> playlistList = this.soundroidDatabaseInstance.playlistDao().getAllPlayLists().stream().filter(p -> !p.isAutomatic()).collect(Collectors.toList());
                // transform the playlist list into an array of playlist name
                String[] playlistsNames = playlistList.stream().map(Playlist::getName).toArray(String[]::new);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this.requireContext(),
                        android.R.layout.simple_dropdown_item_1line, playlistsNames);

                requireActivity().runOnUiThread(() -> {
                    final AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(this.getContext());
                    autoCompleteTextView.setAdapter(adapter);
                    alertDialogBuilder.setView(autoCompleteTextView);
                    // Display later the playlist name because it takes time to set the adapter
                    alertDialogBuilder.setNegativeButton("Cancel", null);
                    alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                        String playlistName = autoCompleteTextView.getText().toString();
                        // If the playlist name is not correct
                        if (playlistName.length() == 0 || !Arrays.asList(playlistsNames).contains(playlistName)) {
                            Toast.makeText(getContext(), "Click on a playlist name !", Toast.LENGTH_SHORT).show();
                        } else {
                            new Thread(() -> {
                                Playlist playlistClicked = playlistList.stream().filter(p -> p.getName().equals(playlistName)).findAny().get();
                                SoundroidDatabase.getInstance(requireContext()).junctionDAO().insertSongIntoPlayList(songPressed.getSongId(), playlistClicked.getPlaylistId());
                            }).start();
                        }
                    });

                    new Handler().postDelayed(autoCompleteTextView::showDropDown, 100);
                    alertDialogBuilder.setIcon(R.drawable.ic_menu_playlists);
                    // Add scroll in the dropdown list
                    androidx.appcompat.app.AlertDialog ad = alertDialogBuilder.show();
                    Objects.requireNonNull(ad.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    ad.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
                    ad.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryFlash));
                });
            }).start();
            return true;
        });
    }

    /**
     * Allow initialization of service's intance when fragment begin
     */
    @Override
    public void onStart() {
        super.onStart();
        this.doBindService();
        this.tvNumberOfSongs.setText(String.valueOf(RootList.getRootList().size()));
    }


    /**
     * Allow once the user returns to the application after you set the background
     * to interact with the controls when the reading itself is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        this.isOnBackground = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isOnBackground)
            isOnBackground = false;
    }

    /**
     * When the activity is not presented to the user
     */
    @Override
    public void onStop() {
        this.currentNumbersOfSongs = Integer.parseInt(this.tvNumberOfSongs.getText().toString());
        super.onStop();
    }

    @Override
    public void onDestroy() {
        this.toolbarController = null;
        super.onDestroy();
    }

    /**
     * to bind songService with our AllTracksFragment
     */
    private void doBindService() {
        if (intent == null) {
            intent = new Intent(getContext(), SongService.class);
            requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        requireActivity().startService(intent);
    }

    /**
     * to unbind songService with AllTracksFragment
     */
    private void doUnbindService() {
        if (connectionEstablished) {
            songService.unbindService(serviceConnection);
            connectionEstablished = false;
        }
        this.songService.stopService(intent);
        this.songService = null;
    }

    /**
     * to begin random playback on click to shuffle button
     */
    private void toShuffle() {
        this.shuffleButton.setOnClickListener(e -> {
            if (this.songService.toShuffle()) {
                this.songService.playOrPauseSong();
                this.toolbarController.setPlayImageOnPushed();
                this.shuffleButton.setTextColor(this.shuffleButton.getContext().getResources().getColor(R.color.colorPrimaryFlash));
            } else {
                this.shuffleButton.setTextColor(Color.BLACK);
            }
            this.toolbarController.setWidgetsValuesToolbar();
        });
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
            // Retrieves service
            songService = songBinder.getService();
            // Toggles serving the arraylist
            songService.setPlaylistSongs(playlistSongs);
            connectionEstablished = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectionEstablished = false;
        }
    };

}