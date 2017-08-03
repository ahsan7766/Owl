package com.ourwayoflife.owl.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.activities.LoginActivity;
import com.ourwayoflife.owl.activities.MainActivity;
import com.ourwayoflife.owl.activities.StackActivity;
import com.ourwayoflife.owl.adapters.FeedRecyclerAdapter;
import com.ourwayoflife.owl.models.FeedItem;
import com.ourwayoflife.owl.models.Photo;
import com.ourwayoflife.owl.models.PhotoLike;
import com.ourwayoflife.owl.models.User;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Zach on 7/25/17.
 */

public class LikesFragment extends Fragment
        implements FeedRecyclerAdapter.ImageClickListener,
        FeedRecyclerAdapter.ProfileClickListener,
        FeedRecyclerAdapter.ItemCheckedChangeListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "USER_ID";

    // Parameters
    private String mUserId;

    private static final String TAG = LikesFragment.class.getName();

    private static final int SPAN_COUNT = 1; // number of columns in the grid

    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerView mRecyclerFeed;
    protected TextView mTextEmptyFeed;
    protected FeedRecyclerAdapter mAdapterFeed;
    protected RecyclerView.LayoutManager mLayoutManagerFeed;
    protected ArrayList<FeedItem> mDatasetFeed = new ArrayList<>();

    private HashMap<String, User> mUserHashMap = new HashMap<>(); // Used so we don't have to repeat querying for user data

    private boolean isUpdatingDataset = false;

    private DownloadTask mDownloadTask;
    private PhotoLikeTask mPhotoLikeTask;

    public LikesFragment() {
        // Required empty constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId The userId who we are viewing the likes of.
     * @return A new instance of fragment LikesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LikesFragment newInstance(String userId) {
        LikesFragment fragment = new LikesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getString(ARG_PARAM1);
        }

        // If no userId was found show error
        if (mUserId == null || mUserId.isEmpty()) {
            Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Started LikesFragment without passing UserId.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_likes, container, false);
        rootView.setTag(TAG);


        // Set up recycler and its empty view
        mRecyclerFeed = rootView.findViewById(R.id.recycler_feed);
        mTextEmptyFeed = rootView.findViewById(R.id.text_empty_feed);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManagerFeed = new GridLayoutManager(getActivity(), SPAN_COUNT);

        // set up the RecyclerView
        mRecyclerFeed.setLayoutManager(mLayoutManagerFeed);
        mAdapterFeed = new FeedRecyclerAdapter(getActivity(), mDatasetFeed);
        mAdapterFeed.setImageClickListener(this);
        mAdapterFeed.setProfileClickListener(this);
        //mAdapterFeed.setDragListener(this);
        mAdapterFeed.setOnCheckedChangeListener(this);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerFeed.setAdapter(mAdapterFeed);

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);

        // Set up show/hide animation for fab
        final FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();
        mRecyclerFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    fab.hide();
                else if (dy < 0)
                    fab.show();
            }
        });


        //return inflater.inflate(R.layout.fragment_feed, container, false);
        return rootView;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize dataset, this data would usually come from a local content provider or
        if (savedInstanceState != null && savedInstanceState.getParcelableArrayList("FEED_ITEMS") != null) {
            //Restore the fragment's state here
            Log.d("TAG", savedInstanceState.toString());
            mDatasetFeed = savedInstanceState.getParcelableArrayList("FEED_ITEMS");
            Log.d("TAG", "Restoring FeedItem Dataset. Dataset Size: " + mDatasetFeed.size());
        } else {
            // Retrieve data
            //new DownloadTask().execute(VIEW_FEED);
            //mDownloadTask.cancel(true); // Make sure any previous mDownloadTask is cancelled
            mDownloadTask = new DownloadTask();
            mDownloadTask.execute();
        }

        // Setup refresh listener which triggers new data loading
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.

                if (mDownloadTask != null && mDownloadTask.getStatus() == AsyncTask.Status.RUNNING) {
                    mDownloadTask.cancel(true); // Make sure any previous mDownloadTask is cancelled
                }
                mDownloadTask = new DownloadTask();
                mDownloadTask.execute();


            }
        });

    }


    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link android.app.Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();

        // If any of the async tasks are running, cancel them

        if (mDownloadTask != null && mDownloadTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mDownloadTask.cancel(true);
        }

        if (mPhotoLikeTask != null && mPhotoLikeTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mPhotoLikeTask.cancel(true);
        }

    }

    /**
     * Called when the fragment is visible to the user and actively running.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Set title bar
        getActivity().setTitle(getString(R.string.title_fragment_likes));
    }


    // Handle a photo in the feed being clicked
    @Override
    public void onImageClick(View view, int position) {
        Intent intent = new Intent(getContext(), StackActivity.class);
        intent.putExtra("USER_ID", mDatasetFeed.get(position).getUserId());
        intent.putExtra("PHOTO_ID", mDatasetFeed.get(position).getPhotoId());
        view.getContext().startActivity(intent);
    }

    // Handle a profile in the feed being clicked
    @Override
    public void onProfileClick(View view, int position) {
        // Start canvas fragment
        // Pass in the UserId of the photo that was clicked
        CanvasFragment fragment = CanvasFragment.newInstance(mDatasetFeed.get(position).getUserId());

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.
                beginTransaction()
                .replace(R.id.flContent, fragment, fragment.getClass().getName())
                .addToBackStack(fragment.getClass().getName())
                .commit();

        // Set action bar title
        getActivity().setTitle(getString(R.string.title_fragment_canvas));
    }


    @Override
    public void onItemCheckedChange(CompoundButton compoundButton, boolean isChecked, int position) {
        // Only like the photo if we are not in the middle of updating the dataset
        // Also make sure the button is pressed, otherwise we will inadvertently  fire this listener during scrolls
        if (compoundButton.isPressed() && !isUpdatingDataset) {
            mPhotoLikeTask = new PhotoLikeTask();
            mPhotoLikeTask.execute(position);
        }
    }


    private class DownloadTask extends AsyncTask<Void, FeedItem, List<FeedItem>> {

        int addedCount = 0; // Keeps track of how many new photos we are adding to the dataset
        int origDatasetSize = 0; // How many were originally in the dataset

        protected List<FeedItem> doInBackground(Void... params) {


            // ARN:  arn:aws:dynamodb:us-east-1:971897998846:table/photo

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(), // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient, credentialsProvider);


            List<Photo> result = new ArrayList<>(); // Array that the results will be stored in

            // Show only the photos that this person has liked
            // First get all the PhotoLike's for the user
            PhotoLike queryPhotoLike = new PhotoLike();
            queryPhotoLike.setUserId(mUserId);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withIndexName("UserId-LikeDate-index")
                    .withHashKeyValues(queryPhotoLike)
                    .withScanIndexForward(false) // Sort if by most recently liked first
                    .withConsistentRead(false); // Can't use consistent read on GSI

            List<PhotoLike> photoLikeList = mapper.query(PhotoLike.class, queryExpression);

            // Now get the Photos using the PhotoId in the PhotoLike objects
            for (PhotoLike photoLike : photoLikeList) {
                Photo photo = mapper.load(Photo.class, photoLike.getPhotoId());
                if(!photo.isDeleted()) {
                    // Only add photo to results if it isn't deleted
                    result.add(photo);
                }
            }


            // ArrayList that the feed items will be stored in for the updated dataset
            ArrayList<FeedItem> feedItems = new ArrayList<>();


            // Convert the photo list to a FeedItem list
            for (Photo photo : result) {

                if (photo.isDeleted()) {
                    // Don't load photo if it's deleted
                    continue;
                }

                Bitmap photoBitmap;
                BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize = 4;

                //Check if the bitmap is cached
                photoBitmap = FeedFragment.getBitmapFromMemCache(photo.getPhotoId());
                if (photoBitmap == null) {
                    //Bitmap is not cached.  Have to download


                    // Convert the photo string to a bitmap
                    String photoString = photo.getPhoto();
                    if (photoString == null || photoString.length() <= 0) {
                        continue;
                    }
                    try {
                        byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                        photoBitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length, options);

                        //Add bitmap to the cache
                        FeedFragment.addBitmapToMemoryCache(String.valueOf(photo.getPhotoId()), photoBitmap);

                    } catch (Exception e) {
                        Log.e(TAG, "Conversion from String to Bitmap: " + e.getMessage());
                        e.printStackTrace();
                        continue; // Don't add this to the feed
                    }
                }


                final String USER_ID = photo.getUserId();
                User user;

                // Put all the unique userIds in an array to download all the user's data
                if (!mUserHashMap.containsKey(USER_ID) || mUserHashMap.get(USER_ID) == null) {
                    // Make sure we aren't overriding an existing entry just in case their data is already loaded.  We don't want to overwrite it with null
                    //mUserHashMap.put(USER_ID, null);

                    user = mapper.load(User.class, USER_ID);
                } else {
                    // We already have this user loaded
                    user = mUserHashMap.get(USER_ID);
                }


                if (user == null) {
                    Log.e(TAG, "Could not load user information. UserId: " + USER_ID);
                    continue;
                }


                // Get the user's profile picture bitmap
                //Check if the bitmap is cached
                Bitmap userBitmap;
                BitmapFactory.Options optionsUser = new BitmapFactory.Options();
                //options.inSampleSize = 4;
                userBitmap = FeedFragment.getBitmapFromMemCache("u" + user.getUserId()); // Added a 'u' in front in case there is an overlap between a userId and photoId
                userPhoto:
                if (userBitmap == null) {
                    //Bitmap is not cached.  Have to download

                    // Convert the photo string to a bitmap
                    String photoString = user.getPhoto();
                    if (photoString == null || photoString.length() <= 0) {
                        break userPhoto;
                    }
                    try {
                        byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                        userBitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length, optionsUser);

                        //Add bitmap to the cache
                        FeedFragment.addBitmapToMemoryCache(String.valueOf("u" + user.getUserId()), userBitmap);
                    } catch (Exception e) {
                        Log.e(TAG, "Conversion from String to Bitmap: " + e.getMessage());
                        e.printStackTrace();
                        continue; // Don't add this to the feed
                    }
                }


                // Figure out of the user has previously liked the photo
                PhotoLike photoLike = mapper.load(PhotoLike.class, photo.getPhotoId(), LoginActivity.sUserId);
                boolean isLiked = (photoLike != null && photoLike.getLikeDate() != null && !photoLike.getLikeDate().isEmpty()); // If photoLike isn't null, then user currently 'likes' this photo

                //FeedItem feedItem = new FeedItem(photo.getStackId(), bitmap, "Stack Title", 4);
                FeedItem feedItem = new FeedItem(photo.getPhotoId(), photoBitmap, USER_ID, userBitmap, user.getName(), isLiked);
                feedItems.add(feedItem);

                publishProgress(feedItem);
            } // for photo : result

            return feedItems;
        }

        @Override
        protected void onProgressUpdate(FeedItem... values) {
            super.onProgressUpdate(values);
            FeedItem feedItem = values[0];

            if (mSwipeRefreshLayout.isRefreshing()) {
                // If we are refreshing, then check to see if we already have this item in the dataset
                // If we do, then we can stop downloading because we should already have the rest (since it's sorted by date)
                // TODO I think there are issues with this.  Need to re-visit
                boolean isDatasetAlreadyHaveThisPhoto = false;
                for (FeedItem feedItemLoop : mDatasetFeed) {
                    if (feedItemLoop.equals(feedItem)) {
                        isDatasetAlreadyHaveThisPhoto = true;
                        break; // Break the loop
                    }
                }
                if (isDatasetAlreadyHaveThisPhoto) {
                    cancel(true);
                } else {
                    mDatasetFeed.add(addedCount, feedItem);
                    mAdapterFeed.notifyItemInserted(addedCount);

                    addedCount++;
                }

            } else {
                // If we aren't refreshing, then were adding each item to the dataset (it was cleared pre-execute)
                mDatasetFeed.add(feedItem);
                mAdapterFeed.notifyItemChanged(mDatasetFeed.size());

                addedCount++;
            }


        }

        @Override
        protected void onCancelled(List<FeedItem> feedItems) {
            super.onCancelled(feedItems);
            isUpdatingDataset = false;

            // If we were refreshing
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false); // Make sure we stop the refreshing spinner
                mRecyclerFeed.smoothScrollToPosition(0);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            isUpdatingDataset = false;

            // If we were refreshing
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false); // Make sure we stop the refreshing spinner
                mRecyclerFeed.smoothScrollToPosition(0);
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            isUpdatingDataset = true;

            // Clear the dataset and recyclerview, but not if we are just refreshing
            if (!mSwipeRefreshLayout.isRefreshing()) {
                mDatasetFeed.clear();
                mAdapterFeed.notifyDataSetChanged();
            }

            // Store the original size of the dataset
            origDatasetSize = mDatasetFeed.size();
        }

        protected void onPostExecute(List<FeedItem> result) {
            // TODO: check this.exception
            // TODO: do something with the feed


            if (mDatasetFeed.isEmpty()) {
                // If there are no photos, hide the recycler and show the empty view
                mRecyclerFeed.setVisibility(View.GONE);
                mTextEmptyFeed.setVisibility(View.VISIBLE);
            } else {
                // Otherwise, make sure the recycler is shown
                mRecyclerFeed.setVisibility(View.VISIBLE);
                mTextEmptyFeed.setVisibility(View.GONE);
            }

            isUpdatingDataset = false;

            // If we were refreshing
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false); // Make sure we stop the refreshing spinner
                mRecyclerFeed.smoothScrollToPosition(0);
            }
        }
    }


    private class PhotoLikeTask extends AsyncTask<Integer, Void, Integer> {

        protected Integer doInBackground(Integer... params) {
            // Get the photo like bool
            final int position = params[0];

            final Boolean isLiked = !mDatasetFeed.get(position).getLiked(); // Like should be the opposite of whatever it is now

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(), // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient, credentialsProvider);

            PhotoLike photoLike = new PhotoLike();
            photoLike.setPhotoId(mDatasetFeed.get(position).getPhotoId());
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

            // Update the position in the dataset (only if we are still in the same view)
            if (!isUpdatingDataset) {
                mDatasetFeed.get(position).setLiked(isLiked);
            }


            return position;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

        }


        protected void onPostExecute(Integer position) {
            // Notify the adapter

            // If we are viewing the user that is logged in, we need to remove this photo from the list
            // It's impossible to "Like" a photo while in the like view, because we are only viewing photos that we were previously liked
            if (mUserId.equals(LoginActivity.sUserId)) {
                mDatasetFeed.remove(position.intValue());
                mAdapterFeed.notifyItemRemoved(position);
            } else {
                // We are not viewing the user that is logged in

                // Only update the adapter if we haven't already changed views
                if (!isUpdatingDataset) {
                    mAdapterFeed.notifyItemChanged(position);
                }
            }


        }
    }



}
