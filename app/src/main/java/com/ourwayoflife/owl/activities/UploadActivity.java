package com.ourwayoflife.owl.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.S3Link;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.adapters.UploadPhotosRecyclerAdapter;
import com.ourwayoflife.owl.adapters.UploadStackRecyclerAdapter;
import com.ourwayoflife.owl.models.Photo;
import com.ourwayoflife.owl.models.PhotoVideoHolder;
import com.ourwayoflife.owl.models.Stack;
import com.ourwayoflife.owl.models.StackPhoto;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadActivity extends AppCompatActivity
        implements UploadPhotosRecyclerAdapter.ItemClickListener,
        UploadStackRecyclerAdapter.ItemClickListener {

    private static final String TAG = UploadActivity.class.getName();


    public static final int REQUEST_SELECT_PHOTOS = 100;
    public static final int REQUEST_IMAGE_CAPTURE = 200;
    public static final int REQUEST_VIDEO_CAPTURE = 300;


    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerViewPhotos;
    private LinearLayoutManager mLayoutManagerPhotos;
    private UploadPhotosRecyclerAdapter mAdapterPhotos;
    private ArrayList<PhotoVideoHolder> mDatasetPhotos = new ArrayList<>();

    private RecyclerView mRecyclerViewStack;
    private LinearLayoutManager mLayoutManagerStack;
    private UploadStackRecyclerAdapter mAdapterStack;
    private ArrayList<Stack> mDatasetStack = new ArrayList<>();

    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Enable the up navigation button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mProgressBar = findViewById(R.id.progress_bar);

        // Setup Photos Recycler
        // Initialize RecyclerView
        mRecyclerViewPhotos = findViewById(R.id.recycler_upload_photos);

        // Initialize Dataset
        // TODO Initialize Dataset

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManagerPhotos = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        // set up the RecyclerView
        mRecyclerViewPhotos.setLayoutManager(mLayoutManagerPhotos);
        mAdapterPhotos = new UploadPhotosRecyclerAdapter(this, mDatasetPhotos);
        mAdapterPhotos.setClickListener(this);

        // Set divider
        DividerItemDecoration dividerItemDecorationPhotos = new DividerItemDecoration(mRecyclerViewPhotos.getContext(),
                mLayoutManagerPhotos.getOrientation());
        mRecyclerViewPhotos.addItemDecoration(dividerItemDecorationPhotos);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerViewPhotos.setAdapter(mAdapterPhotos);


        // Setup Stack Recycler
        // Initialize RecyclerView
        mRecyclerViewStack = findViewById(R.id.recycler_upload_stack);

        // Initialize Stack Dataset
        new GetStacksTask().execute();

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManagerStack = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // set up the RecyclerView
        mRecyclerViewStack.setLayoutManager(mLayoutManagerStack);
        mAdapterStack = new UploadStackRecyclerAdapter(this, mDatasetStack);
        mAdapterStack.setClickListener(this);

        // Set divider
        DividerItemDecoration dividerItemDecorationStack = new DividerItemDecoration(mRecyclerViewStack.getContext(),
                mLayoutManagerStack.getOrientation());
        mRecyclerViewStack.addItemDecoration(dividerItemDecorationStack);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerViewStack.setAdapter(mAdapterStack);


        // When the FAB is clicked, upload the photos
        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Do not allow upload if no photos are selected
                if (mDatasetPhotos.size() <= 0) {
                    Toast.makeText(UploadActivity.this, "No Photos Selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                /*
                // Do not allow upload if no stack is selected
                if (mAdapterStack.getSelectedPos() <= -1) {
                    Toast.makeText(UploadActivity.this, "No Stack Selected", Toast.LENGTH_SHORT).show();
                    mFab.setEnabled(true); // re-enable
                    return;
                }
                */

                // "Create New Stack" is selected.  Show dialog to name the stack
                if (mAdapterStack.getSelectedPos() == 0) {
                    //TODO make sure name is not a duplicate of one the user already has (case insensitive)


                    AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
                    builder.setTitle("Create New Stack");

                    // Set up the input
                    final EditText input = new EditText(UploadActivity.this);

                    // Create container for the EditText to be in.  This is needed to set margins
                    FrameLayout container = new FrameLayout(UploadActivity.this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    //Add padding to the container
                    params.leftMargin = getResources().getDimensionPixelSize(R.dimen.edit_text_margin);
                    params.rightMargin = getResources().getDimensionPixelSize(R.dimen.edit_text_margin);
                    input.setLayoutParams(params);

                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setSingleLine(true);

                    //Set max length
                    final int STACK_TITLE_MAX_LENGTH = 50;
                    InputFilter[] filterArray = new InputFilter[1];
                    filterArray[0] = new InputFilter.LengthFilter(STACK_TITLE_MAX_LENGTH);
                    input.setFilters(filterArray);

                    input.setHint("Stack Title");

                    container.addView(input); //Add view to container
                    builder.setView(container); //Set Dialog view to the container

                    // Set up the buttons
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String STACK_NAME = input.getText().toString();

                            if (STACK_NAME.isEmpty()) {
                                Toast.makeText(UploadActivity.this, "Stack name cannot be empty.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                // Execute task to create new stack.  Upload photos task will follow
                                new CreateStackTask().execute(STACK_NAME);
                            }


                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();

                    return;
                }


                /*
                // Initialize the Amazon Cognito credentials provider
                CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        getString(R.string.cognito_identity_pool), // Identity Pool ID
                        Regions.US_EAST_1 // Region
                );

                AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
                TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
                */


                String stackId = mAdapterStack.getSelectedPos() < 0 ? null : mDatasetStack.get(mAdapterStack.getSelectedPos()).getStackId();
                new UploadTask().execute(stackId);

            }
        });


        // Check for READ_EXTERNAL_STORAGE permission to read photos
        // Note: After Research (as of SDK 25) if any permission is changed to denied while in an
        // application, and that app is returned to, then the activity that was running is
        // automatically restarted.  This means permission checks can be safely placed in the
        // onCreate of activities, and do NOT need to checked every time the activity is resumed
        checkPermissionReadExternalStorage();

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
            case MainActivity.PERMISSION_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // storage-related task you need to do.
                    // In this case, just don't close the activity

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    // Finish the activity
                    finish();
                }
                return;

            case MainActivity.PERMISSION_WRITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // storage-related task you need to do.
                    // In this case, just don't close the activity

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    // Finish the activity
                    finish();
                }
                return;

            // other 'case' lines to check for other
            // permissions this app might request
        }
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case REQUEST_SELECT_PHOTOS:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    // String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    // If imageReturnedIntent.getData() returns something, then there was exactly 1 photo/video selected
                    if (selectedImage != null) {
                        // One image/video was selected

                        if (selectedImage.toString().contains("image")) {
                            // Item is an image
                            Bitmap bitmap;

                            // Check if the photo is locally stored.  Probably is, but check anyway.
                            if (selectedImage.toString().startsWith("content://com.google.android.apps.photos.content")) {
                                // The photo is NOT locally stored (Could be using Google Photos, etc...
                                InputStream is;
                                try {
                                    is = getContentResolver().openInputStream(selectedImage);
                                } catch (FileNotFoundException e) {
                                    Log.e(TAG, "Could not find file. " + e);
                                    e.printStackTrace();
                                    Toast.makeText(this, "Unable to upload photo.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Bitmap tempBitmap = BitmapFactory.decodeStream(is);
                                bitmap = generateCroppedBitmap(tempBitmap);

                            } else {
                                // The photo we are downloading is stored locally
                                String path = getRealPathFromURI(this, selectedImage);
                                bitmap = generateCroppedBitmap(path);
                            }

                            if (bitmap != null) {
                                // Add a new PhotoVideoHolder to the dataset with photo type and the bitmap
                                mDatasetPhotos.add(new PhotoVideoHolder(true, bitmap, null));
                                mAdapterPhotos.notifyItemInserted(mDatasetPhotos.size() - 1);
                            }


                        } else if (selectedImage.toString().contains("video")) {
                            // Item is a video

                            // Check to make sure video is under time limit
                            if (!isVideoUnderTimeLimit(this, selectedImage)) {
                                Toast.makeText(this, "Could not load video.  Max length is 20 seconds", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Get the path
                            String path = getRealVideoPathFromURI(this, selectedImage);

                            if (path == null || path.isEmpty()) {
                                Log.e(TAG, "Error getting full video path.");
                                Toast.makeText(this, "Error loading video.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Get a thumbnail of the video
                            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
                                    MediaStore.Images.Thumbnails.MINI_KIND);

                            // Add the bitmap thumbnail to the dataset
                            if (thumb == null) {
                                Log.e(TAG, "Error getting video thumbnail.");
                                Toast.makeText(this, "Error loading video thumbnail.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Load a new PhotoVideoHolder into the dataset with video type, the thumb and path of the video
                            mDatasetPhotos.add(new PhotoVideoHolder(false, thumb, path));
                            mAdapterPhotos.notifyItemInserted(mDatasetPhotos.size() - 1);


                        } else if (selectedImage.toString().startsWith("content://com.google.android.apps.photos.content")) {
                            // Item is from cloud storage
                            // TODO This only handles photos from cloud, not videos
                            // TODO remember to check video length here

                            Bitmap bitmap;

                            // Check if the photo is locally stored
                            if (selectedImage.toString().startsWith("content://com.google.android.apps.photos.content")) {
                                // The photo is NOT locally stored (Could be using Google Photos, etc...
                                InputStream is;
                                try {
                                    is = getContentResolver().openInputStream(selectedImage);
                                } catch (FileNotFoundException e) {
                                    Log.e(TAG, "Could not find file. " + e);
                                    e.printStackTrace();
                                    Toast.makeText(this, "Unable to upload photo.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Bitmap tempBitmap = BitmapFactory.decodeStream(is);
                                bitmap = generateCroppedBitmap(tempBitmap);

                            } else {
                                // The photo we are downloading is stored locally
                                String path = getRealPathFromURI(this, selectedImage);
                                bitmap = generateCroppedBitmap(path);
                            }

                            if (bitmap != null) {
                                // Add a new PhotoVideoHolder to the dataset with photo type and the bitmap
                                mDatasetPhotos.add(new PhotoVideoHolder(true, bitmap, null));
                                mAdapterPhotos.notifyItemInserted(mDatasetPhotos.size() - 1);
                            }


                        } else {
                            // Item is not a local image or video, and is not from cloud storage. Skip it.
                            Toast.makeText(this, "Unsupported file type selected.", Toast.LENGTH_SHORT).show();
                            return;
                        }


                    } else if (imageReturnedIntent.getClipData().getItemCount() > 1) {
                        // Multiple images selected


                        ClipData clipData = imageReturnedIntent.getClipData();

                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Bitmap bitmap;
                            ClipData.Item item = clipData.getItemAt(i);
                            Uri uri = item.getUri();

                            // Make sure URI isn't null
                            if (uri == null) {
                                Log.e(TAG, "Unable to find URI of photo/video.");
                                Toast.makeText(this, "Could not load media. Skipping...", Toast.LENGTH_SHORT).show();
                                continue;
                            }

                            if (uri.toString().contains("image")) {
                                // Item is an image

                                // Check if the photo is locally stored.  Probably is, but check anyway.
                                if (uri.toString().startsWith("content://com.google.android.apps.photos.content")) {
                                    // The photo is NOT locally stored (Could be using Google Photos, etc...
                                    InputStream is;
                                    try {
                                        is = getContentResolver().openInputStream(uri);
                                    } catch (FileNotFoundException e) {
                                        Log.e(TAG, "Could not find file. " + e);
                                        e.printStackTrace();
                                        Toast.makeText(this, "Unable to upload photo.", Toast.LENGTH_SHORT).show();
                                        continue;
                                    }

                                    Bitmap tempBitmap = BitmapFactory.decodeStream(is);
                                    bitmap = generateCroppedBitmap(tempBitmap);

                                } else {
                                    // The photo we are downloading is stored locally
                                    String path = getRealPathFromURI(this, uri);
                                    bitmap = generateCroppedBitmap(path);
                                }

                                if (bitmap != null) {
                                    // Add a new PhotoVideoHolder to the dataset with photo type and the bitmap
                                    mDatasetPhotos.add(new PhotoVideoHolder(true, bitmap, null));
                                    mAdapterPhotos.notifyItemInserted(mDatasetPhotos.size() - 1);
                                }


                            } else if (uri.toString().contains("video")) {
                                // Item is a video

                                // Check to make sure video is under time limit
                                if (!isVideoUnderTimeLimit(this, uri)) {
                                    Toast.makeText(this, "Could not load video.  Max length is 20 seconds", Toast.LENGTH_SHORT).show();
                                    continue;
                                }

                                // Get the path
                                String path = getRealVideoPathFromURI(this, uri);

                                if (path == null || path.isEmpty()) {
                                    Log.e(TAG, "Error getting full video path.");
                                    Toast.makeText(this, "Error loading video.", Toast.LENGTH_SHORT).show();
                                    continue;
                                }

                                // Get a thumbnail of the video
                                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
                                        MediaStore.Images.Thumbnails.MINI_KIND);

                                // Add the bitmap thumbnail to the dataset
                                if (thumb == null) {
                                    Log.e(TAG, "Error getting video thumbnail.");
                                    Toast.makeText(this, "Error loading video thumbnail.", Toast.LENGTH_SHORT).show();
                                    continue;
                                }

                                // Load a new PhotoVideoHolder into the dataset with video type, the thumb and path of the video
                                mDatasetPhotos.add(new PhotoVideoHolder(false, thumb, path));
                                mAdapterPhotos.notifyItemInserted(mDatasetPhotos.size() - 1);


                            } else if (uri.toString().startsWith("content://com.google.android.apps.photos.content")) {
                                // Item is from cloud storage
                                // TODO This only handles photos from cloud, not videos

                                // Check if the photo is locally stored
                                if (uri.toString().startsWith("content://com.google.android.apps.photos.content")) {
                                    // The photo is NOT locally stored (Could be using Google Photos, etc...
                                    InputStream is;
                                    try {
                                        is = getContentResolver().openInputStream(uri);
                                    } catch (FileNotFoundException e) {
                                        Log.e(TAG, "Could not find file. " + e);
                                        e.printStackTrace();
                                        Toast.makeText(this, "Unable to upload photo.", Toast.LENGTH_SHORT).show();
                                        continue;
                                    }

                                    Bitmap tempBitmap = BitmapFactory.decodeStream(is);
                                    bitmap = generateCroppedBitmap(tempBitmap);

                                } else {
                                    // The photo we are downloading is stored locally
                                    String path = getRealPathFromURI(this, uri);
                                    bitmap = generateCroppedBitmap(path);
                                }

                                if (bitmap != null) {
                                    // Add a new PhotoVideoHolder to the dataset with photo type and the bitmap
                                    mDatasetPhotos.add(new PhotoVideoHolder(true, bitmap, null));
                                    mAdapterPhotos.notifyItemInserted(mDatasetPhotos.size() - 1);
                                }


                            } else {
                                // Item is not a local image or video, and is not from cloud storage. Skip it.
                                Toast.makeText(this, "Unsupported file type selected for one of the files. Skipping...", Toast.LENGTH_SHORT).show();
                                continue;
                            }


                            /*
                            // Check if the photo is locally stored
                            if (uri.toString().startsWith("content://com.google.android.apps.photos.content")) {
                                // The photo is NOT locally stored (Could be using Google Photos, etc...
                                InputStream is;
                                try {
                                    is = getContentResolver().openInputStream(uri);
                                } catch (FileNotFoundException e) {
                                    Log.e(TAG, "Could not find file. " + e);
                                    e.printStackTrace();
                                    Toast.makeText(this, "Unable to upload photo.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Bitmap tempBitmap = BitmapFactory.decodeStream(is);
                                bitmap = generateCroppedBitmap(tempBitmap);

                            } else {
                                // The photo we are downloading is stored locally
                                String path = getRealPathFromURI(this, uri);
                                bitmap = generateCroppedBitmap(path);
                            }

                            if (bitmap != null) {
                                // Add a new PhotoVideoHolder to the dataset with photo type and the bitmap
                                mDatasetPhotos.add(new PhotoVideoHolder(true, bitmap, null));
                            }

                            */

                        } // for loop through selected items

                    } else {
                        Log.wtf(TAG, "Unable to recognize if a single or multiple items selected");
                        Toast.makeText(this, "Error loading media.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Instead of this, notify each time one is inserted
                    //mAdapterPhotos.notifyDataSetChanged();

                    // Scroll to the end
                    mRecyclerViewPhotos.smoothScrollToPosition(mDatasetPhotos.size());
                }
                break;

            case REQUEST_IMAGE_CAPTURE:
                // A photo was taken with the camera
                if(resultCode == RESULT_OK) {
                    checkPermissionWriteExternalStorage(); // Make sure we can save the photo they took

                    /*
                    // Create an image file name
                    // Get timestamp string
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                    DateTime dt = new DateTime(DateTimeZone.UTC);
                    DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
                    //final String dateString = fmt.print(dt);

                    String imageFileName = "OWL_" + timeStamp;
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File image;
                    try {
                        image = File.createTempFile(
                                imageFileName,  // prefix
                                ".jpg",         // suffix
                                storageDir      // directory
                        );
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to create file from captured photo.");
                        return;
                    }
                    */

                    //String path = image.getPath();
                    //Log.d(TAG, "PATH: " + path);

                    /*
                    Uri photoUri = FileProvider.getUriForFile(this,
                            "com.ourwayoflife.owl.fileprovider",
                            image);*/

                    // Get the path that the image was saved to
                    String path = mAdapterPhotos.getCapturedPhotoPath();  // getRealPathFromURI(this, photoUri);

                    if(path == null){
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Add the photo to the phone's gallery (it's good practice to save the photo they took)
                    // Note: Saving the file alone will not add it to the gallery because we saved it in the app's private directory
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(path);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    this.sendBroadcast(mediaScanIntent);


                    // Generate a bitmap of the photo and add it to the dataset
                    Bitmap bitmap = generateCroppedBitmap(path);
                    mDatasetPhotos.add(new PhotoVideoHolder(true, bitmap, null));
                    mAdapterPhotos.notifyItemInserted(mDatasetPhotos.size() - 1);

                    /*
                    // This gets a lower-quality thumbnail
                    // Get the bitmap of the image that was taken and add it to the dataset
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap origBitmap = (Bitmap) extras.get("data");
                    Bitmap croppedBitmap = generateCroppedBitmap(origBitmap);
                    mDatasetPhotos.add(new PhotoVideoHolder(true, origBitmap, null));
                    mAdapterPhotos.notifyItemInserted(mDatasetPhotos.size() - 1);
                    */
                }
                break;

            case REQUEST_VIDEO_CAPTURE:

                break;
        }
    }

    private void checkPermissionReadExternalStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED) {
            // The permission is not granted.  Request from the user.

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog dialog = new AlertDialog.Builder(this)
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
                                ActivityCompat.requestPermissions(UploadActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        MainActivity.PERMISSION_READ_EXTERNAL_STORAGE);
                            }
                        })
                        .create();
                dialog.show(); // Show the dialog

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MainActivity.PERMISSION_READ_EXTERNAL_STORAGE);
            }

        }
    }


    private void checkPermissionWriteExternalStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED) {
            // The permission is not granted.  Request from the user.

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog dialog = new AlertDialog.Builder(this)
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
                                ActivityCompat.requestPermissions(UploadActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MainActivity.PERMISSION_READ_EXTERNAL_STORAGE);
                            }
                        })
                        .create();
                dialog.show(); // Show the dialog

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MainActivity.PERMISSION_WRITE_EXTERNAL_STORAGE);
            }

        }
    }

    /**
     * Returns true if the video is under the max time limit
     *
     * @param context context
     * @param uri     video uri
     * @return true if video length is under time limit
     */
    public boolean isVideoUnderTimeLimit(Context context, Uri uri) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(context, uri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        retriever.release();

        final int MAX_VIDEO_LENGTH = 20000; // Max video length (in milliseconds)

        return timeInMillisec <= MAX_VIDEO_LENGTH;
    }

    public static String getRealPathFromURI(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        if (cursor == null) {
            // There was a problem creating the cursor.  Potentially invalid context
            Log.e(TAG, "In getRealPathFromURI: Error creating cursor.");
            return null;
        }

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    public static String getRealVideoPathFromURI(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        if (cursor == null) {
            // There was a problem creating the cursor.  Potentially invalid context
            Log.e(TAG, "In getRealPathFromURI: Error creating cursor.");
            return null;
        }

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    public static Bitmap generateCroppedBitmap(String path) {
        File imgFile = new File(path);

        if (!imgFile.exists()) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap srcBmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

        return generateCroppedBitmap(srcBmp);
    }

    public static Bitmap generateCroppedBitmap(Bitmap srcBmp) {
        Bitmap dstBmp;

        if (srcBmp.getWidth() >= srcBmp.getHeight()) {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        } else {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }


    private class UploadTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... stackId) {

            boolean success = true;

            final String STACK_ID = stackId[0];

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    UploadActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );


            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);


            // Get credentials for S3
            //AWSSessionCredentials sessionCredentials = credentialsProvider.getCredentials();
            //AWSCredentials s3Credentials = new BasicAWSCredentials(sessionCredentials.getAWSAccessKeyId(), sessionCredentials.getAWSSecretKey());


            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient, credentialsProvider);


            try {

                datasetLoop:
                for (PhotoVideoHolder photoVideoHolder : mDatasetPhotos) {

                    if (photoVideoHolder.isPhotoType()) {
                        // We are uploading a photo


                        Bitmap bitmap = photoVideoHolder.getPhoto();

                        // Convert bitmap to String
                        String photoString;
                        final int MAX_IMAGE_SIZE = 350000; // The max size of the photo string after compression (bytes)
                        int compressQuality = 100; // Initial compression quality (percentage)
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        do {
                            try {
                                baos.flush(); //to avoid out of memory error
                                baos.reset();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            bitmap.compress(Bitmap.CompressFormat.WEBP, compressQuality, baos);
                            byte[] byteArray = baos.toByteArray();

                            Log.d(TAG, "Upload Quality: " + compressQuality);

                            // Calculate the size of the string (in bytes)
                            photoString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                            int stringSize = 8 * ((((photoString.length()) * 2) + 45) / 8);
                            Log.d(TAG, "Photo String Size (bytes): " + stringSize);

                            if (stringSize <= MAX_IMAGE_SIZE) {
                                // The photo has been compressed to a size below the max
                                Log.d(TAG, "Photo has been compressed to a size below the max.");
                                break;
                            }

                            if (compressQuality == 1) {
                                // Compress quality is already at 1%, can't compress further
                                success = false;
                                continue datasetLoop;
                            }


                            if (stringSize > 5 * MAX_IMAGE_SIZE) {
                                // If the image size is more than 5x max then compress it an extra 10 percent before next loop
                                compressQuality -= 20;
                            } else if (stringSize > 1.5 * MAX_IMAGE_SIZE) {
                                // If the image size is more than double max then compress it an extra 10 percent before next loop
                                compressQuality -= 10;
                            }

                            compressQuality -= 5;


                            // Make sure compress quality doesn't fall below 1
                            if (compressQuality < 1) {
                                compressQuality = 1;
                            }

                        } while (true);


                        // Convert bitmap to String
                        /*
                        Bitmap bitmap = mDatasetPhotos.get(0);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 10, baos);
                        byte[] b = baos.toByteArray();
                        String photoString = Base64.encodeToString(b, Base64.DEFAULT);
                        */

                        // Get date string
                        DateTime dt = new DateTime(DateTimeZone.UTC);
                        DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
                        final String dateString = fmt.print(dt);

                        // Insert photo to DB
                        Photo photo = new Photo();
                        photo.setUserId(LoginActivity.sUserId); // Set user ID to the logged in user
                        photo.setUploadDate(dateString);
                        photo.setPhoto(photoString);

                        mapper.save(photo);


                        // If photo is in a stack, then insert to the StackPhoto table
                        if (STACK_ID != null && !STACK_ID.isEmpty()) {
                            StackPhoto stackPhoto = new StackPhoto();
                            stackPhoto.setStackId(STACK_ID);
                            stackPhoto.setPhotoId(photo.getPhotoId());
                            stackPhoto.setAddedDate(dateString);

                            mapper.save(stackPhoto);
                        }

                    } else {
                        // We are uploading a video

                        // First, upload a Photo object with the thumbnail as the Photo
                        // We need to do this before we upload to S3 so we can get a PhotoId

                        Bitmap bitmap = photoVideoHolder.getPhoto();

                        // Convert bitmap to String
                        String photoString;
                        final int MAX_IMAGE_SIZE = 350000; // The max size of the photo string after compression (bytes)
                        int compressQuality = 100; // Initial compression quality (percentage)
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        do {
                            try {
                                baos.flush(); //to avoid out of memory error
                                baos.reset();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            bitmap.compress(Bitmap.CompressFormat.WEBP, compressQuality, baos);
                            byte[] byteArray = baos.toByteArray();

                            Log.d(TAG, "Upload Quality: " + compressQuality);

                            // Calculate the size of the string (in bytes)
                            photoString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                            int stringSize = 8 * ((((photoString.length()) * 2) + 45) / 8);
                            Log.d(TAG, "Photo String Size (bytes): " + stringSize);

                            if (stringSize <= MAX_IMAGE_SIZE) {
                                // The photo has been compressed to a size below the max
                                Log.d(TAG, "Photo has been compressed to a size below the max.");
                                break;
                            }

                            if (compressQuality == 1) {
                                // Compress quality is already at 1%, can't compress further
                                success = false;
                                continue datasetLoop;
                            }


                            if (stringSize > 5 * MAX_IMAGE_SIZE) {
                                // If the image size is more than 5x max then compress it an extra 10 percent before next loop
                                compressQuality -= 20;
                            } else if (stringSize > 1.5 * MAX_IMAGE_SIZE) {
                                // If the image size is more than double max then compress it an extra 10 percent before next loop
                                compressQuality -= 10;
                            }

                            compressQuality -= 5;


                            // Make sure compress quality doesn't fall below 1
                            if (compressQuality < 1) {
                                compressQuality = 1;
                            }

                        } while (true);

                        // Get date string
                        DateTime dt = new DateTime(DateTimeZone.UTC);
                        DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
                        final String dateString = fmt.print(dt);


                        // Insert photo to DB
                        Photo photo = new Photo();
                        photo.setUserId(LoginActivity.sUserId); // Set user ID to the logged in user
                        photo.setUploadDate(dateString);
                        photo.setPhoto(photoString);


                        mapper.save(photo);


                        // Upload the video to s3 now that we (allegedly) have a photoId

                        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
                        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());


                        File file = new File(photoVideoHolder.getVideoPath()); // Test is file name

                        if (photo.getPhotoId() == null || photo.getPhotoId().isEmpty()) {
                            // We need a photoId
                            success = false;
                            Log.e(TAG, "Error uploading video.  Don't have a PhotoId");
                            continue;
                        }

                        final String PHOTO_ID = photo.getPhotoId();

                        final String FILE_EXTENSION = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());

                        final String FILE_NAME_PLUS_EXTENSION = photo.getPhotoId() + "." + FILE_EXTENSION;


                        // Now update the photo object we have for this video to include the video link
                        S3Link s3link = mapper.createS3Link(getString(R.string.s3_bucket_name), FILE_NAME_PLUS_EXTENSION);
                        photo.setVideo(s3link);
                        mapper.save(photo);


                        // If photo is in a stack, then insert to the StackPhoto table
                        if (STACK_ID != null && !STACK_ID.isEmpty()) {
                            StackPhoto stackPhoto = new StackPhoto();
                            stackPhoto.setStackId(STACK_ID);
                            stackPhoto.setPhotoId(photo.getPhotoId());
                            stackPhoto.setAddedDate(dateString);

                            mapper.save(stackPhoto);
                        }


                        ObjectMetadata objectMetadata = new ObjectMetadata();

                        //create a map to store user metadata
                        Map<String, String> userMetadata = new HashMap<>();
                        userMetadata.put("UploadDate", dateString);
                        userMetadata.put("UserId", LoginActivity.sUserId);
                        userMetadata.put("PhotoId", PHOTO_ID);

                        //call setUserMetadata on our ObjectMetadata object, passing it our map
                        objectMetadata.setUserMetadata(userMetadata);

                        // Upload file
                        TransferObserver observer = transferUtility.upload(
                                getString(R.string.s3_bucket_name),     // The bucket to upload to
                                FILE_NAME_PLUS_EXTENSION,    // The key for the uploaded object
                                file,        // The file where the data to upload exists
                                objectMetadata //Metadata
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

                                // Delete the photo object we created for this video
                                // TODO how to get this to work on the main thread? Getting NetworkOnMainThreadException
                                /*
                                Photo photo = new Photo();
                                photo.setPhotoId(PHOTO_ID);
                                mapper.delete(photo);
                                */

                                Toast.makeText(UploadActivity.this, "Error uploading video", Toast.LENGTH_SHORT).show();


                            }


                        });


                    }

                } // for loop through dataset
            } catch (Exception e) {
                Log.e(TAG, "Error on Upload Photo/Video: " + e);
                success = false;
            }

            return success;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            mProgressBar.setVisibility(View.VISIBLE);
            mFab.setEnabled(false); // re-enable
            mFab.hide();
        }

        protected void onPostExecute(Boolean success) {
            // TODO: check this.exception
            // TODO: do something with the feed

            if (success) {
                Toast.makeText(UploadActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(UploadActivity.this, "Upload Unsuccessful.  One or photos may have failed.", Toast.LENGTH_SHORT).show();
            }

            finish(); // End the upload activity

            mProgressBar.setVisibility(View.INVISIBLE);
            mFab.setEnabled(true); // re-enable
            mFab.show();

        }
    }


    private class CreateStackTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... stackName) {

            final String STACK_NAME = stackName[0];
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    UploadActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            //AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
            //TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient, credentialsProvider);

            // Stack object that will be used to upload new stack to the DB
            Stack stack = new Stack();

            try {
                // Get date string
                DateTime dt = new DateTime(DateTimeZone.UTC);
                DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
                final String dateString = fmt.print(dt);

                // Create stack object
                //photo.setStackId("1");
                stack.setUserId(LoginActivity.sUserId);
                stack.setCreatedDate(dateString);
                stack.setName(STACK_NAME);

                mapper.save(stack);

            } catch (Exception e) {
                Log.e(TAG, "Error on Create Stack: " + e);
            }

            return stack.getStackId();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            mFab.setEnabled(false); //Don't allow double-clicking
            mProgressBar.setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(String stackId) {
            // If stackId is null/empty, then the stack was not uploaded to the DB
            if (stackId == null || stackId.isEmpty()) {
                Toast.makeText(UploadActivity.this, "Error: Stack was not created", Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
                mFab.setEnabled(true); // re-enable
                mFab.show();
            } else {
                // Stack was created, now upload the photos
                Toast.makeText(UploadActivity.this, "Stack Created.  Uploading photos...", Toast.LENGTH_SHORT).show();
                new UploadTask().execute(stackId);
            }

        }
    }


    private class GetStacksTask extends AsyncTask<Void, Void, List<Stack>> {

        protected List<Stack> doInBackground(Void... voids) {
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    UploadActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient, credentialsProvider);

            // Query for stacks
            Stack queryStack = new Stack();
            queryStack.setUserId(LoginActivity.sUserId);  // Set userId to the logged in user

            // Create filter expression map
            Map<String, AttributeValue> expressionAttributeValueMap = new HashMap<>();
            expressionAttributeValueMap.put(":isDeleted", new AttributeValue().withN("0"));

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withIndexName("UserId-CreatedDate-index")
                    .withHashKeyValues(queryStack)
                    .withFilterExpression(("IsDeleted = :isDeleted OR attribute_not_exists(IsDeleted)")) // Filter on Stacks that are not deleted
                    .withExpressionAttributeValues(expressionAttributeValueMap) // Add filter expression attribute values
                    .withScanIndexForward(false)
                    .withConsistentRead(false); //Cannot use consistent read on GSI


            PaginatedQueryList<Stack> result = mapper.query(Stack.class, queryExpression);

            return result;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected void onPostExecute(List<Stack> result) {
            // TODO: check this.exception
            // TODO: do something with the feed
            // Clear dataset, add new items, then notify
            mDatasetStack.clear();
            mDatasetStack.addAll(result);

            // Add option at top to create new stack
            mDatasetStack.add(0, new Stack("-1", "-1", "-1", "Create New Stack"));

            mAdapterStack.notifyDataSetChanged();
        }
    }

}
