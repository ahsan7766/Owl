package com.ourwayoflife.owl.activities;

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
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.fragments.CanvasFragment;
import com.ourwayoflife.owl.models.PhotoComment;
import com.ourwayoflife.owl.adapters.PhotoCommentsRecyclerAdapter;
import com.ourwayoflife.owl.adapters.StackPhotoPagerAdapter;

import com.ourwayoflife.owl.fragments.StackPhotoPagerFragment;
import com.ourwayoflife.owl.models.Photo;
import com.ourwayoflife.owl.models.User;
import com.google.gson.internal.bind.ArrayTypeAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StackActivity extends AppCompatActivity
        implements StackPhotoPagerFragment.OnFragmentInteractionListener,
        PhotoCommentsRecyclerAdapter.ItemClickListener,
        CanvasFragment.OnFragmentInteractionListener {


    private static final String TAG = StackActivity.class.getName();


    private PagerAdapter mPagerAdapter;
    private ArrayList<Bitmap> mDatasetPhotos = new ArrayList<>();

    private RecyclerView mRecyclerViewComments;
    private RecyclerView.LayoutManager mLayoutManagerComments;
    private PhotoCommentsRecyclerAdapter mAdapterPhotoComments;
    private ArrayList<PhotoComment> mDatasetPhotoComments = new ArrayList<>();
    private HashMap<String, User> mUserHashMap = new HashMap<>();

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
        String photoId = getIntent().getStringExtra("PHOTO_ID");
        if (photoId != null && photoId.length() > 0) {
            // Photo mode

            // Download photo
            new DownloadPhotoTask().execute(photoId);

            // Get the comments for the photo
            new DownloadPhotoCommentsTask().execute(photoId);

        } else if (true) {
            // Stack mode
            finish();
        }


        ViewPager pager = (ViewPager) findViewById(R.id.view_pager_stack);
        //mPagerAdapter = new StackPhotoPagerAdapter(getSupportFragmentManager(), mDatasetPhotos);

        mPagerAdapter = new StackPhotoPagerAdapter(this, mDatasetPhotos);
        pager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout_stack);
        tabLayout.setupWithViewPager(pager, true);


        // Initialize RecyclerView
        mRecyclerViewComments = (RecyclerView) findViewById(R.id.recycler_comments);

        // Initialize Dataset
        // TODO Initialize Dataset

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManagerComments = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // set up the RecyclerView
        mRecyclerViewComments.setLayoutManager(mLayoutManagerComments);
        mAdapterPhotoComments = new PhotoCommentsRecyclerAdapter(this, mDatasetPhotoComments, mUserHashMap);
        mAdapterPhotoComments.setClickListener(this);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerViewComments.setAdapter(mAdapterPhotoComments);

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
        // User picture was pressed
        // Go to the canvas of that profile


        /*
        // Start canvas fragment
        Fragment fragment = null;
        Class fragmentClass = CanvasFragment.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.
                beginTransaction()
                //.replace(R.id.flContent, fragment)
                .addToBackStack(fragmentClass.getName())
                .commit();

        */


        // Start the canvas fragment via the main activity
        Intent openFragmentBIntent = new Intent(this, MainActivity.class);
        openFragmentBIntent.putExtra(MainActivity.OPEN_FRAGMENT_CANVAS, true);
        startActivity(openFragmentBIntent);


        // Start canvas fragment
        /*
        Fragment fragment = null;
        Class fragmentClass = CanvasFragment.class;

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
                .addToBackStack(fragmentClass.getName())
                .commit();
        */


        /*
        // Start the Canvas Activity
        Intent intent = new Intent(this, CanvasActivity.class);
        startActivity(intent);
        */


    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    private class DownloadPhotoTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {
            // Get the photo
            final String PHOTO_ID = params[0];
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this,
                    "us-east-1:4c7583cd-9c5a-4175-b39e-8690323a893e", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            // Query for photos
            Photo result = mapper.load(Photo.class, PHOTO_ID);

            if (result == null || result.getPhotoId().isEmpty()) {
                Log.e(TAG, "Error: Unable to retrieve photo.");
                finish(); // TODO notify user
            }

            mDatasetPhotos.clear();

            // Convert photo string to bitmap
            String photoString = result.getPhoto();
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

    private class DownloadPhotoCommentsTask extends AsyncTask<String, Void, List<PhotoComment>> {

        protected List<PhotoComment> doInBackground(String... params) {
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

            // Query for PhotoComments
            PhotoComment queryPhotoComment = new PhotoComment();
            queryPhotoComment.setPhotoId(photoId);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withIndexName("PhotoId-CommentDate-index")
                    .withHashKeyValues(queryPhotoComment)
                    .withScanIndexForward(true) // Set sort forward to true so oldest comment is retrieved first
                    .withConsistentRead(false); //Consistent read must be false when using GSI

            return mapper.query(PhotoComment.class, queryExpression);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(List<PhotoComment> result) {

            if(result.size() <= 0) {
                // TODO: if there's no comments, show an empty view or something
            } else {
                // Add comments to dataset and notify adapter
                mDatasetPhotoComments.addAll(result);

                // Put all the unique userIds in an array to download all the user's data
                //ArrayList<String> userIdList = new ArrayList<>();
                for(PhotoComment photoComment : result) {
                    mUserHashMap.put(photoComment.getUserId(), null);
                }

                new DownloadUsersTask().execute();
                //mAdapterPhotoComments.notifyDataSetChanged();
            }
        }
    }


    private class DownloadUsersTask extends AsyncTask<Void, Void, Void> {
        DynamoDBMapper mapper;


        protected Void doInBackground(Void... params) {

            // Iterate through users and load their info
            Iterator it = mUserHashMap.keySet().iterator();
            while (it.hasNext()) {
                String key = (String)it.next();
                //String key = entry.getKey().toString();
                it.remove(); // avoids a ConcurrentModificationException (we already have the key anyway)


                User user = mapper.load(User.class, key);

                if(user != null && !user.getUserId().isEmpty()) {
                    mUserHashMap.put(key, user);
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this,
                    "us-east-1:4c7583cd-9c5a-4175-b39e-8690323a893e", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            mapper = new DynamoDBMapper(ddbClient);

        }

        protected void onPostExecute(Void result) {

            mAdapterPhotoComments.notifyDataSetChanged();
        }

    }
}
