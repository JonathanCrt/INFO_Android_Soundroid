package fr.crt.dc.ngn.soundroid.fragment;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import fr.crt.dc.ngn.soundroid.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlaylistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaylistFragment extends Fragment {
    private Button addPplaylist;
    public PlaylistFragment() {
        // Required empty public constructor
    }

    public static PlaylistFragment newInstance(String param1, String param2) {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_playlist, container, false);
        this.addPplaylist = fragmentView.findViewById(R.id.btn_add_playlist);
        this.addPplaylist.setOnClickListener(v->{

            Log.i("PLAYLIST FRAGMENT", "ADD PLAYLIS");
            Toast.makeText(this.getContext(), "ADD PLAYLIST", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
            builder.setMessage("Nom de la playlist");
            final EditText tvPlaylistName = new EditText(getActivity());
            builder.setView(tvPlaylistName);
            builder.setNegativeButton("Cancel", null);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String playlistName = tvPlaylistName.getText().toString();
                    Log.i("PLAYLIST FRAGMENT", "PLAYLIS NAME : " + playlistName);
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
            alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));



        });
        return fragmentView;
    }
}
