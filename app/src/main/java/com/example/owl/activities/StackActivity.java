package com.example.owl.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.example.owl.R;
import com.example.owl.fragments.ProfileFragment;
import com.example.owl.models.Comment;
import com.example.owl.adapters.CommentsRecyclerAdapter;
import com.example.owl.adapters.StackPhotoPagerAdapter;

import com.example.owl.fragments.StackPhotoPagerFragment;
import com.example.owl.models.Photo;

import java.util.ArrayList;

public class StackActivity extends AppCompatActivity
        implements StackPhotoPagerFragment.OnFragmentInteractionListener,
        CommentsRecyclerAdapter.ItemClickListener,
        ProfileFragment.OnFragmentInteractionListener {


    private static final String TAG = StackActivity.class.getName();


    private PagerAdapter mPagerAdapter;
    private ArrayList<Bitmap> mDatasetPhotos = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CommentsRecyclerAdapter mAdapter;
    private Comment[] mDataset = new Comment[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stack);

        // Enable the up navigation button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        //mDatasetPhotos.add();
        // Get extras data
        // If a photo was included in the extras, then the activity is in photo mode
        String photoId = getIntent().getStringExtra("photoId");
        if (photoId != null && photoId.length() > 0) {
            // Photo mode

            new DownloadPhotoTask().execute(photoId);

        } else if (true) {
            // Stack mode
            finish();
        }


        ViewPager pager = (ViewPager) findViewById(R.id.view_pager_stack);
        mPagerAdapter = new StackPhotoPagerAdapter(getSupportFragmentManager(), mDatasetPhotos);
        pager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout_stack);
        tabLayout.setupWithViewPager(pager, true);


        // Initialize RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_comments);

        // Initialize Dataset
        // TODO Initialize Dataset

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


        // Start profile fragment
        /*
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

        Intent openFragmentBIntent = new Intent(this, MainActivity.class);
        openFragmentBIntent.putExtra(MainActivity.OPEN_FRAGMENT_CANVAS, true);
        startActivity(openFragmentBIntent);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    private class DownloadPhotoTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {
            // Get the photo
            final String photoId = params[0];
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this,
                    "us-east-1:4c7583cd-9c5a-4175-b39e-8690323a893e", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);


            // Query for photos
            Photo queryPhoto = new Photo();
            queryPhoto.setPhotoId(photoId);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(queryPhoto)
                    .withConsistentRead(false);

            PaginatedQueryList<Photo> result = mapper.query(Photo.class, queryExpression);

            if (result.size() != 1) {
                Log.e(TAG, "Unexpected number of photos returned in Query.  Expected 1, query returned " + result.size());
                finish(); // TODO notify user
            }

            mDatasetPhotos.clear();

            // Convert photo string to bitmap
            String photoString = result.get(0).getPhoto();
            try {
                byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                mDatasetPhotos.add(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Conversion from String to Bitmap: " + e);
                finish(); // TODO notify user
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Void result) {
            // TODO: check this.exception
            // TODO: do something with the feed
            mPagerAdapter.notifyDataSetChanged();
        }

    }

}
