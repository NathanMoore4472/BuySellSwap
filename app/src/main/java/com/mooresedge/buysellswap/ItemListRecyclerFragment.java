package com.mooresedge.buysellswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;

import static com.mooresedge.buysellswap.MainActivity.navigationView;

/**
 * Created by Nathan on 29/05/2017.
 */

public class ItemListRecyclerFragment extends Fragment {
    private static final String TAB_POSITION = "tab_position";
    boolean loading = true;
    int listType;
    static int start = 0;
    static FloatingActionButton fab;
    ArrayList<Item> items = new ArrayList<>();
    public static RefineFragment filterFragment;
    SharedPreferences app = App.getInstance().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
    String category, sort, price, search;
    final ItemListRecyclerAdapter RecyclerAdapter = new ItemListRecyclerAdapter(items, listType);

    public ItemListRecyclerFragment() {
    }

    public static ItemListRecyclerFragment newInstance(int tabPosition) {
        ItemListRecyclerFragment fragment = new ItemListRecyclerFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_POSITION, tabPosition);
        fragment.setArguments(args);

        return fragment;
    }

    public static ItemListRecyclerFragment newInstance(int tabPosition, RefineFragment rf) {
        ItemListRecyclerFragment fragment = new ItemListRecyclerFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        filterFragment = rf;

        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if(listType == 0 || listType == 1)
            inflater.inflate(R.menu.menu_buy_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        int tabPosition = args.getInt(TAB_POSITION);
        listType = args.getInt(TAB_POSITION);
        RecyclerAdapter.mListType = listType;
        setHasOptionsMenu(true);

        final View v = inflater.inflate(R.layout.fragment_list_view, container, false);

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary), getResources().getColor(R.color.primary_dark), getResources().getColor(R.color.accent));

        //items = new ArrayList<>();
        //ItemListRecyclerAdapter RecyclerAdapter = new ItemListRecyclerAdapter(items, listType);
        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(RecyclerAdapter);
        new ItemList.SetItems(start, listType, RecyclerAdapter).execute();

//        try {
//            if(items == null) {
//                items = new SetItems(start, listType).execute().get();
//                RecyclerAdapter = new com.mooresedge.buysellswap.ItemListRecyclerAdapter(items, listType);
//            }
//            else {
//                RecyclerAdapter = new com.mooresedge.buysellswap.ItemListRecyclerAdapter(items, listType);
//                items.clear();
//                start = 0;
//                items.addAll(new SetItems(start, listType, RecyclerAdapter).execute().get());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if (tabPosition < 2) {
            fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
            fab.setImageResource(R.drawable.ic_shopping_cart_white_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (app.getBoolean("SignedIn", false)) {
                        FragmentManager FM = getActivity().getSupportFragmentManager();
                        Fragment Sell = new SellFragment();
                        FM.beginTransaction().replace(R.id.viewpager, Sell).commit();

                        navigationView.getMenu().getItem(2).setChecked(true);
                    } else
                        Toast.makeText(getActivity(), "You must be signed in before you can sell an item", Toast.LENGTH_SHORT).show();
                }
            });
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(listType < 2) {
                    sort = filterFragment.sortByFilter;
                    category = filterFragment.categoryFilter;
                    price = filterFragment.priceFilter;
                    search = filterFragment.searchFilter;
                }else{
                    search = "";
                    category = "All";
                    price = "All";
                    sort = "Date: New to Old";
                }
                try {
                    start = 0;
                    new ItemList.SetItems(start, listType, sort, category, price, search, RecyclerAdapter).execute();
                    //items.clear();
                    //items.addAll(new SetItems(start, listType).execute().get());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //RecyclerAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                int pastVisableItems, visableItemCount, totalItemCount;

                if (dy > 0) {
                    visableItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisableItems = mLayoutManager.findFirstVisibleItemPosition();

                    if ((visableItemCount + pastVisableItems) >= totalItemCount) {
                        //Toast.makeText(getActivity(), "At Bottom Lad!", Toast.LENGTH_SHORT).show();
                        if (loading) {
                            loading = false;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        start = start + 10;
                                        sort = filterFragment.sortByFilter;
                                        category = filterFragment.categoryFilter;
                                        price = filterFragment.priceFilter;
                                        search = filterFragment.searchFilter;
                                        new ItemList.AddItems(start, listType, sort, category, price, search, RecyclerAdapter).execute();
                                        loading = true;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 0);
                        }
                    }

                }
            }
        });
        return v;
    }

    public void refine()
    {
        start = 0;
        sort = filterFragment.sortByFilter;
        category = filterFragment.categoryFilter;
        price = filterFragment.priceFilter;
        search = filterFragment.searchFilter;
        new ItemList.SetItems(start, listType, sort, category, price, search, RecyclerAdapter).execute();
    }

}

