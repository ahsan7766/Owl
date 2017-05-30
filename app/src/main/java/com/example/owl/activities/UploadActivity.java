package com.example.owl.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.example.owl.R;
import com.example.owl.adapters.UploadPhotosRecyclerAdapter;
import com.example.owl.adapters.UploadStackRecyclerAdapter;

public class UploadActivity extends AppCompatActivity
        implements UploadPhotosRecyclerAdapter.ItemClickListener,
        UploadStackRecyclerAdapter.ItemClickListener {

    private RecyclerView mRecyclerViewPhotos;
    private LinearLayoutManager mLayoutManagerPhotos;
    private UploadPhotosRecyclerAdapter mAdapterPhotos;
    private String[] mDatasetPhotos = new String[8];

    private RecyclerView mRecyclerViewStack;
    private LinearLayoutManager mLayoutManagerStack;
    private UploadStackRecyclerAdapter mAdapterStack;
    private String[] mDatasetStack = new String[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Enable the up navigation button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup Photos Recycler
        // Initialize RecyclerView
        mRecyclerViewPhotos = (RecyclerView) findViewById(R.id.recycler_upload_photos);

        // Initialize Dataset
        // TODO Initialize Dataset

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManagerPhotos = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        // set up the RecyclerView
        mRecyclerViewPhotos.setLayoutManager(mLayoutManagerPhotos);
        mAdapterPhotos = new UploadPhotosRecyclerAdapter(this, mDatasetPhotos);
        mAdapterPhotos.setClickListener(this);

        // Set divider
        DividerItemDecoration dividerItemDecorationPhotos = new DividerItemDecoration(mRecyclerViewPhotos.getContext(),
                mLayoutManagerPhotos.getOrientation());
        mRecyclerViewPhotos.addItemDecoration(dividerItemDecorationPhotos);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerViewPhotos.setAdapter(mAdapterPhotos);



        // Setup Stack Recycler
        // Initialize RecyclerView
        mRecyclerViewStack = (RecyclerView) findViewById(R.id.recycler_upload_stack);

        // Initialize Dataset
        // TODO Initialize Dataset

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManagerStack = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // set up the RecyclerView
        mRecyclerViewStack.setLayoutManager(mLayoutManagerStack);
        mAdapterStack = new UploadStackRecyclerAdapter(this, mDatasetStack);
        mAdapterStack.setClickListener(this);

        // Set divider
        DividerItemDecoration dividerItemDecorationStack = new DividerItemDecoration(mRecyclerViewStack.getContext(),
                mLayoutManagerStack.getOrientation());
        mRecyclerViewStack.addItemDecoration(dividerItemDecorationStack);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerViewStack.setAdapter(mAdapterStack);


        /*
        // Allow user to select photo(s)
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
        */
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
}
