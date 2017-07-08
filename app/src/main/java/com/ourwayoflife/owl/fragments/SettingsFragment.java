package com.ourwayoflife.owl.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.activities.LoginActivity;

/**
 * Created by Zach on 5/30/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.getName();

    private GoogleApiClient mGoogleApiClient;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();
        */


        // Sign Out Preference
        Preference prefSignOut = findPreference(getString(R.string.pref_key_sign_out));
        prefSignOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                // If the ApiClient is not connected, then we can't sign the user out
                if(!mGoogleApiClient.isConnected()) {
                    Toast.makeText(getActivity(), "Sign Out Unsuccessful.  No connection established", Toast.LENGTH_SHORT).show();
                }

                // Sign the user out
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toast.makeText(getActivity(), "Sign Out Successful", Toast.LENGTH_SHORT).show();
                            }
                        });

                return true;
            }
        });

        // Delete Account Preference
        Preference prefDeleteAccount = findPreference(getString(R.string.pref_key_delete_account));
        prefDeleteAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                // If the ApiClient is not connected, then we can't sign the user out
                if(!mGoogleApiClient.isConnected()) {
                    Toast.makeText(getActivity(), "Account Deletion Unsuccessful.  No connection established", Toast.LENGTH_SHORT).show();
                }


                Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                // TODO need to do a bunch more than just disconnecting when account is deleted
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toast.makeText(getActivity(), "Account Deletion Successful", Toast.LENGTH_SHORT).show();
                            }
                        });
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }
}
