package com.example.owl.activities;

import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.example.owl.R;
import com.example.owl.fragments.ProfileFragment;
import com.example.owl.models.Comment;
import com.example.owl.adapters.CommentsRecyclerAdapter;
import com.example.owl.adapters.StackPhotoPagerAdapter;

import layout.StackPhotoPagerFragment;

public class StackActivity extends AppCompatActivity
    implements StackPhotoPagerFragment.OnFragmentInteractionListener,
        CommentsRecyclerAdapter.ItemClickListener,
        ProfileFragment.OnFragmentInteractionListener{

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CommentsRecyclerAdapter mAdapter;
    private Comment[] mDataset = new Comment[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stack);

        // Enable the up navigation button in the action bar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewPager pager = (ViewPager) findViewById(R.id.view_pager_stack);
        PagerAdapter adapter = new StackPhotoPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout_stack);
        tabLayout.setupWithViewPager(pager, true);



        // Initialize RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_comments);

        // Initialize Dataset
        // TODO

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // set up the RecyclerView
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CommentsRecyclerAdapter(this, mDataset);
        mAdapter.setClickListener(this);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // This is a hack-ish way to make it so that when the back arrow is pressed
            // in the action bar, instead of re-starting the main activity and sending the user
            // back to the default tab, they are sent back to whatever tab they were on last.
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        // Profile picture was pressed
        // Go to the canvas of that profile

        /*
        // Start profile fragment
        Fragment fragment = null;
        Class fragmentClass = ProfileFragment.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.
                beginTransaction()
                .replace(R.id.flContent, fragment)
                .addToBackStack(null)
                .commit();
        */
        // TODO Convert Profile Fragment to an activity
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
