package com.connectedcuckooclockforandroid.ui.configuration;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConfigViewModel extends ViewModel {

    private final MutableLiveData<Color> mClockScreenColour;

    public ConfigViewModel() {
        mClockScreenColour = new MutableLiveData<Color>();
        mClockScreenColour.setValue(Color.valueOf(0.1f, 0.2f, 0.3f, 0.5f));
    }

    public LiveData<Color> getClockScreenColour() {
        return mClockScreenColour;
    }

    public void setClockScreenColour(Color colour){
        mClockScreenColour.setValue(colour);
    }
}