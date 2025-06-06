package com.cuckooclock.app;

import android.content.Context;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.rxjava2.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava2.RxDataStore;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class ApplicationSettings {
    private static final String SETTINGS_DATA_STORE_NAME = "settings_prefs";
    private volatile RxDataStore<Preferences> mDataStore = null;

    public ApplicationSettings(Context context) {
        if (mDataStore == null) {
            synchronized (ApplicationSettings.class) {
                if (mDataStore == null) {
                    try {
                        mDataStore = new RxPreferenceDataStoreBuilder(context, SETTINGS_DATA_STORE_NAME).build();
                    } catch (IllegalStateException exception) {
                        System.out.println("Error initializing DataStore: " + exception.getMessage());
                    }
                }
            }
        }
    }

    public <T> void saveValue(Preferences.Key<T> key, T value) {
        mDataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(key, value);
            return Single.just(mutablePreferences);
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public <T> void deleteValue(Preferences.Key<T> key) {
        mDataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.remove(key);
            return Single.just(mutablePreferences);
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    public <T> T getValue(Preferences.Key<T> key, T defaultValue) {
        return mDataStore.data().map(prefs -> {
            T value = prefs.get(key);
            return value != null ? value : defaultValue;
        }).blockingFirst();
    }
}