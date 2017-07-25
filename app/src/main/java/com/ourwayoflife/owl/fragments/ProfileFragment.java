package com.ourwayoflife.owl.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
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
import com.ourwayoflife.owl.activities.MainActivity;
import com.ourwayoflife.owl.activities.UploadActivity;
import com.ourwayoflife.owl.models.Following;
import com.ourwayoflife.owl.models.PhotoLike;
import com.ourwayoflife.owl.models.User;
import com.ourwayoflife.owl.views.ProfileCounterView;
import com.ourwayoflife.owl.views.ProfilePictureView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements
        ProfileEditDialogFragment.ProfileEditDialogListener,
        ProfileCounterView.OnTouchListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "USER_ID";

    private static final String TAG = ProfileFragment.class.getName();

    public static final int REQUEST_SELECT_PHOTOS = 100;

    // Parameters
    private String mUserId;

    private User mUser;

    private ProfilePictureView mProfilePictureView;
    private TextView mTextUserName;
    private TextView mTextUserBio;
    private ProfileCounterView mProfileCounterView;
    private Button mButtonEditFollow;

    private OnFragmentInteractionListener mListener;

    private ProfileEditDialogFragment mProfileEditDialog;

    private DownloadUserTask mDownloadUserTask;
    private CheckFollowingTask mCheckFollowingTask;
    private FollowUserTask mFollowUserTask;
    private UpdateProfilePictureTask mUpdateProfilePictureTask;
    private UpdateUserTask mUpdateUserTask;
    private GetUserLikeCountTask mGetUserLikeCountTask;
    private GetUserFollowerCountTask mGetUserFollowerCountTask;
    private GetUserFollowingCountTask mGetUserFollowingCountTask;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId UserId of the profile we are trying to view.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String userId) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, userId);
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
        }

        // If no userId was found, set it to the signed in user
        if (mUserId == null || mUserId.isEmpty()) {
            mUserId = LoginActivity.sUserId;
        }


        /*
        // For some reason some tasks were unexpectedly running with a null context and crashing.  This will prevent that.
        if(getContext() == null) {
            return;
        }
        */


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


        // Set this fragment as a listener of the view's touches
        mProfileCounterView.setOnTouchListener(this);


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

        // If this user is the logged in user..
        if (mUserId.equals(LoginActivity.sUserId)) {
            // Allow the user to edit profile picture by clicking on the profile picture
            mProfilePictureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Check for READ_EXTERNAL_STORAGE permission to read photos
                    // Note: After Research (as of SDK 25) if any permission is changed to denied while in an
                    // application, and that app is returned to, then the activity that was running is
                    // automatically restarted.  This means permission checks can be safely placed in the
                    // onCreate of activities, and do NOT need to checked every time the activity is resumed
                    checkPermissionReadExternalStorageForEditProfilePicture();
                }
            });


            // Make the edit/follow button say "Edit"
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
                    mProfileEditDialog.show(getActivity().getSupportFragmentManager(), ProfileEditDialogFragment.class.getName());
                }
            });
        } else {
            // Otherwise, check if the user is following this user we are viewing to set the button text to either follow or unfollow

            mCheckFollowingTask = new CheckFollowingTask();
            mCheckFollowingTask.execute();

            mButtonEditFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Run task to follow/unfollow the user
                    // The boolean we pass should be 'true' if we are already following (when the button text is 'unfollow')
                    mFollowUserTask = new FollowUserTask();
                    mFollowUserTask.execute(mButtonEditFollow.getText().toString().equals(getString(R.string.unfollow)));
                }
            });
        }


        // TODO  try to save instance state or something instead of re-loading this on every resume
        //Execute task to get user data
        mDownloadUserTask = new DownloadUserTask();
        mDownloadUserTask.execute();

        // Get Like count for profile counter
        mGetUserLikeCountTask = new GetUserLikeCountTask();
        mGetUserLikeCountTask.execute();

        mGetUserFollowerCountTask = new GetUserFollowerCountTask();
        mGetUserFollowerCountTask.execute();

        mGetUserFollowingCountTask = new GetUserFollowingCountTask();
        mGetUserFollowingCountTask.execute();

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
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link android.app.Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();

        // If any of the async tasks are running, cancel them

        if (mDownloadUserTask != null && mDownloadUserTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mDownloadUserTask.cancel(true);
        }

        if (mCheckFollowingTask != null && mCheckFollowingTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mCheckFollowingTask.cancel(true);
        }

        if (mFollowUserTask != null && mFollowUserTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mFollowUserTask.cancel(true);
        }

        if (mUpdateProfilePictureTask != null && mUpdateProfilePictureTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mUpdateProfilePictureTask.cancel(true);
        }

        if (mUpdateUserTask != null && mUpdateUserTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mUpdateUserTask.cancel(true);
        }

        if (mGetUserLikeCountTask != null && mGetUserLikeCountTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mGetUserLikeCountTask.cancel(true);
        }

        if (mGetUserFollowerCountTask != null && mGetUserFollowerCountTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mGetUserFollowerCountTask.cancel(true);
        }

        if (mGetUserFollowingCountTask != null && mGetUserFollowingCountTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mGetUserFollowingCountTask.cancel(true);
        }

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
    public void onDialogPositiveClick(ProfileEditDialogFragment dialog) {
        // User touched the dialog's positive button

        // Make sure we have a valid name
        if (dialog.getName().length() <= 0) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mUser.setName(dialog.getName());
        mUser.setBio(dialog.getBio());

        mUpdateUserTask = new UpdateUserTask();
        mUpdateUserTask.execute();

    }

    @Override
    public void onDialogNegativeClick(ProfileEditDialogFragment dialog) {
        // User touched the dialog's negative button
        // Don't have to update any user info since they canceled
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case REQUEST_SELECT_PHOTOS:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    //String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    if (selectedImage != null) {
                        // One image was selected
                        Bitmap bitmap;

                        // Check if the photo is locally stored
                        if (selectedImage.toString().startsWith("content://com.google.android.apps.photos.content")) {
                            // The photo is NOT locally stored (Could be using Google Photos, etc...
                            InputStream is;
                            try {
                                is = getContext().getContentResolver().openInputStream(selectedImage);
                            } catch (FileNotFoundException e) {
                                Log.e(TAG, "Could not find file. " + e);
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Unable to upload photo.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            Bitmap tempBitmap = BitmapFactory.decodeStream(is);
                            bitmap = UploadActivity.generateCroppedBitmap(tempBitmap);

                        } else {
                            // The photo we are downloading is stored locally
                            String path = UploadActivity.getRealPathFromURI(getContext(), selectedImage);
                            bitmap = UploadActivity.generateCroppedBitmap(path);
                        }

                        if (bitmap != null) {
                            // Run task to upload profile picture
                            mUpdateProfilePictureTask = new UpdateProfilePictureTask();
                            mUpdateProfilePictureTask.execute(bitmap);
                        }

                    } else if (imageReturnedIntent.getClipData().getItemCount() > 1) {
                        // Multiple images selected
                        Toast.makeText(getContext(), "Error: Multiple images were selected", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void checkPermissionReadExternalStorageForEditProfilePicture() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED) {
            // The permission is not granted.  Request from the user.

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.dialog_title_permission_requested))
                        .setMessage(getString(R.string.permission_rationale_storage))
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Positive button has same
                                dialog.dismiss();
                            }
                        })
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                // When the dialog is dismissed, request the permission
                                requestPermissions(
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MainActivity.PERMISSION_READ_EXTERNAL_STORAGE);
                            }
                        })
                        .create();
                dialog.show(); // Show the dialog

            } else {

                // No explanation needed, we can request the permission.
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MainActivity.PERMISSION_READ_EXTERNAL_STORAGE);
            }
        } else {
            // permission is granted
            // Allow user to select photo(s)
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Select Picture"),
                    ProfileFragment.REQUEST_SELECT_PHOTOS);
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MainActivity.PERMISSION_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // storage-related task you need to do.

                    // Allow user to select photo(s)
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(
                            Intent.createChooser(intent, "Select Picture"),
                            ProfileFragment.REQUEST_SELECT_PHOTOS);

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // Don't open the image picker intent
                    Toast.makeText(getContext(), "Unable to upload new profile picture without storage permissions.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    public boolean OnTouchLikes() {
        // TODO Implement viewing the user's liked photos when this is clicked
        Toast.makeText(getContext(), "Viewing other user's Hoots not yet available.", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean OnTouchFollowers() {
        // Start the FriendsFragment passing in the UserId of the profile we are viewing
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = FriendsFragment.newInstance(mUserId, FriendsFragment.MODE_FOLLOWERS);
        fragmentManager.
                beginTransaction()
                .replace(R.id.flContent, fragment, fragment.getClass().getName())
                .addToBackStack(fragment.getClass().getName())
                .commit();

        return true;
    }

    @Override
    public boolean OnTouchFollowing() {
        // Start the FriendsFragment passing in the UserId of the profile we are viewing
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = FriendsFragment.newInstance(mUserId, FriendsFragment.MODE_FOLLOWING);
        fragmentManager.
                beginTransaction()
                .replace(R.id.flContent, fragment, fragment.getClass().getName())
                .addToBackStack(fragment.getClass().getName())
                .commit();
        return true;
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


    private class UpdateUserTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... urls) {
            // Double check that the UserId is not null AND that it matches the signed in user
            if (mUser.getUserId() == null || !mUser.getUserId().equals(LoginActivity.sUserId)) {
                cancel(true);
            }

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


            // Save user
            mapper.save(mUser);

            return null;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Void result) {
            mTextUserName.setText(mUser.getName());
            mTextUserBio.setText(mUser.getBio());
        }
    }


    private class UpdateProfilePictureTask extends AsyncTask<Bitmap, Void, Bitmap> {

        protected Bitmap doInBackground(Bitmap... params) {

            Bitmap bitmap = params[0];

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

            try {
                // Convert bitmap to String
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.WEBP, 10, baos);
                byte[] b = baos.toByteArray();
                String photoString = Base64.encodeToString(b, Base64.DEFAULT);

                // Insert User to DB
                // Make sure we have a UserId
                if (mUser.getUserId() == null) {
                    return null;
                }
                mUser.setPhoto(photoString);

                mapper.save(mUser);

            } catch (Exception e) {
                Log.e(TAG, "Error on Update Profile Picture: " + e);
                return null;
            }

            return bitmap;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                // Profile picture was updated
                mProfilePictureView.setBitmap(result);
                Toast.makeText(getContext(), "Profile Picture Updated", Toast.LENGTH_SHORT).show();
            } else {
                // Profile picture was not updated
                Toast.makeText(getContext(), "Error while updating Profile Picture", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class FollowUserTask extends AsyncTask<Boolean, Void, Boolean> {

        protected Boolean doInBackground(Boolean... params) {
            
            final boolean isFollowing = params[0];
            
            // Double check that the UserId is not null AND that it does NOT match the signed in user
            if (mUser.getUserId() == null || mUser.getUserId().equals(LoginActivity.sUserId)) {
                return null;
            }

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



            if(isFollowing) {
                // We are already following, so we need to unfollow
                Following following = new Following();
                following.setUserId(LoginActivity.sUserId);
                following.setFollowingId(mUserId);
                try{
                    mapper.delete(following);
                } catch (Exception e) {
                    Log.e(TAG, "Error when attempting to unfollow: " + e);
                    return null; // Return null to signify error
                }
                
            } else {
                // We were not already following, so we need to follow
                
                // Get date string
                DateTime dt = new DateTime(DateTimeZone.UTC);
                DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
                final String dateString = fmt.print(dt);

                Following following = new Following(LoginActivity.sUserId, mUserId, dateString);

                // Save Following
                try {
                    mapper.save(following);
                } catch (Exception e) {
                    Log.e(TAG, "Error when attempting to follow: " + e);
                    return null; // Return null to signify error
                }
            }
            
            return !isFollowing;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(Boolean isNowFollowing) {
            
            if(isNowFollowing == null) {
                Toast.makeText(getContext(), "Error attempting to follow/unfollow", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if(isNowFollowing) {
                // We are NOW following the user
                mButtonEditFollow.setText(getString(R.string.unfollow));
            } else {
                // We are now NOT following the user
                mButtonEditFollow.setText(getString(R.string.follow));
            }
        }
    }

}
