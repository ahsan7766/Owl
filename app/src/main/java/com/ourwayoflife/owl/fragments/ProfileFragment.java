package com.ourwayoflife.owl.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Base64;
import android.util.Log;
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

import static com.ourwayoflife.owl.fragments.FeedFragment.addBitmapToMemoryCache;
import static com.ourwayoflife.owl.fragments.FeedFragment.getBitmapFromMemCache;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements
        ProfileEditDialogFragment.ProfileEditDialogListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "USER_ID";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = ProfileFragment.class.getName();

    // TODO: Rename and change types of parameters
    private String mUserId = LoginActivity.sUserId;
    private String mParam2;

    private User mUser;

    private ProfilePictureView mProfilePictureView;
    private TextView mTextUserName;
    private TextView mTextUserBio;
    private ProfileCounterView mProfileCounterView;
    private Button mButtonEditFollow;

    private OnFragmentInteractionListener mListener;

    ProfileEditDialogFragment mProfileEditDialog;

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

        mProfilePictureView = rootView.findViewById(R.id.profile_picture);

        mProfileCounterView = rootView.findViewById(R.id.profile_counter);

        mTextUserName = rootView.findViewById(R.id.text_user_name);

        mTextUserBio = rootView.findViewById(R.id.text_user_bio);

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
            mButtonEditFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Create an instance of the dialog fragment and show it
                    mProfileEditDialog = new ProfileEditDialogFragment();
                    mProfileEditDialog.setTargetFragment(ProfileFragment.this, 0);
                    mProfileEditDialog.setName(mUser.getName());
                    mProfileEditDialog.setEmail(mUser.getEmail());
                    mProfileEditDialog.setBio(mUser.getBio());
                    mProfileEditDialog.show(getFragmentManager(), "ProfileEditDialogFragment");
                }
            });
        }else {
            // Otherwise, check if the user is following this user we are viewing to set the button text to either follow or unfollow
            new CheckFollowingTask().execute();

            mButtonEditFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Run task that follows/unfollows the user
                    //new FollowUserTask().execute();
                }
            });
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


    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the ProfileEditDialogFragment.ProfileEditDialogListener interface
    @Override
    public void onDialogPositiveClick(AppCompatDialogFragment dialog) {
        // User touched the dialog's positive button

    }

    @Override
    public void onDialogNegativeClick(AppCompatDialogFragment dialog) {
        // User touched the dialog's negative button
        // Don't have to update any user info since they canceled
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
                getActivity().getSupportFragmentManager().beginTransaction().remove(ProfileFragment.this).commit();
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
            userBitmap = getBitmapFromMemCache("u" + mUser.getUserId()); // Added a 'u' in front in case there is an overlap between a userId and photoId
            userPhoto: if (userBitmap == null) {
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
                    addBitmapToMemoryCache(String.valueOf("u" + mUser.getUserId()), userBitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Conversion from String to Bitmap: " + e.getMessage());
                } finally {
                    mProfilePictureView.setBitmap(userBitmap);
                }
            }

            mTextUserName.setText(mUser.getName());
            mTextUserBio.setText(mUser.getBio());

        }
    }


    private class CheckFollowingTask extends AsyncTask<Void, Void, Following> {

        protected Following doInBackground(Void... urls) {
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
                    getContext(), // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
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
                    getContext(), // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
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
                    getContext(), // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
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
