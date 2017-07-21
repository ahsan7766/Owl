package com.ourwayoflife.owl.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.activities.LoginActivity;
import com.ourwayoflife.owl.activities.StackActivity;
import com.ourwayoflife.owl.adapters.CanvasOuterRecyclerAdapter;
import com.ourwayoflife.owl.models.CanvasTile;
import com.ourwayoflife.owl.models.Photo;
import com.ourwayoflife.owl.models.Stack;
import com.ourwayoflife.owl.models.StackPhoto;
import com.ourwayoflife.owl.models.User;
import com.ourwayoflife.owl.views.ProfilePictureView;

/**
 * Created by Zach on 5/23/17.
 */

public class CanvasFragment extends Fragment
    implements CanvasOuterRecyclerAdapter.OuterItemClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "USER_ID";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mUserId = LoginActivity.sUserId; // Default the userId to the logged in user
    private String mParam2;

    private User mUser; // User object that can be used in multiple places

    private static final String TAG = CanvasFragment.class.getName();
    public static final int COLUMN_COUNT = 7; // number of columns of pictures in the grid
    public static final int ROW_COUNT = 8; // number of rows of pictures in the grid


    private ProfilePictureView mProfilePictureView;
    private Button mButtonViewProfile;

    protected RecyclerView mRecyclerView;
    protected CanvasOuterRecyclerAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected CanvasTile[][] mDataset = new CanvasTile[ROW_COUNT][COLUMN_COUNT];


    private OnFragmentInteractionListener mListener;

    public CanvasFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CanvasFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CanvasFragment newInstance(String userId, String param2) {
        CanvasFragment fragment = new CanvasFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, userId);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get args
        if (getArguments() != null) {
            // Get the UserId of the profile we are viewing
            mUserId = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // If no userId was found, set it to the signed in user
        if(mUserId == null || mUserId.isEmpty()) {
            mUserId = LoginActivity.sUserId;
        }

        // Initialize dataset, this data would usually come from a local content provider or
        //initDataset();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_canvas, container, false);
        rootView.setTag(TAG);



        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_canvas_outer);

        //mRecyclerView.setHasFixedSize(true); //TODO see if this works
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);



        // set up the RecyclerView
        mAdapter = new CanvasOuterRecyclerAdapter(getActivity(), mDataset);
        mAdapter.setInnerClickListener(this);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);


        // Set the profile picture
        mProfilePictureView = (ProfilePictureView) rootView.findViewById(R.id.profile_picture);


        mButtonViewProfile = (Button) rootView.findViewById(R.id.button_view_profile);
        mButtonViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start profile fragment
                ProfileFragment fragment = ProfileFragment.newInstance(mUserId, null);

                // Insert the fragment by replacing any existing fragment
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.
                        beginTransaction()
                        .replace(R.id.flContent, fragment, fragment.getClass().getName())
                        .addToBackStack(fragment.getClass().getName())
                        .commit();

                // Set action bar title
                getActivity().setTitle("User");
            }
        });



        /*
        final FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0)
                    fab.hide();
                else if (dy < 0)
                    fab.show();
            }
        });
        */

        //return inflater.inflate(R.layout.fragment_feed, container, false);


        new DownloadUserTask().execute();

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

        // Get the stacks for the user we are viewing
        new GetStacksTask().execute();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CanvasFragment.OnFragmentInteractionListener) {
            mListener = (CanvasFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Set title bar
        getActivity().setTitle(getString(R.string.title_fragment_canvas));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    /*
    private void initDataset() {
        mDataset = new CanvasTile[ROW_COUNT][COLUMN_COUNT];
        for (int i = 0; i < ROW_COUNT; i++) {
            mDataset[i] = new CanvasTile[COLUMN_COUNT];

            for (int x = 0; x < COLUMN_COUNT; x++) {
                mDataset[i][x] = new CanvasTile("ROW " + i + " COL " + x);
            }

        }
    }
    */


    // Handles clicks from in the canvas
    @Override
    public void onOuterItemClick(View view, int row, int column) {
        Log.d(TAG, "CANVAS ITEM CLICKED: Row " + row + ", Col " + column);

        CanvasTile canvasTile = mDataset[row][column];
        if(canvasTile == null) {
            // Don't start stack activity if we don't have a canvasTile in the position that was clicked
            return;
        }

        final String STACK_ID = mDataset[row][column].getStackId();
        if(STACK_ID == null || STACK_ID.isEmpty()) {
            // Don't start stack activity if we don't have a stackId to pass it
            return;
        }

        // Send the user to the StackActivity for the stack the just clicked on
        Intent intent = new Intent(view.getContext(), StackActivity.class);
        intent.putExtra("USER_ID", mUserId);
        intent.putExtra("STACK_ID", STACK_ID);
        view.getContext().startActivity(intent);
    }


    private class GetStacksTask extends AsyncTask<Void, Void, Integer> {

        protected Integer doInBackground(Void... voids) {

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

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            // Query for stacks
            Stack queryStack = new Stack();
            queryStack.setUserId(mUserId); // Set userId to the userId of the canvas we are viewing

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withIndexName("UserId-CreatedDate-index")
                    .withHashKeyValues(queryStack)
                    //.withRangeKeyCondition("Title", rangeKeyCondition)
                    .withScanIndexForward(false)
                    .withConsistentRead(false); //Cannot use consistent read on GSI


            PaginatedQueryList<Stack> stackList = mapper.query(Stack.class, queryExpression);



            // Now that we have the list of stacks, get the first picture of each stack to set the canvas tiles
            //mDataset = new CanvasTile[ROW_COUNT][COLUMN_COUNT];

            if(stackList == null) {
                // Stack list was not found.  Don't try inflating the canvas tiles
                return null;
            }

            int stackCount = 0;
            for (int i = 0; i < ROW_COUNT; i++) {

                mDataset[i] = new CanvasTile[COLUMN_COUNT]; // TODO don't think this is necessary

                for (int x = 0; x < COLUMN_COUNT; x++) {
                    if(stackCount >= stackList.size()) {
                        // If we have reached the number of stacks the user has, stop inflating dataset
                        return stackCount;
                    }
                    // Just set the stackId of the canvas tile for now
                    Stack stack = stackList.get(stackCount);
                    mDataset[i][x] = new CanvasTile(stack.getStackId(), stack.getName(), null);

                    stackCount++;
                }

            }

            return stackCount;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Integer stackCount) {
            // TODO: check this.exception
            // TODO: do something with the feed

            // Clear dataset, add new items, then notify
            //mAdapter.notifyDataSetChanged();

            // If there are stacks, run task to get cover photos
            if(stackCount > 0) {
                new DownloadStackCoverPhotoTask().execute(stackCount);
            }
        }
    }


    private class DownloadStackCoverPhotoTask extends AsyncTask<Integer, Void, Void> {

        protected Void doInBackground(Integer... params) {

            int numOfStacks = params[0];

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getActivity(), // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);


            // First get the photoIds for each tile by querying the StackPhoto table

            // Loop through canvas
            int stackCount = 0;
            for (int i = 0; i < ROW_COUNT; i++) {

                for (int x = 0; x < COLUMN_COUNT; x++) {

                    if(stackCount >= numOfStacks) {
                        // If we have reached the number of stacks the user has, stop querying
                        break;
                    }

                    String stackId = mDataset[i][x].getStackId();

                    // Make sure we have a stackId
                    if(stackId == null || stackId.isEmpty()) {
                        stackCount++;
                        continue;
                    }


                    StackPhoto queryStackPhoto = new StackPhoto();
                    queryStackPhoto.setStackId(stackId);


                    // TODO need to make sure we are getting the most recent photo by sorting/indexing by AddedDate
                    DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                            .withHashKeyValues(queryStackPhoto)
                            .withLimit(1) // Only want the first one
                            //.withRangeKeyCondition("Title", rangeKeyCondition)
                            .withScanIndexForward(false)
                            .withConsistentRead(false);


                    PaginatedQueryList<StackPhoto> stackPhotoList = mapper.query(StackPhoto.class, queryExpression);

                    // Make sure we found a StackPhoto
                    if(stackPhotoList == null || stackPhotoList.isEmpty()) {
                        stackCount++;
                        continue;
                    }



                    /*
                    Photo queryPhoto = new Photo();
                    queryPhoto.setPhotoId(stackPhotoList.get(0).getPhotoId());


                    // Now use the PhotoId of the stack photo to query for the photo
                    DynamoDBQueryExpression queryExpressionPhoto = new DynamoDBQueryExpression()
                            .withIndexName("UserId-UploadDate-index")
                            .withHashKeyValues(queryPhoto)
                            //.withRangeKeyCondition("Title", rangeKeyCondition)
                            .withConsistentRead(false)
                            .withScanIndexForward(false);

                    */

                    Photo photo = mapper.load(Photo.class, stackPhotoList.get(0).getPhotoId());


                    Bitmap bitmap;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inSampleSize = 4;
                    // Convert the photo list to a FeedItem list

                    //Check if the bitmap is cached
                    bitmap = FeedFragment.getBitmapFromMemCache(photo.getPhotoId());
                    if (bitmap == null) {
                        //Bitmap is not cached.  Have to download


                        // Convert the photo string to a bitmap
                        String photoString = photo.getPhoto();
                        if (photoString == null || photoString.length() <= 0) {
                            stackCount++;
                            continue;
                        }
                        try {
                            byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                            bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length, options);

                            //Add bitmap to the cache
                            FeedFragment.addBitmapToMemoryCache(String.valueOf(photo.getPhotoId()), bitmap);

                        } catch (Exception e) {
                            Log.e(TAG, "Conversion from String to Bitmap: " + e.getMessage());
                            stackCount++;
                            continue;
                        }
                    }

                    // Set the photo of the dataset position we are loading a photo of
                    if(bitmap != null) {
                        mDataset[i][x].setPhoto(bitmap);
                    }

                    stackCount++;
                }

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

            // Clear dataset, add new items, then notify
            mAdapter.notifyDataSetChanged();
            //mAdapter.notifyInnerDatasetRowsChanged();
        }
    }


    private class DownloadUserTask extends AsyncTask<Void, Void, User> {

        protected User doInBackground(Void... urls) {
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
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            // Load user
            return mapper.load(User.class, mUserId);
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


            // If the user is not retrieved, then close the fragment
            if (user == null || user.getUserId() == null || user.getUserId().isEmpty()) {
                Toast.makeText(getActivity(), "Unable to retrieve user data", Toast.LENGTH_SHORT).show();
                // End fragment by popping itself from the stack
                getActivity().getSupportFragmentManager().beginTransaction().remove(CanvasFragment.this).commit();
                return;
            }

            // Set the class' user variable
            mUser = user;

            // Update the UI to whatever the user's data is
            // Get the user's profile picture bitmap
            //Check if the bitmap is cached
            Bitmap userBitmap;
            BitmapFactory.Options optionsUser = new BitmapFactory.Options();
            //options.inSampleSize = 4;
            userBitmap = FeedFragment.getBitmapFromMemCache("u" + mUser.getUserId()); // Added a 'u' in front in case there is an overlap between a userId and photoId
            userPhoto:
            if (userBitmap == null) {
                //Bitmap is not cached.  Have to download

                // Convert the photo string to a bitmap
                String photoString = mUser.getPhoto();
                if (photoString == null || photoString.length() <= 0) {
                    break userPhoto;
                }
                try {
                    byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                    userBitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length, optionsUser);

                    //Add bitmap to the cache
                    FeedFragment.addBitmapToMemoryCache(String.valueOf("u" + mUser.getUserId()), userBitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Conversion from String to Bitmap: " + e.getMessage());
                }
            }

            mProfilePictureView.setBitmap(userBitmap);

            //mTextUserName.setText(mUser.getName());

        }
    }

}
