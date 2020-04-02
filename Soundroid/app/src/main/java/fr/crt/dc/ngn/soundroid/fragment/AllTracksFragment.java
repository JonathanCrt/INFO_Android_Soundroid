package fr.crt.dc.ngn.soundroid.fragment;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.crt.dc.ngn.soundroid.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllTracksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllTracksFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public AllTracksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllTracksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllTracksFragment newInstance(String param1, String param2) {
        AllTracksFragment fragment = new AllTracksFragment();
        Bundle args = new Bundle();
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
        return inflater.inflate(R.layout.fragment_all_tracks, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
