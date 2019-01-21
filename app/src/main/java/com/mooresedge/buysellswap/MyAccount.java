package com.mooresedge.buysellswap;

import java.util.ArrayList;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

public class MyAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        // Set up the action bar.
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MyAccountPagerAdapter adapter = new MyAccountPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        //prevent off screen views in the pager from unloading.
        viewPager.setOffscreenPageLimit(3);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    class MyAccountPagerAdapter extends FragmentStatePagerAdapter {

        public MyAccountPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position < 2)
                return ItemListRecyclerFragment.newInstance(position + 2);
            else
                return new profileFragment();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            ArrayList<String> titles = new ArrayList<>();
            titles.add("Selling");
            titles.add("Watching");
            titles.add("Profile");

            return titles.get(position);
        }
    }
}