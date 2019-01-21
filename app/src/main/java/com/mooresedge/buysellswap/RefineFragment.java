package com.mooresedge.buysellswap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import com.mooresedge.buysellswap.expandableListAdapter.Group;
import com.mooresedge.buysellswap.expandableListAdapter.GroupAdapter;
import com.mooresedge.buysellswap.expandableListAdapter.PrintSparseArrays;

public class RefineFragment extends android.support.v4.app.Fragment {

    private ExpandableListView mListView;
    private GroupAdapter mAdapter;

    public String categoryFilter, sortByFilter, priceFilter, searchFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> categories = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.search_category_spinner_entries)));
        ArrayList<String> sortBy = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.sortby_spinner_entries)));
        ArrayList<String> priceRange = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.search_price_spinner_entries)));
        ArrayList<Group> groups = new ArrayList<>();
        groups.add(new Group("Categories", categories));
        groups.add(new Group("Sort by", sortBy));
        groups.add(new Group("Price Range", priceRange));
        mAdapter = new GroupAdapter(getContext(), groups);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_refine, container, false);

        View filterButton = view.findViewById(R.id.filter_tick);
        final EditText search = (EditText) view.findViewById(R.id.filter_search);
        // Set the adapter
        mListView = (ExpandableListView) view.findViewById(R.id.filter_expandableListView);
        mListView.setAdapter(mAdapter);
        mAdapter.setChoiceMode(GroupAdapter.CHOICE_MODE_SINGLE_PER_GROUP);
        mAdapter.setClicked(0,0);
        mAdapter.setClicked(1,0);
        mAdapter.setClicked(2,0);

        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                mAdapter.setClicked(groupPosition, childPosition);
                return false;
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedCategoryIndex = Integer.valueOf(PrintSparseArrays.sparseBooleanArrayToString(mAdapter.getCheckedPositions().get(0)));
                int selectedSortIndex = Integer.valueOf(PrintSparseArrays.sparseBooleanArrayToString(mAdapter.getCheckedPositions().get(1)));
                int selectedPriceIndex = Integer.valueOf(PrintSparseArrays.sparseBooleanArrayToString(mAdapter.getCheckedPositions().get(2)));

                categoryFilter = (String) mAdapter.getChild(0, selectedCategoryIndex);
                sortByFilter = (String) mAdapter.getChild(1, selectedSortIndex);
                priceFilter = (String) mAdapter.getChild(2, selectedPriceIndex);
                searchFilter = search.getText().toString();

                ((MainActivity)getActivity()).refine();
            }
        });

        return view;
    }
}
