package com.example.owl.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.owl.R;
import com.example.owl.fragments.CanvasFragment;
import com.example.owl.fragments.FeedFragment;
import com.example.owl.fragments.FriendsFragment;
import com.example.owl.fragments.ProfileFragment;
import com.example.owl.fragments.SettingsFragment;

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
            while (getSupportFragmentManager().getBackStackEntryCount() > 0){
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
            }
            else { // re-use the old fragment
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
}
