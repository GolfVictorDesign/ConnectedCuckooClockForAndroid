package com.cuckooclock.ui.configuration;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.cuckooclock.R;
import com.cuckooclock.app.ApplicationSettings;
import com.cuckooclock.app.MainActivity;
import com.cuckooclock.databinding.FragmentConfigBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConfigFragment extends Fragment {

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
            mbWifiConfigDone = true;
            // Creation du nouveau fragment de Scan BLE
//            BleScanFragment bleScanFragment =BleScanFragment.newInstance("toto", "tata");
//
//            NavController navController = NavHostFragment.findNavController(this);
//            navController.navigate(R.id.action_nav_config_to_nav_scan);
//
//            getParentFragmentManager().beginTransaction()
//                    .replace(R.id.frame_ble_scan, bleScanFragment)
//                    .addToBackStack(null)
//                    .commit();
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