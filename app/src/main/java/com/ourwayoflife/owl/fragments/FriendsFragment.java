package com.ourwayoflife.owl.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.activities.MainActivity;
import com.ourwayoflife.owl.adapters.FriendsRecyclerAdapter;
import com.ourwayoflife.owl.models.Following;
import com.ourwayoflife.owl.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zach on 5/23/17.
 */

public class FriendsFragment extends Fragment
        implements FriendsRecyclerAdapter.ItemClickListener {

    public static final String MODE_FOLLOWING = "MODE_FOLLOWING";
    public static final String MODE_FOLLOWERS = "MODE_FOLLOWERS";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "USER_ID";
    private static final String ARG_PARAM2 = "MODE";

    // TODO: Rename and change types of parameters
    private String mUserId;
    private String mMode;

    private static final String TAG = FriendsFragment.class.getName();
    private static final int SPAN_COUNT = 3; // number of columns in the grid
    private static final int DATASET_COUNT = 20;

    protected RecyclerView mRecyclerView;
    protected FriendsRecyclerAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected ArrayList<User> mDataset = new ArrayList<>();


    private FriendsFragment.OnFragmentInteractionListener mListener;

    public FriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1 (UserId)
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
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
            mUserId = getArguments().getString(ARG_PARAM1);
            mMode = getArguments().getString(ARG_PARAM2);
        }

        // Make sure we have both a userId and mode
        if (mUserId == null || mUserId.isEmpty() || mMode == null || mMode.isEmpty()) {
            Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "UserId or Mode not passed into FriendsFragment.  Ending fragment.");
            //getActivity().onBackPressed(); // End fragment by simulating back press
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_friends);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);

        // set up the RecyclerView
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FriendsRecyclerAdapter(getActivity(), mDataset);
        mAdapter.setClickListener(this);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);


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

        //return inflater.inflate(R.layout.fragment_friends, container, false);
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
        new DownloadUsersTask().execute();
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
        if (context instanceof FriendsFragment.OnFragmentInteractionListener) {
            mListener = (FriendsFragment.OnFragmentInteractionListener) context;
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
        /*
        mDataset = new String[DATASET_COUNT];
        for (int i = 0; i < DATASET_COUNT; i++) {
            mDataset[i] = "Category #" + i;
        }
        */

    }

    /**
     * Called when the fragment is visible to the user and actively running.
     */
    @Override
    public void onResume() {
        super.onResume();

        // Set the title based off of which mode we are in
        switch (mMode) {
            case MODE_FOLLOWERS:
                getActivity().setTitle(getString(R.string.followers));
                break;

            case MODE_FOLLOWING:
                getActivity().setTitle(getString(R.string.following));
                break;

            default:
                // Mode doesn't match.  May have passed an invalid mode.
                Toast.makeText(getContext(), "Error loading data", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Mode not valid. Ending fragment.");
                //getActivity().onBackPressed(); // End fragment by simulating back press
        }
    }

    @Override
    public void onItemClick(View view, int position) {

        // User picture was pressed
        // Go to the canvas of that profile



        // Check if the fragment is already in the stack.
        // If it is, then use that instead of making a new instance
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag(CanvasFragment.class.getName());

        // If fragment doesn't exist yet, create one
        if (fragment == null) {
            fragment = CanvasFragment.newInstance(mDataset.get(position).getUserId());
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

            // Remove the existing CanvasFragment from the stack first
            //fragmentTransaction
            //        .remove(fragment);

            //fragmentManager.popBackStackImmediate(CanvasFragment.class.getName(), 0);
            Fragment newFragment = CanvasFragment.newInstance(mDataset.get(position).getUserId());
            fragmentTransaction
                    .replace(R.id.flContent, newFragment, CanvasFragment.class.getName())
                    .addToBackStack(CanvasFragment.class.getName())
                    .commit();


        }


        /*
        // Start canvas fragment
        Fragment fragment = null;
        try {
            fragment = new CanvasFragment();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.
                beginTransaction()
                .replace(R.id.flContent, fragment)
                .addToBackStack(CanvasFragment.class.getName())
                .commit();

         */
    }

    private class DownloadUsersTask extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void... urls) {
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

            // First load the list of users that this user is following/followed by (depends on which mode we are in)
            List<Following> followingList = new ArrayList<>();
            Following queryFollowing = new Following();

            if (mMode.equals(MODE_FOLLOWING)) {
                queryFollowing.setUserId(mUserId);

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(queryFollowing)
                        .withIndexName("UserId-FollowDate-index")
                        .withScanIndexForward(true) // Order so that the most recently followed are last (it will be reversed when we insert into the dataset)
                        .withConsistentRead(false); // Can't use consistent read on GSI

                followingList =  mapper.query(Following.class, queryExpression);

                // Now that we have the list of Following, use that to load the user's data for each row
                for(Following following : followingList) {
                    User user = mapper.load(User.class, following.getFollowingId());
                    mDataset.add(user);
                    publishProgress();
                }
            } else if(mMode.equals(MODE_FOLLOWERS)) {
                queryFollowing.setFollowingId(mUserId);

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(queryFollowing)
                        .withIndexName("FollowingId-FollowDate-index")
                        .withScanIndexForward(true) // Order so that the most recently followed are last (it will be reversed when we insert into the dataset)
                        .withConsistentRead(false); // Can't use consistent read on GSI

                followingList =  mapper.query(Following.class, queryExpression);

                // Now that we have the list of Following, use that to load the user's data for each row
                for(Following following : followingList) {
                    User user = mapper.load(User.class, following.getUserId());
                    mDataset.add(user);
                    publishProgress();
                }

            } else {
                // Invalid mode
                return false;
            }

            // Load user
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDataset.clear(); // Make sure dataset is cleared
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            mAdapter.notifyItemInserted(mDataset.size());
        }

        protected void onPostExecute(Boolean success) {
            // If not successful, notify user and exit fragment
            if(!success) {
                Toast.makeText(getContext(), "Unable to retrieve user list", Toast.LENGTH_SHORT).show();
                //getActivity().onBackPressed(); // Press back to leave fragment
            }
        }
    }
}
