package com.ourwayoflife.owl.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.activities.LoginActivity;
import com.ourwayoflife.owl.activities.MainActivity;
import com.ourwayoflife.owl.activities.StackActivity;
import com.ourwayoflife.owl.adapters.CanvasOuterRecyclerAdapter;
import com.ourwayoflife.owl.adapters.FeedRecyclerAdapter;
import com.ourwayoflife.owl.models.CanvasTile;
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
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment
        implements FeedRecyclerAdapter.ItemClickListener,
        FeedRecyclerAdapter.ItemLongClickListener,
        FeedRecyclerAdapter.ItemCheckedChangeListener,
        CanvasOuterRecyclerAdapter.ItemInnerDragListener {
    //FeedRecyclerAdapter.ItemDragListener,
    //CanvasInnerRecyclerAdapter.ItemDragListener {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = FeedFragment.class.getName();


    public static final int CANVAS_COLUMN_COUNT = 7; // number of columns of pictures in the grid
    public static final int CANVAS_ROW_COUNT = 2; // number of rows of pictures in the grid

    public static LruCache<String, Bitmap> mMemoryCache; // TODO move this to MainActivity


    protected RecyclerView mCanvasRecyclerView;
    protected CanvasOuterRecyclerAdapter mCanvasAdapter;
    protected RecyclerView.LayoutManager mCanvasLayoutManager;
    protected CanvasTile[][] mCanvasDataset;


    private static final int SPAN_COUNT = 1; // number of columns in the grid
    private static final int DATASET_COUNT = 10;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected FeedRecyclerAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected ArrayList<FeedItem> mDataset = new ArrayList<>();


    private HashMap<String, User> mUserHashMap = new HashMap<>(); // Used so we don't have to repeat querying for user data


    private OnFragmentInteractionListener mListener;

    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };


        // Initialize dataset, this data would usually come from a local content provider or
        if (savedInstanceState != null && savedInstanceState.getParcelableArrayList("FEED_ITEMS") != null) {
            //Restore the fragment's state here
            Log.d("TAG", savedInstanceState.toString());
            mDataset = savedInstanceState.getParcelableArrayList("FEED_ITEMS");
            Log.d("TAG", "Restoring FeedItem Dataset. Dataset Size: " + mDataset.size());
        } else {
            initDataset();
        }
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onCreate(Bundle)},
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
     * {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>This corresponds to {@link android.app.Activity#onSaveInstanceState(Bundle)
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.  Note however: <em>this method may be called
     * at any time before {@link #onDestroy()}</em>.  There are many situations
     * where a fragment may be mostly torn down (such as when placed on the
     * back stack with no UI showing), but its state will not be saved until
     * its owning activity actually needs to save its state.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putParcelableArrayList("FEED_ITEMS", mDataset);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        rootView.setTag(TAG);

        // SET UP CANVAS
        mCanvasRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_feed_canvas_outer);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mCanvasLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        // set up the RecyclerView
        mCanvasRecyclerView.setLayoutManager(mCanvasLayoutManager);
        mCanvasAdapter = new CanvasOuterRecyclerAdapter(getActivity(), mCanvasDataset);
        //mCanvasAdapter.setClickListener(this);
        mCanvasAdapter.setInnerDragListener(this);

        // Set CustomAdapter as the adapter for RecyclerView.
        mCanvasRecyclerView.setAdapter(mCanvasAdapter);


        // SET UP FEED
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_feed);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);

        // set up the RecyclerView
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FeedRecyclerAdapter(getActivity(), mDataset);
        mAdapter.setClickListener(this);
        mAdapter.setLongClickListener(this);
        //mAdapter.setDragListener(this);
        mAdapter.setOnCheckedChangeListener(this);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);


        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        // Setup refresh listener which triggers new data loading
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });


        // Set up show/hide animation for fab
        final FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.

        //initDataset();
        mSwipeRefreshLayout.setRefreshing(false);

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
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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
        getActivity().setTitle(getString(R.string.title_fragment_feed));
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

    private void initDataset() {
        // Set Canvas Data
        mCanvasDataset = new CanvasTile[CANVAS_ROW_COUNT][CANVAS_COLUMN_COUNT];
        for (int i = 0; i < CANVAS_ROW_COUNT; i++) {
            mCanvasDataset[i] = new CanvasTile[CANVAS_COLUMN_COUNT];

            for (int x = 0; x < CANVAS_COLUMN_COUNT; x++) {
                mCanvasDataset[i][x] = new CanvasTile("ROW " + i + " COL " + x, "test", null);
            }
        }

        // Set Feed Data
        new DownloadTask().execute();
    }


    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }


    // Handle an item in the feed being clicked
    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getContext(), StackActivity.class);
        intent.putExtra("PHOTO_ID", mDataset.get(position).getPhotoId());
        view.getContext().startActivity(intent);
    }

    // Handle an item in the feed being long clicked
    @Override
    public boolean onItemLongClick(View view, int position) {

        // Vibrate for 500 milliseconds to let the user know they long clicked
        //Vibrator v = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        //v.vibrate(500);

        // Animate the top RecyclerView to expand it and allow dragging
        mCanvasRecyclerView.setVisibility(View.VISIBLE);


        // Show a shadow of the image that is being dragged when it is long clicked
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        // Can only use new startDragAndDrop function on Nougat and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(data, shadowBuilder, view, 0);
        } else {
            view.startDrag(data, shadowBuilder, view, 0);
        }

        return true;

    }


    // Handle an item from an inner RecyclerView in the canvas being dragged
    @Override
    public boolean onItemDrag(View view, DragEvent dragEvent, int row, int column) {
        switch (dragEvent.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                // drag has started, return true to tell that you're listening to the drag
                //mRecyclerView.setNestedScrollingEnabled(false);

                return true;

            case DragEvent.ACTION_DROP:
                // the dragged item was dropped into this view
                //CanvasTile a = mDataset[0][position];
                //a.setComment("DRAG");
                //mAdapter.notifyItemChanged(position);
                //mAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Dragged Photo To Row " + row + ", Col " + column, Toast.LENGTH_SHORT).show();
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                // the drag has ended

                // Hide the top canvas
                mCanvasRecyclerView.setVisibility(View.GONE);
                return false;
        }
        return false;
    }

    @Override
    public void onItemCheckedChange(CompoundButton compoundButton, boolean isChecked, int position) {
        new PhotoLikeTask().execute(position);
    }




    private class DownloadTask extends AsyncTask<Void, Void, List<FeedItem>> {

        protected List<FeedItem> doInBackground(Void... urls) {

            /*
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(),
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
            TransferUtility transferUtility = new TransferUtility(s3, getContext());


            // TODO Check if there are any files int the cache directory already
            // If they can be used, use them instead of downloading
            // Otherwise, make sure you delete the cache files that are not needed

            int count = 0;
            for (FeedItem feedItem : mDataset) {
                // Convert bitmap to file
                try {

                    File file = new File(getContext().getCacheDir() + "/" + feedItem.getStackId() + ".jpg");

                    TransferObserver observer = transferUtility.download(
                            "owl-aws",     // The bucket to download from
                            feedItem.getStackId() + ".bmp",    // The key for the object to download
                            file        // The file to download the object to
                    );


                    observer.setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            Log.d(TAG, "StateChanged: " + state);
                            if (TransferState.COMPLETED.equals(state)) {
                                // Upload Completed
                                Log.d(TAG, "Upload finished");

                            }
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            // TODO make a progress bar that uses this data
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            Log.e(TAG, "Error on upload: " + ex);

                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "problem");
                }
                count++;
            }
            */


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


            try {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());
                AccountManager am = AccountManager.get(getActivity());
                Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

                String token = GoogleAuthUtil.getToken(getActivity(), accounts[0].name,
                        "audience:server:client_id:" + getString(R.string.server_client_id));

                Map<String, String> logins = new HashMap<>();

                logins.put("accounts.google.com", token);
                credentialsProvider.setLogins(logins);
            } catch (Exception e) {
                Log.e(TAG, "Error getting Google+ Credentials: " + e);
                e.printStackTrace();
            }

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);


            // Query for photos
            /*
            Photo queryPhoto = new Photo();
            queryPhoto.setUserId(LoginActivity.sUserId);

            // Create our map of values
            Map keyConditions = new HashMap();

            String userId = LoginActivity.sUserId;

            // Specify the key conditions
            Condition hashKeyCondition = new Condition()
                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                    .withAttributeValueList(new AttributeValue().withS(userId));

            //keyConditions.put("UserId", hashKeyCondition);


            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withIndexName("UserId-UploadDate-index")
                    .withHashKeyValues(queryPhoto)
                    //.withRangeKeyCondition("Title", rangeKeyCondition)
                    .withConsistentRead(false)
                    .withScanIndexForward(false);

            PaginatedQueryList<Photo> result = mapper.query(Photo.class, queryExpression);
            */


            // Scan for photos

            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                    .withLimit(10);

            PaginatedScanList<Photo> result = mapper.scan(Photo.class, scanExpression);


            // ArrayList that the feed items will be stored in for the updated dataset
            ArrayList<FeedItem> feedItems = new ArrayList<>();


            // Convert the photo list to a FeedItem list
            for (Photo photo : result) {
                Bitmap photoBitmap;
                BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inSampleSize = 4;

                //Check if the bitmap is cached
                photoBitmap = getBitmapFromMemCache(photo.getPhotoId());
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
                        addBitmapToMemoryCache(String.valueOf(photo.getPhotoId()), photoBitmap);

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
                    Log.e(TAG, "Could not load user information");
                    continue;
                }


                // Get the user's profile picture bitmap
                //Check if the bitmap is cached
                Bitmap userBitmap;
                BitmapFactory.Options optionsUser = new BitmapFactory.Options();
                //options.inSampleSize = 4;
                userBitmap = getBitmapFromMemCache("u" + user.getUserId()); // Added a 'u' in front in case there is an overlap between a userId and photoId
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
                        addBitmapToMemoryCache(String.valueOf("u" + user.getUserId()), userBitmap);
                    } catch (Exception e) {
                        Log.e(TAG, "Conversion from String to Bitmap: " + e.getMessage());
                        e.printStackTrace();
                        continue; // Don't add this to the feed
                    }
                }


                // Figure out of the user has previously liked the photo
                PhotoLike photoLike =  mapper.load(PhotoLike.class, photo.getPhotoId(), LoginActivity.sUserId);
                boolean isLiked = (photoLike != null && photoLike.getLikeDate() != null && !photoLike.getLikeDate().isEmpty()); // If photoLike isn't null, then user currently 'likes' this photo

                //FeedItem feedItem = new FeedItem(photo.getStackId(), bitmap, "Stack Title", 4);
                FeedItem feedItem = new FeedItem(photo.getPhotoId(), photoBitmap, USER_ID, userBitmap, user.getName(), isLiked);
                feedItems.add(feedItem);
            } // for photo : result

            return feedItems;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(List<FeedItem> result) {
            // TODO: check this.exception
            // TODO: do something with the feed
            // Clear dataset, add new items, then notify
            mDataset.clear();
            mDataset.addAll(result);
            mAdapter.notifyDataSetChanged();

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }



    private class PhotoLikeTask extends AsyncTask<Integer, Void, Integer> {

        protected Integer doInBackground(Integer... params) {
            // Get the photo like bool
            final int position = params[0];
            final Boolean isLiked = !mDataset.get(position).getLiked(); // Like should be the opposite of whatever it is now

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

            PhotoLike photoLike = new PhotoLike();
            photoLike.setPhotoId(mDataset.get(position).getPhotoId());
            photoLike.setUserId(LoginActivity.sUserId);

            // We are adding a PhotoLike to the table
            if(isLiked) {
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

            // Update the position in the dataset
            mDataset.get(position).setLiked(isLiked);

            return position;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }


        protected void onPostExecute(Integer position) {
            // Notify the adapter
            mAdapter.notifyItemChanged(position);
        }
    }
}
