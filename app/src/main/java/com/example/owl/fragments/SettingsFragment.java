package com.example.owl.fragments;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.View;

import com.example.owl.R;
import com.example.owl.activities.MainActivity;

/**
 * Created by Zach on 5/30/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.getName();

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set the background color to white, or else the fragment will be transparent
        View view = getView();
        if (view != null) {
            view.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));

            // This could (potentially) prevent things behind the settings fragment
            // from being unintentionally clicked in case the fragment does not
            // replace the underlying fragment as expected
            view.setClickable(true);
        } else {
            Log.e(TAG, "Could not obtain view of Fragment");
        }

    }
}
