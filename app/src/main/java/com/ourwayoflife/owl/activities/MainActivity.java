package com.ourwayoflife.owl.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.IdentityChangedListener;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.fragments.CanvasFragment;
import com.ourwayoflife.owl.fragments.FeedFragment;
import com.ourwayoflife.owl.fragments.FriendsFragment;
import com.ourwayoflife.owl.fragments.ProfileFragment;
import com.ourwayoflife.owl.fragments.SettingsFragment;
import com.ourwayoflife.owl.models.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FeedFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener,
        CanvasFragment.OnFragmentInteractionListener,
        FriendsFragment.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getName();

    public static final String OPEN_FRAGMENT_CANVAS = "OPEN_FRAGMENT_CANVAS";

    public static final int PERMISSION_READ_EXTERNAL_STORAGE = 100;

    public static final int RESULT_OPEN_FRAGMENT_CANVAS = 200;


    private NavigationView mNavigationView;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the UploadActivity
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // Get user data for the nav header
        new DownloadUserTask().execute();
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // If given the extra indication to start profile fragment, do it
        if (getIntent().getBooleanExtra(OPEN_FRAGMENT_CANVAS, false)) {


            //Check if the fragment is already in the stack.
            //If it is, then use that instead of making a new instance
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment fragment = fragmentManager.findFragmentByTag(CanvasFragment.class.getName());

            // If fragment doesn't exist yet, create one
            if (fragment == null) {
                fragment = new CanvasFragment();
                fragmentTransaction
                        .replace(R.id.flContent, fragment, CanvasFragment.class.getName())
                        .addToBackStack(CanvasFragment.class.getName())
                        .commit();
            } else { // re-use the old fragment
                /*
                fragmentTransaction
                        .replace(R.id.flContent, fragment, fragmentClass.getName())
                        .addToBackStack(fragmentClass.getName())
                        .commit();
                        */
                fragmentManager.popBackStackImmediate(CanvasFragment.class.getName(), 0);
            }


            /*
            Fragment fragment = new CanvasFragment();
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.
                    beginTransaction()
                    .replace(R.id.flContent, fragment)
                    .addToBackStack(CanvasFragment.class.getName())
                    .commit();
            */
        } else {
            //Clear the backstack
            while (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStackImmediate();
            }

            // Open a fragment on startup
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.flContent, new FeedFragment());
            fragmentTransaction.commit();
            mNavigationView.getMenu().getItem(0).setChecked(true);

        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.
                    beginTransaction()
                    .replace(R.id.flContent, new SettingsFragment())
                    .addToBackStack(SettingsFragment.class.getName())
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (id) {
            case R.id.nav_feed:
                fragmentClass = FeedFragment.class;
                break;
            case R.id.nav_profile:
                fragmentClass = ProfileFragment.class;
                break;
            case R.id.nav_canvas:
                fragmentClass = CanvasFragment.class;
                break;
            /*
            case R.id.nav_friends:
                fragmentClass = FriendsFragment.class;
                break;
                */
            case R.id.nav_settings:
                fragmentClass = SettingsFragment.class;
                break;
            /*
            case R.id.nav_share:
                fragmentClass = FeedFragment.class;
                break;
            case R.id.nav_send:
                fragmentClass = FeedFragment.class;
                break;
                */
            default:
                fragmentClass = FeedFragment.class;
        }

        try {
            //Check if the fragment is already in the stack.
            //If it is, then use that instead of making a new instance
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragment = fragmentManager.findFragmentByTag(fragmentClass.getName());

            // If fragment doesn't exist yet, create one
            if (fragment == null) {
                fragment = (Fragment) fragmentClass.newInstance();
                fragmentTransaction
                        .replace(R.id.flContent, fragment, fragmentClass.getName())
                        .addToBackStack(fragmentClass.getName())
                        .commit();
            } else { // re-use the old fragment
                /*
                fragmentTransaction
                        .replace(R.id.flContent, fragment, fragmentClass.getName())
                        .addToBackStack(fragmentClass.getName())
                        .commit();
                        */
                fragmentManager.popBackStackImmediate(fragmentClass.getName(), 0);
            }


            //fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment

        //FragmentManager fragmentManager = getSupportFragmentManager();
        /*
        fragmentManager.
                beginTransaction()
                .replace(R.id.flContent, fragment)
                .addToBackStack(fragmentClass.getName())
                .commit();
                */

        // Highlight the selected item has been done by NavigationView
        item.setChecked(true);
        // Set action bar title
        setTitle(item.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public FloatingActionButton getFloatingActionButton() {
        return mFab;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class DownloadUserTask extends AsyncTask<Void, Void, User> {

        protected User doInBackground(Void... urls) {
            // Initialize the Amazon Cognito credentials provider



            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    MainActivity.this, // Context
                    "971897998846", // AWS Account ID
                    LoginActivity.COGNITO_IDENTITY_POOL, // Identity Pool ID
                    "arn:aws:iam::971897998846:role/Cognito_OwlUnauth_Role", // Unauthenticated Role ARN\
                    "arn:aws:iam::971897998846:role/Cognito_OwlAuth_Role", // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            credentialsProvider.registerIdentityChangedListener(new IdentityChangedListener() {
                @Override
                public void identityChanged(String oldIdentityId, String newIdentityId) {
                    //Logic to handle identity change
                }
            });

            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
            AccountManager am = AccountManager.get(MainActivity.this);
            Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            try {
                String token = GoogleAuthUtil.getToken(MainActivity.this, accounts[0].name,
                        "audience:server:client_id:" + getString(R.string.server_client_id));

                Map<String, String> logins = new HashMap<>();

                logins.put("accounts.google.com", token);
                credentialsProvider.setLogins(logins);
            } catch (Exception e) {
                Log.e(TAG, "Error getting Google+ Credentials: " + e);
            }

            //credentialsProvider.refresh();

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            // Query for User
            final String USER_ID = "0"; //TODO set user id

            return mapper.load(User.class, USER_ID);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(User user) {
            // TODO: check this.exception
            // TODO: do something with the feed
            // Clear dataset, add new items, then notify


            // If the user is not retrieved, then close the app
            if (user.getUserId() == null || user.getUserId().isEmpty()) {
                //TODO maybe do something better than just showing a toast?
                Toast.makeText(MainActivity.this, "Unable to retrieve user data", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Update the nav drawer UI to whatever the user's data is
            // Get the header view first
            View headerView = mNavigationView.getHeaderView(0);

            TextView textUserName = headerView.findViewById(R.id.text_user_name);
            textUserName.setText(user.getName());

            TextView textUserEmail = headerView.findViewById(R.id.text_user_email);
            textUserEmail.setText(user.getEmail());

        }
    }
}
