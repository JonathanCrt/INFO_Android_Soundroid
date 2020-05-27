package fr.crt.dc.ngn.soundroid.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import fr.crt.dc.ngn.soundroid.R;

public class SettingsFragment extends Fragment {

    private Switch switchAutoPauseSetting;
    private Switch switchSpeakerSetting;
    private SharedPreferences sharedPreferences;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        this.switchAutoPauseSetting = v.findViewById(R.id.switch_settings_active_auto_pause);
        this.switchSpeakerSetting = v.findViewById(R.id.switch_settings_active_speaker);
        this.handleAutoPauseSettingSwitch();
        this.handleSpeakerSettingSwitch();
        this.setStateOfSwitchs();
        return v;
    }

    /**
     * set switch checked if settings is activate or not
     */
    private void setStateOfSwitchs() {
        boolean autoPauseBatterySetting = sharedPreferences.getBoolean("AutoPauseBatterySetting", false);
        if(autoPauseBatterySetting){
            this.switchAutoPauseSetting.setChecked(true);
        } else {
            this.switchAutoPauseSetting.setChecked(false);
        }

        boolean smsSpeakerSetting = this.sharedPreferences.getBoolean("SMSSpeakerSetting", false);
        if(smsSpeakerSetting){
            this.switchSpeakerSetting.setChecked(true);
        } else {
            this.switchSpeakerSetting.setChecked(false);
        }
    }

    /**
     * to manage onClick of switch button (Auto pause setting)
     */
    private void handleAutoPauseSettingSwitch() {
        SharedPreferences.Editor editorSharedPrefs = this.sharedPreferences.edit();
        if (this.switchAutoPauseSetting != null) {
            this.switchAutoPauseSetting.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    editorSharedPrefs.putBoolean("AutoPauseBatterySetting", true);
                    editorSharedPrefs.apply();

                } else {
                    editorSharedPrefs.putBoolean("AutoPauseBatterySetting", false);
                    editorSharedPrefs.apply();
                }
            });
        }
    }


    /**
     * to manage onClick of switch button (Speaker setting)
     */
    private void handleSpeakerSettingSwitch() {
        SharedPreferences.Editor editorSharedPrefs = this.sharedPreferences.edit();
        if (this.switchSpeakerSetting != null) {
            this.switchSpeakerSetting.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    editorSharedPrefs.putBoolean("SMSSpeakerSetting", true);
                    editorSharedPrefs.apply();

                } else {
                    editorSharedPrefs.putBoolean("SMSSpeakerSetting", false);
                    editorSharedPrefs.apply();
                }
            });
        }
    }


}
