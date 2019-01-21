package com.mooresedge.buysellswap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserLogoutTask;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.instruction.ApplozicPermissions;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.applozic.mobicomkit.uiwidgets.people.fragment.ProfileFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.FacebookSdk.getApplicationContext;

public class profileFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {


    GoogleApiClient mGoogleApiClient;
    SharedPreferences app = App.getInstance().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread
    AppContactService contactService;
    Contact userContact;
    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        contactService = new AppContactService(getActivity());
        userContact = contactService.getContactById(MobiComUserPreference.getInstance(mContext).getUserId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        //setup views
        ImageView profilePicture = (ImageView)rootView.findViewById(R.id.circlepp);
        Button LogOutButton = (Button)rootView.findViewById(R.id.logOutButton);
        ListView premiumListView = (ListView) rootView.findViewById(R.id.listView2);
        ListView lower = (ListView) rootView.findViewById(R.id.listView);

        lower.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    WebView wv = new WebView(getContext());
                    wv.loadUrl("http://www.facebook.com/MooresEdge");
                }
                if(position == 1)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Send Feedback to mooresedge@gmail.com")
                    .setTitle("Feedback?");
                    builder.create().show();
                }
                if(position == 2)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Libaries")
                            .setItems(R.array.libaries_entries, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                }
                            });
                    builder.create().show();
                }

            }
        });

        //setup list view and items within
        ArrayList<Item> items = new ArrayList<>();
        Item item1 = new Item("0","Messages", "View your private messages", 0, "NONE", "", 0, "", false, false);
        Item item2 = new Item("1","Notifications", "change your notification settings", 0, "NONE", "", 0, "", false, false);
        Item item3 = new Item("2","Your reviews", "View your reviews from other users", 0, "NONE", "", 0, "", false, false);
        Item item4 = new Item("3","Settings", "Change your profile settings", 0, "NONE", "", 0, "", true, false);
        items.add(item1);items.add(item2);items.add(item3);items.add(item4);

        //assign adaoter
        android.widget.ListAdapter adapter = new ListAdapter(mContext, R.layout.premium_list_row, items);
        premiumListView.setAdapter(adapter);

        premiumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = null;
                switch (i)
                {
                    case 0:
                        //start chat
                        intent = new Intent(getActivity(), ConversationActivity.class);
                        break;
                    case 1:
                       intent = new Intent(getActivity(), NotificationCategory.class);
                        break;
                    case 2:
                        Toast.makeText(mContext, "Feature coming in the future", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Intent intent1 = new Intent(getActivity(), ConversationActivity.class);
                        intent1.putExtra("openProfile", true);
                        startActivity(intent1);
                        break;
                }
                if(intent != null)
                    startActivity(intent);
            }
        });
        //add pofile image
        mImageLoader = new ImageLoader(getActivity(), profilePicture.getHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage(getActivity(), (Contact) data);
            }
        };

        mImageLoader.setImageFadeIn(false);
        mImageLoader.setLoadingImage(com.applozic.mobicomkit.uiwidgets.R.drawable.applozic_ic_contact_picture_180_holo_light);
        mImageLoader.loadImage(userContact, profilePicture);
        //log out function
        LogOutButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (Profile.getCurrentProfile() != null) {
                    LoginManager.getInstance().logOut();
                    Toast.makeText(getActivity(), "Signed Out", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Toast.makeText(getActivity(), "Signed Out", Toast.LENGTH_LONG).show();

                        }
                    });
                }

                //Mark user as signed out
                SharedPreferences.Editor editor = app.edit();
                editor.putBoolean("SignedIn", false);
                editor.putString("ProfileID", "");
                editor.apply();

                UserLogoutTask.TaskListener userLogoutTaskListener = new UserLogoutTask.TaskListener() {
                    @Override
                    public void onSuccess(Context context) {
                        //Logout success
                    }
                    @Override
                    public void onFailure(Exception exception) {
                        //Logout failure
                    }
                };


                UserLogoutTask userLogoutTask = new UserLogoutTask(userLogoutTaskListener, getActivity());
                userLogoutTask.execute((Void) null);

                getActivity().finish();
            }
        });
        return rootView;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Unable to connect...", Toast.LENGTH_SHORT).show();
    }

    public class ListAdapter extends ArrayAdapter<Item> {

        List<Item> mItems;

        public ListAdapter(Context context, int resource, List<Item> items) {
            super(context, resource, items);
            mItems = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.premium_list_row, null);
            }

            Item p = getItem(position);

            if (p != null) {
                TextView title = (TextView) v.findViewById(R.id.item_title);
                TextView description = (TextView) v.findViewById(R.id.item_description);
                CircleImageView circleColor = (CircleImageView) v.findViewById(R.id.premium_list_circle);
                ImageView icon = (ImageView) v.findViewById(R.id.premium_list_icon);

                ArrayList<Bitmap> icons = new ArrayList<>();
                icons.add(BitmapFactory.decodeResource(getResources(), R.drawable.ic_private_messages));
                icons.add(BitmapFactory.decodeResource(getResources(), R.drawable.ic_add_alert_white_24dp));
                icons.add(BitmapFactory.decodeResource(getResources(), R.drawable.ic_star_white_24dp));
                icons.add(BitmapFactory.decodeResource(getResources(), R.drawable.ic_settings_white_24dp));

                int[] colorIds = {android.R.color.holo_orange_light, android.R.color.holo_red_light, R.color.accent
                        ,android.R.color.darker_gray};


                title.setText(mItems.get(position).getName());
                description.setText(mItems.get(position).getDescription());
                circleColor.setImageResource(colorIds[position]);
                icon.setImageBitmap(icons.get(position));
            }

            return v;
        }

    }
}
