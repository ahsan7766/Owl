package com.ourwayoflife.owl.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.auth.IdentityChangedListener;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.models.Photo;
import com.ourwayoflife.owl.models.User;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = LoginActivity.class.getName();

    private static final int RC_SIGN_IN = 9001;


    public static String sUserId;


    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;


    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private UserLoginTask mAuthTask = null;

    // UI references.
    private LinearLayout mLinearLayout;
    //private View mProgressView;
    //private View mLoginFormView;
    private TextView mStatusTextView;
    private ProgressBar mProgressBar;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLinearLayout = findViewById(R.id.layout_login);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        //findViewById(R.id.sign_out_button).setOnClickListener(this);
        //findViewById(R.id.disconnect_button).setOnClickListener(this);


        //AWSMobileClient.initializeMobileClientIfNecessary(getApplicationContext());


        /*
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //attemptLogin();
                signIn();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        */


        /*
        // GET AWS CREDENTIALS
        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                COGNITO_IDENTITY_POOL, // Identity Pool ID
                Regions.US_EAST_1 // Region
        );


        // STORE USER DATA
        // Initialize the Cognito Sync client
        CognitoSyncManager syncClient = new CognitoSyncManager(
                getApplicationContext(),
                Regions.US_EAST_1, // Region
                credentialsProvider);

        // Create a record in a dataset and synchronize with the server
        Dataset dataset = syncClient.openOrCreateDataset("myDataset");
        dataset.put("myKey", "myValue");
        dataset.synchronize(new DefaultSyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List newRecords) {
                //Your handler code here
            }
        });
        */


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestServerAuthCode(getString(R.string.server_client_id))
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

    }

    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideProgressDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());

        // Make sure we have accounts permission
        if(!mayReadContacts()) {
            Log.d(TAG, "Accounts permission not granted.");
            return;
        }

        if (result.isSuccess()) {

            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            // Make sure we retrieved the account
            if(acct == null) {
                Log.d(TAG, "GoogleSignInAccount is null.  Sign in failed.");
                updateUI(false);
                return;
            }

            // Get UserId from user table by querying on User table for matching GoogleId
            // Warning: Do not accept plain user IDs, such as those you can get with the
            // GoogleSignInAccount.getId() method, on your backend server. A modified client
            // application can send arbitrary user IDs to your server to impersonate users, so you
            // must instead use verifiable ID tokens to securely get the user IDs of signed-in
            // users on the server side.
            // See: https://developers.google.com/identity/sign-in/android/backend-auth
            String idToken = acct.getId(); // TODO: Should this be getIdToken??

            // Make sure we got a token
            if(idToken == null || idToken.isEmpty()) {
                Log.d(TAG, "idToken is null.  Sign in failed.");
                updateUI(false);
                return;
            }


            new LoadOrCreateUserTask().execute(acct);


            /*
            // Send token to server and validate server-side
            // Request only the user's ID token, which can be used to identify the
            // user securely to your backend. This will contain the user's basic
            // profile (name, profile picture URL, etc) so you should not need to
            // make an additional call to personalize your application.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.server_client_id))
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this )
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();


            // Send the ID token to your server with an HTTPS POST request:
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("https://yourbackend.example.com/tokensignin");

            try {
                List nameValuePairs = new ArrayList(1);
                nameValuePairs.add(new BasicNameValuePair("idToken", idToken));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpClient.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                final String responseBody = EntityUtils.toString(response.getEntity());
                Log.i(TAG, "Signed in as: " + responseBody);
            } catch (ClientProtocolException e) {
                Log.e(TAG, "Error sending ID token to backend.", e);
            } catch (IOException e) {
                Log.e(TAG, "Error sending ID token to backend.", e);
            }
            */




            /*

            // Skipping token verification for now
            // Do query on user table for GoogleId
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    LoginActivity.this, // Context
                    "971897998846", // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    COGNITO_OWL_UNAUTH_ROLE, // Unauthenticated Role ARN
                    COGNITO_OWL_AUTH_ROLE, // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            credentialsProvider.registerIdentityChangedListener(new IdentityChangedListener() {
                @Override
                public void identityChanged(String oldIdentityId, String newIdentityId) {
                    //Logic to handle identity change
                }
            });

            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this);
            AccountManager am = AccountManager.get(LoginActivity.this);
            Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            try {
                String token = GoogleAuthUtil.getToken(LoginActivity.this, accounts[0].name,
                        "audience:server:client_id:" + getString(R.string.server_client_id));

                Map<String, String> logins = new HashMap<>();

                logins.put("accounts.google.com", token);
                credentialsProvider.setLogins(logins);
            } catch (Exception e) {
                Log.e(TAG, "Error getting Google+ Credentials: " + e);
                return;
            }

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient, credentialsProvider);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withIndexName("GoogleId-index")
                    .withHashKeyValues(idToken)
                    .withConsistentRead(false);


            User user = mapper.load(User.class, queryExpression);

            String userId; // UserId of this user that will be passed to MainActivity.
            if(user == null) {
                // There is no user with this GoogleId, so we have no make a new account
                User newUser = new User();
                newUser.setName(acct.getDisplayName());
                newUser.setEmail(acct.getEmail());

                // Get date string
                DateTime dt = new DateTime(DateTimeZone.UTC);
                DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
                final String dateString = fmt.print(dt);
                newUser.setJoinDate(dateString);

                newUser.setGoogleId(idToken);

                mapper.save(newUser);  //Add user to the user table

                userId = newUser.getUserId(); //Now that the User is saved, we should have a UserId for them now
            } else {
                userId = user.getUserId();
            }


            // Make sure we have a UserId at this point
            if(userId == null) {
                Log.d(TAG, "UserId is null.  Sign in failed.");
                updateUI(false);
                return;
            }

            mStatusTextView.setName(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);


            //Start the Main Activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
            finish(); //Finish the LoginActivity so the user can't go back to it after login


            */
        } else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    private class LoadOrCreateUserTask extends AsyncTask<GoogleSignInAccount, Void, User> {

        protected User doInBackground(GoogleSignInAccount... params) {
            // Initialize the Amazon Cognito credentials provider

            GoogleSignInAccount acct = params[0];

            String idToken = acct.getId();

            /*
            // Send token to server and validate server-side
            // Request only the user's ID token, which can be used to identify the
            // user securely to your backend. This will contain the user's basic
            // profile (name, profile picture URL, etc) so you should not need to
            // make an additional call to personalize your application.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.server_client_id))
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this )
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();


            // Send the ID token to your server with an HTTPS POST request:
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("https://yourbackend.example.com/tokensignin");

            try {
                List nameValuePairs = new ArrayList(1);
                nameValuePairs.add(new BasicNameValuePair("idToken", idToken));
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpClient.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                final String responseBody = EntityUtils.toString(response.getEntity());
                Log.i(TAG, "Signed in as: " + responseBody);
            } catch (ClientProtocolException e) {
                Log.e(TAG, "Error sending ID token to backend.", e);
            } catch (IOException e) {
                Log.e(TAG, "Error sending ID token to backend.", e);
            }
            */



            // Skipping token verification for now
            // Do query on user table for GoogleId
            // Initialize the Amazon Cognito credentials provider
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    LoginActivity.this, // Context
                    getString(R.string.aws_account_id), // AWS Account ID
                    getString(R.string.cognito_identity_pool), // Identity Pool ID
                    getString(R.string.cognito_unauth_role), // Unauthenticated Role ARN
                    getString(R.string.cognito_auth_role), // Authenticated Role ARN
                    Regions.US_EAST_1 // Region
            );

            credentialsProvider.registerIdentityChangedListener(new IdentityChangedListener() {
                @Override
                public void identityChanged(String oldIdentityId, String newIdentityId) {
                    //Logic to handle identity change
                }
            });

            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this);
            AccountManager am = AccountManager.get(LoginActivity.this);
            Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            try {
                String token = GoogleAuthUtil.getToken(LoginActivity.this, accounts[0].name,
                        "audience:server:client_id:" + getString(R.string.server_client_id));

                Map<String, String> logins = new HashMap<>();

                logins.put("accounts.google.com", token);
                credentialsProvider.setLogins(logins);
            } catch (Exception e) {
                Log.e(TAG, "Error getting Google+ Credentials: " + e);
                e.printStackTrace();
                cancel(true);
            }

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient, credentialsProvider);

            User queryUser = new User();
            queryUser.setGoogleId(idToken);

            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withIndexName("GoogleId-index")
                    .withHashKeyValues(queryUser)
                    .withConsistentRead(false);

            List<User> userList = null;
            try{
                 userList = mapper.query(User.class, queryExpression);
            } catch (AmazonClientException e) {
                Log.e(TAG, "Could not load userList: " + e);
                e.printStackTrace();
                cancel(true);
            }

            User user = null;

            if(userList.size() > 0) {
                user = userList.get(0);
            }

            String userId; // UserId of this user that will be passed to MainActivity.
            if(user == null) {
                // There is no user with this GoogleId, so we have no make a new account
                User newUser = new User();
                newUser.setName(acct.getDisplayName());
                newUser.setEmail(acct.getEmail());
                //TODO Email address CAN change, so if we dont have a spot in the profile to edit it, then we need to check if it changed every login

                // Get date string
                DateTime dt = new DateTime(DateTimeZone.UTC);
                DateTimeFormatter fmt = ISODateTimeFormat.basicDateTime();
                final String dateString = fmt.print(dt);
                newUser.setJoinDate(dateString);

                newUser.setGoogleId(idToken);

                mapper.save(newUser);  //Add user to the user table

                userId = newUser.getUserId(); //Now that the User is saved, we should have a UserId for them now

            } else {
                userId = user.getUserId();
            }

            // Make sure we have a UserId at this point
            if(userId == null) {
                Log.d(TAG, "UserId is null.  Sign in failed.");
                cancel(true);
            }

            sUserId = userId;

            try {
                return mapper.load(User.class, userId);
            } catch (Exception e) {
                Log.e(TAG, "Error loading user data: " + e);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }


        protected void onPostExecute(User user) {

            // If the user is not retrieved, don't proceed
            if (user.getUserId() == null || user.getUserId().isEmpty()) {
                //TODO maybe do something better than just showing a toast?
                Toast.makeText(LoginActivity.this, "Unable to retrieve user data", Toast.LENGTH_SHORT).show();
                return;
            }

            mStatusTextView.setText(getString(R.string.signed_in_fmt, user.getName()));
            updateUI(true);

            //Start the Main Activity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            //intent.putExtra("USER_ID", user.getUserId());
            startActivity(intent);
            finish(); //Finish the LoginActivity so the user can't go back to it after login

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(LoginActivity.this, "Could not load data.", Toast.LENGTH_SHORT).show();
            updateUI(false);
        }

        @Override
        protected void onCancelled(User user) {
            super.onCancelled(user);
            Toast.makeText(LoginActivity.this, "Could not load data.", Toast.LENGTH_SHORT).show();
            updateUI(false);
        }
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void showProgressDialog() {
        if (mProgressBar == null) {
            mProgressBar = findViewById(R.id.indeterminate_bar);
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
    }

    private void hideProgressDialog() {
        if (mProgressBar != null && mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            //findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            /*
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
                */
        }
    }


    /*
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }
    */

    private boolean mayReadContacts() {
        //If device is on version before M then permissions are all granted on install
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        // If the permission is granted, then return true
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        // Check if we should show an explanation for requesting this permission
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            Snackbar.make(mLinearLayout, R.string.permission_rationale_contacts, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            // No explanation needed, we can request the permission.

            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // accounts-related thing you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    // In this case, we need accounts or else they can't sign in.
                    // Display a message that says we need it
                    Toast.makeText(this, "Contacts Permission Required to sign into", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // ...other 'case' lines to check for other permissions this app might request
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    /*
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getName().toString();
        String password = mPasswordView.getName().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);


            // TODO Task to login user
            //mAuthTask.execute((Void) null);

            // Temporary code just sends user to main activity on button click
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            finish(); //Finish the LoginActivity so the user can't go back to it after login
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
    */


    /**
     * Shows the progress UI and hides the login form.
     */
    /*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 (SDK 19) we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations.  Use these APIs to fade-in the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
    */

    /*
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
    */

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    /*
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
    */
}

