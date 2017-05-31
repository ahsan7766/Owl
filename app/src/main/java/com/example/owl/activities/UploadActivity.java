package com.example.owl.activities;

import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.owl.R;
import com.example.owl.adapters.UploadPhotosRecyclerAdapter;
import com.example.owl.adapters.UploadStackRecyclerAdapter;

import java.io.File;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity
        implements UploadPhotosRecyclerAdapter.ItemClickListener,
        UploadStackRecyclerAdapter.ItemClickListener {

    private RecyclerView mRecyclerViewPhotos;
    private LinearLayoutManager mLayoutManagerPhotos;
    private UploadPhotosRecyclerAdapter mAdapterPhotos;
    private ArrayList<String> mDatasetPhotos = new ArrayList<>();

    private RecyclerView mRecyclerViewStack;
    private LinearLayoutManager mLayoutManagerStack;
    private UploadStackRecyclerAdapter mAdapterStack;
    private String[] mDatasetStack = new String[8];

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

        // Initialize Dataset
        // TODO Initialize Dataset

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


        /*
        // Allow user to select photo(s)
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
        */
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
        switch(requestCode) {
            case 100:
                if(resultCode == RESULT_OK){
                    //Uri selectedImage = imageReturnedIntent.getData();
                    //imageview.setImageURI(selectedImage);

                    Uri selectedImage = imageReturnedIntent.getData();
                    //String[] filePathColumn = { MediaStore.Images.Media.DATA };


                    if(selectedImage != null) {
                        // One image was selected

                        /*
                        File imgFile = new File("/sdcard/Images/test_image.jpg");

                        if(imgFile.exists()){

                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                            ImageView myImage = (ImageView) findViewById(R.id.imageviewTest);

                            myImage.setImageBitmap(myBitmap);

                        }
                        */

                        //Toast.makeText(this, 1 + "", Toast.LENGTH_SHORT).show();

                    } else if (imageReturnedIntent.getClipData().getItemCount() > 1) {
                        // Multiple images selected/

                        ClipData clipData = imageReturnedIntent.getClipData();

                        for(int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                        }



                        //Toast.makeText(this, clipData.getItemCount() + "", Toast.LENGTH_SHORT).show();
                    }

                    /*
                    ClipData clipData = imageReturnedIntent.getClipData();
                    for (int i = 0; i < clipData.getItemCount(); i++)
                    {
                        Uri uri = clipData.getItemAt(i).getUri();
                    }
                    */

                    /*
                    ArrayList<Parcelable> list = imageReturnedIntent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    for (Parcelable parcel : list) {
                        Uri uri = (Uri) parcel;
                        /// do things here.
                    }
                    */

                    /*
                    ClipData clipData = imageReturnedIntent.getClipData();
                    for(int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item =  clipData.getItemAt(i);
                        Uri uri = item.getUri();

                        // Process the uri...
                    }
                    */



                    /*
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();
                    */


                    //ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    //imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                }
                break;
        }
    }
}
