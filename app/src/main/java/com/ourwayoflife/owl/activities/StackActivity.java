package com.ourwayoflife.owl.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.adapters.PhotoCommentsRecyclerAdapter;
import com.ourwayoflife.owl.adapters.StackPhotoPagerAdapter;
import com.ourwayoflife.owl.fragments.CanvasFragment;
import com.ourwayoflife.owl.fragments.StackPhotoPagerFragment;
import com.ourwayoflife.owl.models.Photo;
import com.ourwayoflife.owl.models.PhotoComment;
import com.ourwayoflife.owl.models.PhotoLike;
import com.ourwayoflife.owl.models.StackLike;
import com.ourwayoflife.owl.models.StackPhoto;
import com.ourwayoflife.owl.models.User;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class StackActivity extends AppCompatActivity
        implements StackPhotoPagerFragment.OnFragmentInteractionListener,
        PhotoCommentsRecyclerAdapter.ItemClickListener,
        CanvasFragment.OnFragmentInteractionListener {


    private static final String TAG = StackActivity.class.getName();

    private String photoId; // Used to store the PhotoId of the currently viewed photo
    private String stackId; // Used to store the StackId of the currently viewed stack
    private String userId; // Used to store the UserId of the owner of the viewed photo/stack

    private PagerAdapter mPagerAdapter;
    private ArrayList<Bitmap> mDatasetPhotos = new ArrayList<>();

    private RecyclerView mRecyclerViewComments;
    private RecyclerView.LayoutManager mLayoutManagerComments;
    private PhotoCommentsRecyclerAdapter mAdapterPhotoComments;
    private ArrayList<PhotoComment> mDatasetPhotoComments = new ArrayList<>();
    private HashMap<String, User> mUserHashMap = new HashMap<>();

    private TextView mTextLikeCount;
    private ToggleButton mToggleButtonLike;
    private int mIntLikeCount = 0; // Keeps track of number of likes

    private EditText mEditTextComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stack);

        // Enable the up navigation button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        mTextLikeCount = findViewById(R.id.text_like_count);

        mToggleButtonLike = findViewById(R.id.button_like);

        // Get extras data
        photoId = getIntent().getStringExtra("PHOTO_ID");
        stackId = getIntent().getStringExtra("STACK_ID");
        userId = getIntent().getStringExtra("USER_ID");

        // Make sure we have a UserId
        if (userId == null || userId.isEmpty()) {
            Log.wtf(TAG, "USER_ID not found, but StackActivity was launched. Finishing Activity...");
            finish();
        }

        // If a photo was included in the extras, then the activity is in photo mode
        if (photoId != null && photoId.length() > 0) {
            // Photo mode
            setTitle("Photo"); // Set Title

            // Check if photo was previously liked
            // TODO instead of this, just pass a boolean to this activity that says if the photo
            // was already liked.  If that bool extra is missing, then run this AsyncTask
            new CheckPhotoLikeTask().execute();

            new CheckPhotoLikeCountTask().execute();

            // Download photo
            new DownloadPhotoTask().execute();

            // Get the comments for the photo
            new DownloadPhotoCommentsTask().execute();

        } else if (stackId != null && stackId.length() > 0) {
            // Stack mode

            // Check if photo was previously liked
            // TODO instead of this, (if easier) just pass a boolean to this activity that says if the stack
            // was already liked.  If that bool extra is missing, then run this AsyncTask
            new CheckStackLikeTask().execute();

            new CheckStackLikeCountTask().execute();

            new DownloadStackPhotosTask().execute();

            //finish();
        } else {
            Log.wtf(TAG, "PHOTO_ID and STACK_ID both not found, but StackActivity was launched. Finishing Activity...");
            finish();
        }


        ViewPager pager = findViewById(R.id.view_pager_stack);
        //mPagerAdapter = new StackPhotoPagerAdapter(getSupportFragmentManager(), mDatasetPhotos);

        mPagerAdapter = new StackPhotoPagerAdapter(this, mDatasetPhotos);
        pager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout_stack);
        tabLayout.setupWithViewPager(pager, true);


        // TODO Change number of likes based on query


        // Initialize RecyclerView
        mRecyclerViewComments = findViewById(R.id.recycler_comments);

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


        // Set up comment send edittext and button
        mEditTextComment = findViewById(R.id.edit_text_comment);
        ImageButton imageButtonComment = findViewById(R.id.image_buton_comment);
        imageButtonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = mEditTextComment.getText().toString();

                // Check that comment isn't empty
                if (!comment.isEmpty()) {
                    PhotoComment photoComment = new PhotoComment();
                    photoComment.setUserId(LoginActivity.sUserId);
                    photoComment.setComment(comment);

                    DateTime dt = new DateTime(DateTimeZone.UTC);
                    DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
                    final String dateString = fmt.print(dt);
                    photoComment.setCommentDate(dateString);

                    photoComment.setPhotoId(photoId);

                    // Upload the comment
                    new UploadCommentTask().execute(photoComment);
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // If we are viewing a photo and the UserId of the stack is the logged in user, show the delete photo option
        if (photoId != null && !photoId.isEmpty() && userId.equals(LoginActivity.sUserId)) {
            menu.findItem(R.id.action_delete).setVisible(true);
        }

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
        int id = item.getItemId();
        switch (id) {
            // This is a hack-ish way to make it so that when the back arrow is pressed
            // in the action bar, instead of re-starting the main activity and sending the user
            // back to the default tab, they are sent back to whatever tab they were on last.
            case android.R.id.home:
                super.onBackPressed();
                return true;

            case R.id.action_delete:
                Toast.makeText(this, "DELETE PHOTO FEATURE NOT YET IMPLEMENTED", Toast.LENGTH_SHORT).show(); // TODO Delete photo feature
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

    // Used to format the number of likes on a photo
    private void updateLikeCountUI() {
        // TODO evenually add formatting for large numbers with locale (Ex: 1k instead of 1,000)
        mTextLikeCount.setText(NumberFormat.getInstance().format(mIntLikeCount));
    }

    private class DownloadPhotoTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            // Get the photo

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            // Query for photos
            Photo result = mapper.load(Photo.class, photoId);

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


    private class DownloadStackPhotosTask extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void... params) {

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            // Get the StackPhotos
            StackPhoto queryStackPhoto = new StackPhoto();
            queryStackPhoto.setStackId(stackId);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(queryStackPhoto)
                    .withConsistentRead(false);

            List<StackPhoto> stackPhotoList = mapper.query(StackPhoto.class, queryExpression);

            if (stackPhotoList == null) {
                return false;
            }

            mDatasetPhotos.clear(); // Clear dataset in case it isn't already for some reason

            // Query for photos
            for (StackPhoto stackPhoto : stackPhotoList) {

                // Make sure we have a photoId
                if (stackPhoto.getPhotoId() == null || stackPhoto.getPhotoId().isEmpty()) {
                    continue;
                }

                Photo photo = mapper.load(Photo.class, stackPhoto.getPhotoId());

                if (photo == null || photo.getPhotoId().isEmpty()) {
                    Log.e(TAG, "Error: Unable to retrieve photo.");
                    continue; // TODO notify user
                }

                // Convert photo string to bitmap
                String photoString = photo.getPhoto();
                try {
                    byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    mDatasetPhotos.add(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Conversion from String to Bitmap: " + e);
                    continue; // TODO notify user
                }

            }

            return true;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Boolean result) {
            // TODO: check this.exception
            // TODO: do something with the feed

            if (!result) {
                // No photo likes found or error occured
                return;
            }

            mPagerAdapter.notifyDataSetChanged();
        }

    }


    private class DownloadPhotoCommentsTask extends AsyncTask<Void, Void, List<PhotoComment>> {

        protected List<PhotoComment> doInBackground(Void... params) {

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
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

            if (result.size() <= 0) {
                // TODO: if there's no comments, show an empty view or something
            } else {
                // Add comments to dataset and notify adapter
                mDatasetPhotoComments.addAll(result);

                // Put all the unique userIds in an array to download all the user's data
                //ArrayList<String> userIdList = new ArrayList<>();
                for (PhotoComment photoComment : result) {
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
                String key = (String) it.next();
                //String key = entry.getKey().toString();
                it.remove(); // avoids a ConcurrentModificationException (we already have the key anyway)


                User user = mapper.load(User.class, key);

                if (user != null && !user.getUserId().isEmpty()) {
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
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            mapper = new DynamoDBMapper(ddbClient);

        }

        protected void onPostExecute(Void result) {

            mAdapterPhotoComments.notifyDataSetChanged();
        }

    }


    private class UploadCommentTask extends AsyncTask<PhotoComment, Void, PhotoComment> {

        protected PhotoComment doInBackground(PhotoComment... params) {
            // Get the photo
            PhotoComment photoComment = params[0];

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            // Save PhotoComment
            mapper.save(photoComment);

            if (photoComment.getCommentId() == null || photoComment.getCommentId().isEmpty()) {
                Log.e(TAG, "Error: Unable to post comment.");
                cancel(true); // TODO notify user that comment couldn't be posted
            }

            return photoComment;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(PhotoComment photoComment) {
            // Clear the comment EditText since the comment has sent
            mEditTextComment.setText("");

            // Now that comment is posted, add the comment the photo comments
            //new DownloadPhotoCommentsTask().execute(photoId);
            mDatasetPhotoComments.add(photoComment);
            mAdapterPhotoComments.notifyItemInserted(mDatasetPhotoComments.size());
        }

    }


    /**
     * Checks if the photo has previously been liked by this user.
     * If it has been, then set the toggle button to checked, otherwise set to unchecked
     */
    private class CheckPhotoLikeTask extends AsyncTask<Void, Boolean, Boolean> {

        protected Boolean doInBackground(Void... params) {

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            // Query the PhotoLike table
            PhotoLike photoLike = mapper.load(PhotoLike.class, photoId, LoginActivity.sUserId);

            // If the PhotoLike loaded is null, then return false to indicate that the photo is not liked
            return photoLike != null;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Boolean isLiked) {
            // Set checked
            mToggleButtonLike.setChecked(isLiked);

            // Now that we know if it was liked or not, set the onCheckChanged listener
            mToggleButtonLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    new PhotoLikeTask().execute(isChecked);
                }
            });
        }
    }


    /**
     * Checks if the stack has previously been liked by this user.
     * If it has been, then set the toggle button to checked, otherwise set to unchecked
     */
    private class CheckStackLikeTask extends AsyncTask<Void, Boolean, Boolean> {

        protected Boolean doInBackground(Void... params) {

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            // Query the StackLike table
            StackLike stackLike = mapper.load(StackLike.class, stackId, LoginActivity.sUserId);

            // If the StackLike loaded is null, then return false to indicate that the stack is not liked
            return stackLike != null;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Boolean isLiked) {
            // Set checked
            mToggleButtonLike.setChecked(isLiked);

            // Now that we know if it was liked or not, set the onCheckChanged listener
            mToggleButtonLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    new StackLikeTask().execute(isChecked);
                }
            });
        }
    }


    /**
     * Finds the number of likes on the photo and sets the like count
     */
    private class CheckPhotoLikeCountTask extends AsyncTask<Void, Void, Integer> {

        protected Integer doInBackground(Void... params) {

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            PhotoLike queryPhotoLike = new PhotoLike();
            queryPhotoLike.setPhotoId(photoId);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(queryPhotoLike)
                    .withConsistentRead(false);

            List<PhotoLike> photoLikeList = mapper.query(PhotoLike.class, queryExpression);

            // Return the number of likes in the photoLikeList.  If it's null, return 0
            return (photoLikeList == null ? 0 : photoLikeList.size());
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Integer likeCount) {
            // Set text of like count to the number of likes
            mIntLikeCount = likeCount;
            updateLikeCountUI();
        }
    }


    /**
     * Finds the number of likes on the stack and sets the like count
     */
    private class CheckStackLikeCountTask extends AsyncTask<Void, Void, Integer> {

        protected Integer doInBackground(Void... params) {
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            StackLike queryStackLike = new StackLike();
            queryStackLike.setStackId(stackId);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(queryStackLike)
                    .withConsistentRead(false);

            List<StackLike> stackLikeList = mapper.query(StackLike.class, queryExpression);

            // Return the number of likes in the photoLikeList.  If it's null, return 0
            return (stackLikeList == null ? 0 : stackLikeList.size());
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Integer likeCount) {
            // Set text of like count to the number of likes
            mIntLikeCount = likeCount;
            updateLikeCountUI();
        }
    }


    private class PhotoLikeTask extends AsyncTask<Boolean, Void, Boolean> {

        protected Boolean doInBackground(Boolean... params) {
            // Get the photo like bool
            final Boolean isLiked = params[0];

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            PhotoLike photoLike = new PhotoLike();
            photoLike.setPhotoId(photoId);
            photoLike.setUserId(LoginActivity.sUserId);

            // We are adding a PhotoLike to the table
            if (isLiked) {
                // Get date string
                DateTime dt = new DateTime(DateTimeZone.UTC);
                DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
                final String dateString = fmt.print(dt);
                photoLike.setLikeDate(dateString);

                // Save PhotoLike
                mapper.save(photoLike);
            } else {
                // Remove the PhotoLike from the table
                mapper.delete(photoLike);
            }

            return isLiked;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Boolean isLiked) {
            // If liked
            if (isLiked) {
                mIntLikeCount++;
            } else {
                mIntLikeCount--;
            }
            updateLikeCountUI();
        }
    }


    private class StackLikeTask extends AsyncTask<Boolean, Void, Boolean> {

        protected Boolean doInBackground(Boolean... params) {
            // Get the stack like bool
            final Boolean isLiked = params[0];

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    StackActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            StackLike stackLike = new StackLike();
            stackLike.setStackId(stackId);
            stackLike.setUserId(LoginActivity.sUserId);

            // We are adding a PhotoLike to the table
            if (isLiked) {
                // Get date string
                DateTime dt = new DateTime(DateTimeZone.UTC);
                DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
                final String dateString = fmt.print(dt);
                stackLike.setLikeDate(dateString);

                // Save StackLike
                mapper.save(stackLike);
            } else {
                // Remove the StackLike from the table
                mapper.delete(stackLike);
            }

            return isLiked;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Boolean isLiked) {
            // If liked
            if (isLiked) {
                mIntLikeCount++;
            } else {
                mIntLikeCount--;
            }
            updateLikeCountUI();
        }
    }
}
