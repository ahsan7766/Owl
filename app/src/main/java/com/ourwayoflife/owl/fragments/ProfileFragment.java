package com.ourwayoflife.owl.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.activities.LoginActivity;
import com.ourwayoflife.owl.activities.StackActivity;
import com.ourwayoflife.owl.models.Following;
import com.ourwayoflife.owl.models.PhotoLike;
import com.ourwayoflife.owl.models.User;
import com.ourwayoflife.owl.views.ProfileCounterView;
import com.ourwayoflife.owl.views.ProfilePictureView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "USER_ID";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mUserId = LoginActivity.sUserId;
    private String mParam2;


    private ProfilePictureView mProfilePictureView;
    private TextView mTextUserName;
    private TextView mTextUserBio;
    private ProfileCounterView mProfileCounterView;
    private Button mButtonEditFollow;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId UserId of the profile we are trying to view.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String userId, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, userId);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link android.app.Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        super.onStart();

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUserId = getArguments().getString(ARG_PARAM1);

            // If no userId was found, set it to the signed in user
            if(mUserId == null || mUserId.isEmpty()) {
                mUserId = LoginActivity.sUserId;
            }

            mParam2 = getArguments().getString(ARG_PARAM2);
        }



        /*
        // For some reason some tasks were unexpectedly running with a null context and crashing.  This will prevent that.
        if(getContext() == null) {
            return;
        }
        */

        //Execute task to get user data
        new DownloadUserTask().execute();


        // Get Like count for profile counter
        new GetUserLikeCountTask().execute();

        new GetUserFollowerCountTask().execute();

        new GetUserFollowingCountTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mProfilePictureView = (ProfilePictureView) rootView.findViewById(R.id.profile_picture);
        mProfilePictureView.setBackgroundPicture(R.drawable.trees);

        mProfileCounterView = (ProfileCounterView) rootView.findViewById(R.id.profile_counter);

        mTextUserName = rootView.findViewById(R.id.text_user_name);

        mTextUserBio = rootView.findViewById(R.id.text_user_bio);


        //mProfileCounterView.setHootCount(57);
        //mProfileCounterView.setFollowerCount(181);
        //mProfileCounterView.setFollowingCount(132);


        mButtonEditFollow = rootView.findViewById(R.id.button_edit_follow);


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

        // If this user is the logged in user, make the edit/follow button say "Edit"
        if(mUserId.equals(LoginActivity.sUserId)) {
            mButtonEditFollow.setText(getString(R.string.edit));
        }else {
            // Otherwise, check if the user is following this user we are viewing to set the button text to either follow or unfollow
            new CheckFollowingTask().execute();
        }
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
        getActivity().setTitle(getString(R.string.title_fragment_profile));
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


    private class DownloadUserTask extends AsyncTask<Void, Void, User> {

        protected User doInBackground(Void... urls) {
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(),
                    LoginActivity.COGNITO_IDENTITY_POOL, // Identity Pool ID
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
            if (user.getUserId() == null || user.getUserId().isEmpty()) {
                Toast.makeText(getActivity(), "Unable to retrieve user data", Toast.LENGTH_SHORT).show();
                // End fragment by popping itself from the stack
                getActivity().getSupportFragmentManager().beginTransaction().remove(ProfileFragment.this).commit();
                return;
            }

            // Update the UI to whatever the user's data is
            mTextUserName.setText(user.getName());
            mTextUserBio.setText(user.getBio());

        }
    }


    private class CheckFollowingTask extends AsyncTask<Void, Void, Following> {

        protected Following doInBackground(Void... urls) {
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(),
                    LoginActivity.COGNITO_IDENTITY_POOL, // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            // Load user
            return mapper.load(Following.class, LoginActivity.sUserId, mUserId);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Following following) {
            // TODO: check this.exception
            // TODO: do something with the feed
            // Clear dataset, add new items, then notify


            // If Following is not retrieved, then this user does not follow the profile's user
            if (following == null) {
                mButtonEditFollow.setText(getString(R.string.follow));
            } else {
                // Otherwise, this person is followed, so set the text to "Unfollow"
                mButtonEditFollow.setText(getString(R.string.unfollow));
            }
        }
    }


    private class GetUserLikeCountTask extends AsyncTask<Void, Void, Integer> {

        protected Integer doInBackground(Void... params) {

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(),
                    LoginActivity.COGNITO_IDENTITY_POOL, // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            PhotoLike queryPhotoLike = new PhotoLike();
            queryPhotoLike.setUserId(mUserId);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withIndexName("UserId-LikeDate-index")
                    .withHashKeyValues(queryPhotoLike)
                    .withConsistentRead(false); // Can't use consistent read on GSI

            List<PhotoLike> photoLikeList = mapper.query(PhotoLike.class, queryExpression);

            // Return the number of likes in the photoLikeList.  If it's null, return 0
            return (photoLikeList == null ? 0 : photoLikeList.size());

            //TODO also add stack like count?
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Integer likeCount) {
            // Set text of like count to the number of likes
            mProfileCounterView.setHootCount(likeCount);
        }
    }

    private class GetUserFollowerCountTask extends AsyncTask<Void, Void, Integer> {

        protected Integer doInBackground(Void... params) {

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(),
                    LoginActivity.COGNITO_IDENTITY_POOL, // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            Following queryFollowing = new Following();
            queryFollowing.setFollowingId(mUserId);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withIndexName("FollowingId-FollowDate-index")
                    .withHashKeyValues(queryFollowing)
                    .withConsistentRead(false); // Can't use consistent read on GSI

            List<Following> followingList = mapper.query(Following.class, queryExpression);

            // Return the number of likes in the followingList.  If it's null, return 0
            return (followingList == null ? 0 : followingList.size());
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Integer likeCount) {
            // Set text of like count to the number of likes
            mProfileCounterView.setFollowerCount(likeCount);
        }
    }


    private class GetUserFollowingCountTask extends AsyncTask<Void, Void, Integer> {

        protected Integer doInBackground(Void... params) {

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(),
                    LoginActivity.COGNITO_IDENTITY_POOL, // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            Following queryFollowing = new Following();
            queryFollowing.setUserId(mUserId);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(queryFollowing)
                    .withConsistentRead(false); // Can't use consistent read on GSI

            List<Following> followingList = mapper.query(Following.class, queryExpression);

            // Return the number of likes in the followingList.  If it's null, return 0
            return (followingList == null ? 0 : followingList.size());
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Integer likeCount) {
            // Set text of like count to the number of likes
            mProfileCounterView.setFollowingCount(likeCount);
        }
    }
}
