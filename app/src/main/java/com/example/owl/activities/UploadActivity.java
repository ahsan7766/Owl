package com.example.owl.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.example.owl.R;
import com.example.owl.adapters.UploadPhotosRecyclerAdapter;
import com.example.owl.adapters.UploadStackRecyclerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity
        implements UploadPhotosRecyclerAdapter.ItemClickListener,
        UploadStackRecyclerAdapter.ItemClickListener {

    private static final String TAG = UploadActivity.class.getName();

    public static final int REQUEST_SELECT_PHOTOS = 100;

    private RecyclerView mRecyclerViewPhotos;
    private LinearLayoutManager mLayoutManagerPhotos;
    private UploadPhotosRecyclerAdapter mAdapterPhotos;
    private ArrayList<Bitmap> mDatasetPhotos = new ArrayList<>();

    private RecyclerView mRecyclerViewStack;
    private LinearLayoutManager mLayoutManagerStack;
    private UploadStackRecyclerAdapter mAdapterStack;
    private String[] mDatasetStack = new String[0];

    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Enable the up navigation button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup Photos Recycler
        // Initialize RecyclerView
        mRecyclerViewPhotos = (RecyclerView) findViewById(R.id.recycler_upload_photos);

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
        mRecyclerViewStack = (RecyclerView) findViewById(R.id.recycler_upload_stack);

        // Initialize Stack Dataset
        // TODO Initialize Dataset
        initDatasetStacks();

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
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Do not allow upload if no photos are selected
                if(mDatasetPhotos.size() <= 0) {
                    Toast.makeText(UploadActivity.this, "No Photos Selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Do not allow upload if no stack is selected
                if(mAdapterStack.getSelectedPos() <= -1) {
                    Toast.makeText(UploadActivity.this, "No Stack Selected", Toast.LENGTH_SHORT).show();
                    return;
                }



                /*
                // Initialize the Amazon Cognito credentials provider
                CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        "us-east-1:4c7583cd-9c5a-4175-b39e-8690323a893e", // Identity Pool ID
                        Regions.US_EAST_1 // Region
                );

                AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
                TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
                */



                new UploadTask().execute();

                /*
                // Loop through each file to upload
                int count = 1;
                for(Bitmap bitmap : mDatasetPhotos) {
                    // Convert bitmap to file

                    try {
                        File f = new File(getCacheDir(), "test"); // Test is file name
                        FileOutputStream fos = new FileOutputStream(f);  // TODO flush and close fos
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos); //Note: quality is ignored for PNGs

                        // Upload file
                        TransferObserver observer = transferUtility.upload(
                                "owl-aws",     // The bucket to upload to
                                count + ".jpg",    // The key for the uploaded object
                                f        // The file where the data to upload exists
                        );

                        Toast.makeText(UploadActivity.this, "Photo Uploaded", Toast.LENGTH_SHORT).show();


                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + "trees.jpg");

                        TransferObserver observer2 = transferUtility.download(
                                "owl-aws",     // The bucket to download from
                                "trees.jpg",    // The key for the object to download
                                file        // The file to download the object to
                        );


                    }catch (IOException e) {
                        Log.e(TAG, "Error during conversion from bitmap to file.");
                    }
                    count++;
                }
                */

            }
        });



        // Check for READ_EXTERNAL_STORAGE permission to read photos
        // Note: After Research (as of SDK 25) if any permission is changed to denied while in an
        // application, and that app is returned to, then the activity that was running is
        // automatically restarted.  This means permission checks can be safely placed in the
        // onCreate of activities, and do NOT need to checked every time the activity is resumed
        checkPermissionReadExternalStorage();

    }


    private class UploadTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... urls) {

            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:4c7583cd-9c5a-4175-b39e-8690323a893e", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
            TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());


            try {


                Bitmap bitmap = mDatasetPhotos.get(0);
                File f = new File(getCacheDir(), "temp"); // Test is file name
                FileOutputStream fos = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);


                // Upload file
                TransferObserver observer = transferUtility.upload(
                        "owl-aws",     /* The bucket to upload to */
                        "1" + ".bmp",    /* The key for the uploaded object */
                        f        /* The file where the data to upload exists */
                );


                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        Log.d(TAG, "StateChanged: " + state);
                        if (TransferState.COMPLETED.equals(state)) {
                            // Upload Completed
                            Log.d(TAG, "Upload finished");


                            // TODO Go to the stack/photo that was just uploaded and also show a message saying upload is complete
                            finish();
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

            } catch (Exception e){
                Log.e(TAG, "problem");
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
                    // contacts-related task you need to do.
                    //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    //Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    // Finish the activity
                    finish();
                }
                return;
            }

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
                    //String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    if (selectedImage != null) {
                        // One image was selected
                        String path = getRealPathFromURI(selectedImage);
                        Bitmap bitmap = generateCroppedBitmap(path);
                        if(bitmap != null) {
                            mDatasetPhotos.add(bitmap);
                        }
                    } else if (imageReturnedIntent.getClipData().getItemCount() > 1) {
                        // Multiple images selected

                        ClipData clipData = imageReturnedIntent.getClipData();

                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            String path = getRealPathFromURI(item.getUri());
                            Bitmap bitmap = generateCroppedBitmap(path);
                            if(bitmap != null) {
                                mDatasetPhotos.add(bitmap);
                            }
                        }
                    }

                    mAdapterPhotos.notifyDataSetChanged();
                }
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

    public String getRealPathFromURI(Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    private Bitmap generateCroppedBitmap(String path) {
        File imgFile = new File(path);

        if (!imgFile.exists()) {
            return null;
        }

        Bitmap srcBmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        Bitmap dstBmp;

        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }

    /**
     *  Initialize stack dataset
     *  Retrieve the list of the user's stacks
     */
    private void initDatasetStacks() {
        // TODO replace fake data generation with pull form web
        mDatasetStack =  new String[] { "Stack Name 1", "Stack Name 2", "Stack Name 3", "Stack Name 4", "Stack Name 5"};

    }

}
