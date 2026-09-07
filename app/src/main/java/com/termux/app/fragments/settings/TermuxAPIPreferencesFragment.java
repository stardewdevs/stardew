package io.stardew.app.fragments.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentio_stardewpat;
import androidx.preference.PreferenceManager;

import io.stardew.R;
import io.stardew.shared.termux.settings.preferences.TermuxAPIAppSharedPreferences;

@Keep
public class TermuxAPIPreferencesFragment extends PreferenceFragmentio_stardewpat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(TermuxAPIPreferencesDataStore.getInstance(context));

        setPreferencesFromResource(R.xml.termux_api_preferences, rootKey);
    }

}

class TermuxAPIPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final TermuxAPIAppSharedPreferences mPreferences;

    private static TermuxAPIPreferencesDataStore mInstance;

    private TermuxAPIPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = TermuxAPIAppSharedPreferences.build(context, true);
    }

    public static synchronized TermuxAPIPreferencesDataStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TermuxAPIPreferencesDataStore(context);
        }
        return mInstance;
    }

}
