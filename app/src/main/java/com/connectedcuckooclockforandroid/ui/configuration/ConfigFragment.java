package com.connectedcuckooclockforandroid.ui.configuration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.connectedcuckooclockforandroid.R;
import com.connectedcuckooclockforandroid.databinding.FragmentConfigBinding;
import com.google.android.material.snackbar.Snackbar;

public class ConfigFragment extends Fragment {

    private FragmentConfigBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ConfigViewModel configViewModel =
                new ViewModelProvider(this).get(ConfigViewModel.class);

        binding = FragmentConfigBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.buttonBird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View currentView) {
                Snackbar.make(currentView, "Bird clicked !", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .setAnchorView(R.id.button_bird).show();
            }
        });

        binding.buttonRightWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View currentView) {
                Snackbar.make(currentView, "Right window clicked !", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .setAnchorView(R.id.button_right_window).show();
            }
        });

        binding.buttonLeftWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View currentView) {
                Snackbar.make(currentView, "Left window clicked !", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .setAnchorView(R.id.button_left_window).show();
            }
        });

        binding.buttonDancers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View currentView) {
                Snackbar.make(currentView, "Dancers clicked !", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .setAnchorView(R.id.button_dancers).show();
            }
        });

        View viewScreen = (View) root.findViewById(R.id.screen_front_light);
        viewScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View currentView) {
                Snackbar.make(currentView, "Screen clicked !", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .setAnchorView(R.id.screen_front_light).show();
            }
        });

        final Button buttonTest1 = binding.buttonLeftWindow;
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}