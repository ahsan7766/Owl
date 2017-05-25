package com.example.owl.activities;

import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.owl.R;
import com.example.owl.adapters.CommentsRecyclerAdapter;
import com.example.owl.adapters.FeedRecyclerAdapter;
import com.example.owl.adapters.StackPhotoPagerAdapter;
import com.example.owl.views.ProfileCounterView;

import org.w3c.dom.Comment;

import layout.StackPhotoPagerFragment;

public class StackActivity extends AppCompatActivity
    implements StackPhotoPagerFragment.OnFragmentInteractionListener,
        CommentsRecyclerAdapter.ItemClickListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CommentsRecyclerAdapter mAdapter;
    private String[] mDataset = new String[4];

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

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
