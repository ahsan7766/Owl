package com.example.owl.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.example.owl.activities.MainActivity;
import com.example.owl.activities.StackActivity;
import com.example.owl.activities.UploadActivity;
import com.example.owl.adapters.FeedRecyclerAdapter;
import com.example.owl.R;
import com.example.owl.models.FeedItem;
import com.example.owl.models.Photo;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment
        implements FeedRecyclerAdapter.ItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String TAG = "FeedFragment";
    private static final int SPAN_COUNT = 1; // number of columns in the grid
    private static final int DATASET_COUNT = 10;


    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected FeedRecyclerAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected ArrayList<FeedItem> mDataset;


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

        // Initialize dataset, this data would usually come from a local content provider or
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_feed);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);

        // set up the RecyclerView
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FeedRecyclerAdapter(getActivity(), mDataset);
        mAdapter.setClickListener(this);

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

        /*
        client.getHomeTimeline(0, new JsonHttpResponseHandler() {
            public void onSuccess(JSONArray json) {
                // Remember to CLEAR OUT old items before appending in the new ones
                mAdapter.clear();

                // ...the data has come back, add new items to your adapter...

                mAdapter.addAll(...);

                // Now we call setRefreshing(false) to signal refresh has finished
                mSwipeRefreshLayout.setRefreshing(false);
            }

            public void onFailure(Throwable e) {
                Log.d("DEBUG", "Fetch timeline error: " + e.toString());
            }
        });
        */
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



        new DownloadTask().execute();


        mDataset = new ArrayList<>();
        for (int i = 0; i < DATASET_COUNT; i++) {
            Random r = new Random();
            mDataset.add(new FeedItem(1, "Category #" + i, r.nextInt(50)));
        }


    }


    private class DownloadTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... urls) {


            /*
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(),
                    "us-east-1:4c7583cd-9c5a-4175-b39e-8690323a893e", // Identity Pool ID
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

                    File file = new File(getContext().getCacheDir() + "/" + feedItem.getPhotoId() + ".jpg");

                    TransferObserver observer = transferUtility.download(
                            "owl-aws",     // The bucket to download from
                            feedItem.getPhotoId() + ".bmp",    // The key for the object to download
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
                    getContext(),
                    "us-east-1:4c7583cd-9c5a-4175-b39e-8690323a893e", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);


            // Get date string
            DateTime dt = new DateTime(DateTimeZone.UTC);
            DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
            String str = fmt.print(dt);

            // Test writing to DB
            Photo photo = new Photo();
            photo.setPhotoId("1");
            photo.setUploadDate(str);


            mapper.save(photo);

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

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getContext(), StackActivity.class);
        view.getContext().startActivity(intent);
    }


}
