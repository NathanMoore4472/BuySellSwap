package com.mooresedge.buysellswap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pushbots.push.Pushbots;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    CallbackManager callbackManager;
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    UserLoginTask.TaskListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login);

        //Declare login buttons
        View fakeloginButton = findViewById(R.id.loginbutton);
        View GooglePlusButton = findViewById(R.id.googlebutton);

        //Create instance of Facebook button and set permissions
        final com.facebook.login.widget.LoginButton loginButton = new LoginButton(LoginActivity.this);
        loginButton.setReadPermissions("user_friends");

        //Setup Google+ sign in requirements
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                //.requestIdToken("103138747503-cr0cppmnnq1r6kv48lbpeejm365neh96.apps.googleusercontent.com")
                .build();
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Use the UI button to call the Facebook login button
        fakeloginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.performClick();
            }
        });

        //attepmt to log Google+ user in
        GooglePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
            }
        });

        //Callback for Facebook login result
        callbackManager = CallbackManager.Factory.create();



        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                //Do not need to show message to user
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LoginActivity.this, "There was a problem signing in, please try again", Toast.LENGTH_SHORT).show();
            }
        });

        //Gets the Facebook profile if user signs in
        new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                //Makes sure the newProfile isnt null
                if(newProfile != null)
                {
                    //Create Applozic User
                    User user = new User();

                    //Set user details
                    user.setUserId(newProfile.getId()); //userId it can be any unique user identifier
                    user.setDisplayName(newProfile.getName()); //displayName is the name of the user which will be shown in chat messages
                    user.setImageLink("https://graph.facebook.com/" + newProfile.getId() + "/picture?type=large");//get Facebook profile picture

                    user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());  //setup authentication method

                    //Log user into Applozic chat
                    new UserLoginTask(user, listener, LoginActivity.this).execute((Void) null);

                    //Asynchronously add users details to database
                    new SignUp().execute(new String[]{newProfile.getId(), newProfile.getFirstName() + " " + newProfile.getLastName(), "email unknown"});

                    //Mark User as signed in within app
                    markAsSignedIn(newProfile.getId(), newProfile.getFirstName() + " " + newProfile.getLastName(), "https://graph.facebook.com/"
                        + newProfile.getId() + "/picture?type=large");
                }
            }
        };

        //Listener for Applozic chat login
        listener = new UserLoginTask.TaskListener()
        {
            @Override
            public void onSuccess(RegistrationResponse registrationResponse, Context context)
            {
                //successfully logged into chat, no need to show message to user
                PushNotificationTask.TaskListener pushNotificationTaskListener =  new PushNotificationTask.TaskListener() {
                    @Override
                    public void onSuccess(RegistrationResponse registrationResponse) {
                        //Successfully setup chat Notifications
                    }

                    @Override
                    public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                        //Display error message to user
                        Toast.makeText(LoginActivity.this, "Chat notificaion error, please sign out and back in", Toast.LENGTH_LONG).show();

                        //todo add a dialog that allows user to submit error report to support
                    }
                };

                //attempt to register users device for chat notifications
                PushNotificationTask pushNotificationTask = new PushNotificationTask(Applozic.getInstance(context).getDeviceRegistrationId(),pushNotificationTaskListener,context);
                pushNotificationTask.execute((Void)null);
            }

            @Override
            public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                Toast.makeText(LoginActivity.this, "Chat login error, please sign out and back in", Toast.LENGTH_LONG).show();
            }};


    }

    //Signing in for Google+
    private void handleSignInResult(GoogleSignInResult result)
    {
        if (result.isSuccess())
        {
            // Signed in successfully, add user to database, setup chat and notifications
            GoogleSignInAccount acct = result.getSignInAccount();

            //Create Applozic user
            User user = new User();

            //Add users details
            user.setUserId(acct.getId()); //userId it can be any unique user identifier
            user.setDisplayName(acct.getDisplayName()); //displayName is the name of the user which will be shown in chat messages
            user.setEmail(acct.getEmail()); //Google+ email

            //set Applozic authentication method
            user.setAuthenticationTypeId(User.AuthenticationType.APPLOZIC.getValue());

            //Get Google+ account image
            Uri uri = acct.getPhotoUrl();
            String ProfilePicture = "couldnt find one";
            if(uri != null) {
                user.setImageLink(uri.toString()); //Set Image for chat
                ProfilePicture = uri.toString(); //Get Image for app
            }


            //Log user into Applozic chat
            new UserLoginTask(user, listener, LoginActivity.this).execute((Void) null);

            //Asynchronously add users details to database
            new SignUp().execute(new String[]{acct.getId(), acct.getDisplayName(), acct.getEmail()});

            //setup user ID for Pushbots Group notifications
            //Pushbots.sharedInstance().setAlias(acct.getId());


            //Mark User as signed in within app
            markAsSignedIn(acct.getId(), acct.getDisplayName(), ProfilePicture);
        }
        else
        {
            Toast.makeText(LoginActivity.this, "Google+ login failed, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //send callback to Facebook listener
        callbackManager.onActivityResult(requestCode, resultCode, data);

        //Handle Google+ sign in result
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
    }

    public void markAsSignedIn(String id, String name, String url)
    {
        SharedPreferences sharedPref = App.getInstance().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("ProfileID", id);
        editor.putBoolean("SignedIn", true);
        editor.putString("Name", name);
        editor.putString("ProfilePictureURL", url);
        editor.apply();
    }


    //AsyncTask for adding user to database
    public class SignUp extends AsyncTask<String, String, String[]>
    {
        ProgressDialog pDialog = new ProgressDialog(LoginActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(!isFinishing()) {
                pDialog.setMessage("Loading...");
                pDialog.setIndeterminate(true);
                pDialog.setCancelable(false);
                pDialog.show();
            }
        }

        @Override
        //Asynchronously add user to database
        protected String[] doInBackground(String... strings)
        {
            //Get JSON Parser
            JSONParser jsonParser = new JSONParser();

            //Get the current date/time and format into a String
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            String formattedDate = dateFormat.format(calendar.getTime());

            //Setup NameValuePairs to hold the parameters for user's details
            List<NameValuePair> params = new ArrayList<>();

            //User details obtained from Facebook or Google
            params.add(new BasicNameValuePair("ID", strings[0]));
            params.add(new BasicNameValuePair("Name", strings[1]));
            params.add(new BasicNameValuePair("Email", strings[2]));

            //setup default values
            params.add(new BasicNameValuePair("Watching", ""));
            params.add(new BasicNameValuePair("No_Of_Sales", "0"));
            params.add(new BasicNameValuePair("No_Of_Buys", "0"));
            params.add(new BasicNameValuePair("Notifications", ""));
            //set to -1 because the user has no reviews, will be properly
            // calcualated after first review
            params.add(new BasicNameValuePair("ReviewScore", "-1"));

            //Account creation date
            params.add(new BasicNameValuePair("Sign_Up_Date", formattedDate));

            //Not currently used but, fields are there for future expansion
            params.add(new BasicNameValuePair("Gender", "unknown"));
            params.add(new BasicNameValuePair("Age", "unknown"));
            params.add(new BasicNameValuePair("Date_Of_Birth", "unknown"));

            //Send request to database
            JSONObject result = jsonParser.makeHttpRequest(
                    "http://www.tempman.ie/dbss/Create_Member.php", "GET", params);

            //get result from database, 0 is unsuccessful 1 is successful
            String success = "0";
            try {
                success = result.getString("success");
            }catch (Exception e){}

            return new String[]{success};
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            pDialog.dismiss();
            finish();
            if(strings[0].equals("1"))
            {
                //success, we dont need to notify user
            }
            else
            {
                //user has signed up before
            }
        }
    }
}
