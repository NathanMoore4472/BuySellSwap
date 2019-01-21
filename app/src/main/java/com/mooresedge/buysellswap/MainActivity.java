package com.mooresedge.buysellswap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.ContactService;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.people.contact.Contact;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends SlidingActivity {

    private DrawerLayout mDrawerLayout;
    static NavigationView navigationView;
    View headerView;
    SharedPreferences app = App.getInstance().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
    ImageLoader mImageLoader;
    Context mContext;
    AppContactService contactService;
    Contact userContact;
    Fragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(R.id.selected_view);
        setBehindContentView(frameLayout);
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        final RefineFragment menuFragment = new RefineFragment();
        ft.replace(R.id.selected_view, menuFragment);
        ft.commit();

        final FragmentManager FM = getSupportFragmentManager();
        Fragment Home = new ItemListRecyclerFragment().newInstance(0, menuFragment);
        FM.beginTransaction().replace(R.id.viewpager, Home).commit();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                final int tappedPos = menuItem.getItemId();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (tappedPos == R.id.navigation_item_home) {
                            fragment = new ItemListRecyclerFragment().newInstance(0, menuFragment);
                        }
                        if (tappedPos == R.id.navigation_item_buy) {
                            fragment = new ItemListRecyclerFragment().newInstance(1, menuFragment);
                        }
                        if (tappedPos == R.id.navigation_item_sell) {
                            if (app.getBoolean("SignedIn", false))
                                fragment = new SellFragment();
                            else
                                Toast.makeText(MainActivity.this, "You must sign up before you can sell an item", Toast.LENGTH_SHORT).show();
                        }
                        if (fragment != null)
                            FM.beginTransaction().replace(R.id.viewpager, fragment).commit();

                    }
                }, 220);
                return true;
            }
        });


        SlidingMenu sm = getSlidingMenu();
        sm.setMode(SlidingMenu.RIGHT);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadowright);
        sm.setFadeDegree(0.35f);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

        headerView = navigationView.getHeaderView(0);
        contactService = new AppContactService(this);
        userContact = contactService.getContactById(MobiComUserPreference.getInstance(this).getUserId());
        View drawerHeader = headerView.findViewById(R.id.DrawerHeader);


        drawerHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean logged = App.getInstance().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE).getBoolean("SignedIn", false);
                if (!logged)
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                else
                    startActivity(new Intent(MainActivity.this, MyAccount.class));
            }
        });

        findViewById(R.id.premium_drawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Coming in the future", Toast.LENGTH_SHORT).show();
            }
        });

        if(getIntent().getExtras()!= null)
            if(getIntent().getExtras().getString("Name").equals("EditListing"))
                EditListing(getIntent().getExtras());
    }

    private void EditListing(Bundle data) {
        FragmentManager FM = getSupportFragmentManager();
        Fragment Sell = new SellFragment();
        Sell.setArguments(data);
        FM.beginTransaction().replace(R.id.viewpager, Sell).commit();

        navigationView.getMenu().getItem(2).setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateUI(headerView);
    }

    public void refine()
    {
        getSlidingMenu().toggle();
        ((ItemListRecyclerFragment)fragment).refine();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_filter:
                getSlidingMenu().showMenu(true);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void UpdateUI(View headerView) {
        final TextView drawerHeaderText = (TextView) headerView.findViewById(R.id.drawer_header_text);
        final TextView drawerHeaderInfoText = (TextView) headerView.findViewById(R.id.drawer_header_info_text);
        final CircleImageView profilePicture = (CircleImageView) headerView.findViewById(R.id.drawer_header_profile_picture);

        if (app.getBoolean("SignedIn", false)) {
            drawerHeaderText.setText(app.getString("Name", "Couldn't get name"));
            drawerHeaderInfoText.setText(app.getString("Buys", "0") + " Buys · " + app.getString("Sales", "0") + " Sales");

            mImageLoader = new ImageLoader(this, profilePicture.getHeight()) {
                @Override
                protected Bitmap processBitmap(Object data) {
                    return contactService.downloadContactImage(MainActivity.this, (Contact) data);
                }
            };

            mImageLoader.setImageFadeIn(false);
            mImageLoader.setLoadingImage(com.applozic.mobicomkit.uiwidgets.R.drawable.applozic_ic_contact_picture_180_holo_light);
            mImageLoader.loadImage(userContact, profilePicture);
        } else {
            drawerHeaderText.setText("Log in now");
            drawerHeaderInfoText.setText("To start selling items");
            Picasso.with(MainActivity.this).load(R.drawable.ic_account_circle_white_48dp).into(profilePicture);
        }
    }
}
