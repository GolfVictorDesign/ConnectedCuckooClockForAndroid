package com.cuckooclock.ui.configuration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cuckooclock.R;
import com.cuckooclock.app.ApplicationSettings;
import com.cuckooclock.app.BlufiConstants;
import com.cuckooclock.ui.MainActivity;
import com.cuckooclock.databinding.FragmentConfigBinding;
import com.google.android.material.snackbar.Snackbar;

public class ConfigFragment extends Fragment {

    private static final int REQUEST_PERMISSION = 0x01;
    private static final int REQUEST_BLUFI = 0x10;
    private static final int MENU_SETTINGS = 0x01;

    private FragmentConfigBinding mFragmentConfigbinding;
    private Context mContext;
    private View mViewScreenFrontLight;

    // Saved settings
    private ApplicationSettings mApplicationSettings;
    private boolean mbWifiConfigDone;
    private Preferences.Key<Boolean> mPreferencesWifiConfigDoneKey;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ConfigViewModel configViewModel =
                new ViewModelProvider(this).get(ConfigViewModel.class);

        mContext = getContext();

        // Get the preferences settings of the application
        MainActivity mainActivity = (MainActivity) getActivity();

        if (mainActivity != null) {
            mApplicationSettings = mainActivity.getApplicationSettings();
        }

        mPreferencesWifiConfigDoneKey = PreferencesKeys.booleanKey("wifi_config_done");
        mbWifiConfigDone = mApplicationSettings.getValue(mPreferencesWifiConfigDoneKey, false);

        if (!mbWifiConfigDone) {
            Intent intent = new Intent(mContext, BleScanActivity.class);
            startActivity(intent);
        }

        mFragmentConfigbinding = FragmentConfigBinding.inflate(inflater, container, false);
        View root = mFragmentConfigbinding.getRoot();
        mViewScreenFrontLight = (View) root.findViewById(R.id.screen_front_light);

        // Set click listeners for all clickables of the fragment
        ConfigClickListener configClickListener = new ConfigClickListener();
        mFragmentConfigbinding.buttonBird.setOnClickListener(configClickListener);
        mFragmentConfigbinding.buttonRightWindow.setOnClickListener(configClickListener);
        mFragmentConfigbinding.buttonLeftWindow.setOnClickListener(configClickListener);
        mFragmentConfigbinding.buttonDancers.setOnClickListener(configClickListener);
        mViewScreenFrontLight.setOnClickListener(configClickListener);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFragmentConfigbinding = null;
    }


    private class ConfigClickListener implements View.OnClickListener, View.OnLongClickListener {
        private Toast mToast;

        @Override
        public void onClick(View currentView) {
            if (currentView == mFragmentConfigbinding.buttonBird) {
                if (mbWifiConfigDone) {
                    Snackbar.make(currentView, "Bird clicked !", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null)
                            .setAnchorView(R.id.button_bird).show();
                } else {
                    Snackbar.make(currentView, "Bird was never clicked before!", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null)
                            .setAnchorView(R.id.button_bird).show();
                    mbWifiConfigDone = true;
                    mApplicationSettings.saveValue(mPreferencesWifiConfigDoneKey, true);
                }
            }
            else if (currentView == mFragmentConfigbinding.buttonLeftWindow) {
                Snackbar.make(currentView, "Left window clicked, delete Prefs", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .setAnchorView(R.id.button_left_window).show();
                mApplicationSettings.deleteValue(mPreferencesWifiConfigDoneKey);
            }
            else if (currentView == mFragmentConfigbinding.buttonRightWindow) {
                Snackbar.make(currentView, "Right window clicked !", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .setAnchorView(R.id.button_left_window).show();
            }
            else if (currentView == mFragmentConfigbinding.buttonDancers) {
                Snackbar.make(currentView, "Dancers clicked !", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .setAnchorView(R.id.button_left_window).show();
            }
            else if (currentView == mViewScreenFrontLight) {
                Snackbar.make(currentView, "Screen front light clicked !", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .setAnchorView(R.id.button_left_window).show();
            }
        }

        @Override
        public boolean onLongClick(View currentView) {
            if (mToast != null) {
                mToast.cancel();
            }

            int msgRes = 0;
            if (currentView == mFragmentConfigbinding.buttonBird) {
                msgRes = R.string.confirm;
            }

            mToast = Toast.makeText(mContext, msgRes, Toast.LENGTH_SHORT);
            mToast.show();

            return true;
        }
    }

}