package fr.crt.dc.ngn.soundroid.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.crt.dc.ngn.soundroid.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends Fragment {


    public PlayerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlayerFragment.
     */
    private static PlayerFragment newInstance() {
        PlayerFragment fragment = new PlayerFragment();
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

        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    // @Override
    //public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { //un fois que la view est crée
    //get the Fragment Manager
    //  FragmentManager fragmentManager = getParentFragmentManager();

    //Transaction start
    //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    //inside a transaction we can add, replace or remove a fragment

    //fragmentTransaction.replace(R.id.list_songs, fragmentManager.findFragmentById(R.id.list_songs));
    //Log.d("FRAG", "FRAG: " + fragmentManager.findFragmentById(R.id.list_songs));

    //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    //fragmentTransaction.addToBackStack(null);

    //fragmentTransaction.commit();
    //super.onViewCreated(view, savedInstanceState);
    //}
}
