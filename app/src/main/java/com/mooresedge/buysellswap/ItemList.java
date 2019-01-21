package com.mooresedge.buysellswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nathan on 31/01/2016.
 */
public class ItemList {

    public static class SetItems extends AsyncTask<ArrayList<Item>, Void, ArrayList<Item>> {

        ItemListRecyclerAdapter mRecyclerAdapter;

        public final int PROMOTED_LIST = 0;
        public final int BUY_LIST = 1;
        public final int SELLING_LIST = 2;
        public final int WATCHING_LIST = 3;

        SharedPreferences app = App.getInstance().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);

        ArrayList<Item> items = new ArrayList<>();
        int mStart;
        int mListType;
        String mSortBy, mCategory, mPrice, mKeyword;

        SetItems(int start, int listType) {
            mStart = start;
            mListType = listType;
        }

        SetItems(int start, int listType, ItemListRecyclerAdapter adapter) {
            mStart = start;
            mListType = listType;
            mRecyclerAdapter = adapter;
        }

        SetItems(int start, int listType, String sortBy, String category, String price, String keyword, ItemListRecyclerAdapter adapter) {
            mStart = start;
            mListType = listType;
            mSortBy = sortBy;
            mCategory = category;
            mKeyword = keyword;
            mPrice = price;
            mRecyclerAdapter = adapter;
        }

        protected void onPreExecute() {
        }

        protected ArrayList<Item> doInBackground(ArrayList<Item>... urls) {

            JSONArray products;
            BasicNameValuePair start = new BasicNameValuePair("start", Integer.toString(mStart));
            final List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(start);

            JSONParser jParser = new JSONParser();
            JSONObject json;

            if (mSortBy != null && !mSortBy.equals("Date: New to Old")) {
                BasicNameValuePair sort = new BasicNameValuePair("sortBy", mSortBy);
                params.add(sort);
            }

            if (mCategory != null && !mCategory.equals("All")) {
                BasicNameValuePair category = new BasicNameValuePair("category", '"' + mCategory + '"');
                params.add(category);
            }

            if (mPrice != null && !mPrice.equals("All")) {
                BasicNameValuePair price = new BasicNameValuePair("price", mPrice );
                params.add(price);
            }

            if (mKeyword != null && !mKeyword.equals("")) {
                BasicNameValuePair keyword = new BasicNameValuePair("keyword", mKeyword);
                params.add(keyword);
            }

            if (mListType == PROMOTED_LIST) {
                BasicNameValuePair promoted = new BasicNameValuePair("promoted", "1");
                params.add(promoted);
                json = jParser.makeHttpRequest("http://tempman.ie/dbss/Get_Items.php", "GET", params);
            } else if (mListType == BUY_LIST) {
                BasicNameValuePair promoted = new BasicNameValuePair("promoted", "0");
                params.add(promoted);
                json = jParser.makeHttpRequest("http://www.tempman.ie/dbss/Get_Items.php", "GET", params);
            } else if (mListType == SELLING_LIST) {
                BasicNameValuePair promoted = new BasicNameValuePair("promoted", "0");
                BasicNameValuePair profileid = new BasicNameValuePair("profileid", app.getString("ProfileID", ""));
                params.add(promoted);
                params.add(profileid);
                json = jParser.makeHttpRequest("http://www.tempman.ie/dbss/Get_Items.php", "GET", params);
            } else if (mListType == WATCHING_LIST) {
                BasicNameValuePair promoted = new BasicNameValuePair("promoted", "0");
                BasicNameValuePair sold = new BasicNameValuePair("sold", "1");
                BasicNameValuePair profileid = new BasicNameValuePair("profileid", app.getString("ProfileID", ""));
                params.add(profileid);
                params.add(promoted);
                params.add(sold);
                json = jParser.makeHttpRequest("http://www.tempman.ie/dbss/Get_Watched_List.php", "GET", params);
            } else {
                json = null;
            }

            try {
                String sEnd = json.getString("end");

                boolean end = false;
                if (sEnd.equals("true"))
                    end = true;

                products = json.getJSONArray("products");
                for (int i = 0; i < products.length(); i++) {
                    JSONObject c = products.getJSONObject(i);

                    // Storing each json item in variable
                    String id = c.getString("pid");
                    String name = c.getString("name");
                    String longimageid = c.getString("imageids");
                    List<String> imageids = Arrays.asList(longimageid.split(","));
                    String imageid = imageids.get(0);
                    boolean sold = false;
                    try {
                        sold = (c.getString("sold").equals("1"));
                    } catch (Exception e) {
                    }

                    final Item item = new Item(c.getString("pid"), c.getString("name"), c.getString("description"), c.getDouble("price"), c.getString("category"), c.getString("imageids"), c.getInt("pid"), imageid, sold, end);
                    items.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return items;
        }

        protected void onPostExecute(ArrayList<Item> items) {
            if (mRecyclerAdapter != null) {
                mRecyclerAdapter.mItems = items;
                mRecyclerAdapter.notifyDataSetChanged();
            }
        }
    }

    public static class AddItems extends AsyncTask<ArrayList<Item>, Void, ArrayList<Item>> {

        ItemListRecyclerAdapter mRecyclerAdapter;

        public final int PROMOTED_LIST = 0;
        public final int BUY_LIST = 1;
        public final int SELLING_LIST = 2;
        public final int WATCHING_LIST = 3;

        SharedPreferences app = App.getInstance().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);

        ArrayList<Item> items = new ArrayList<>();
        int mStart;
        int mListType;
        String mSortBy, mCategory, mPrice, mKeyword;

        AddItems(int start, int listType) {
            mStart = start;
            mListType = listType;
        }

        AddItems(int start, int listType, ItemListRecyclerAdapter adapter) {
            mStart = start;
            mListType = listType;
            mRecyclerAdapter = adapter;
        }

        AddItems(int start, int listType, String sortBy, String category, String price, String keyword, ItemListRecyclerAdapter adapter) {
            mStart = start;
            mListType = listType;
            mSortBy = sortBy;
            mCategory = category;
            mKeyword = keyword;
            mPrice = price;
            mRecyclerAdapter = adapter;
        }

        protected void onPreExecute() {
        }

        protected ArrayList<Item> doInBackground(ArrayList<Item>... urls) {

            JSONArray products;
            BasicNameValuePair start = new BasicNameValuePair("start", Integer.toString(mStart));
            final List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(start);

            JSONParser jParser = new JSONParser();
            JSONObject json;

            if (mSortBy != null && !mSortBy.equals("Date: New to Old")) {
                BasicNameValuePair sort = new BasicNameValuePair("sortBy", mSortBy);
                params.add(sort);
            }

            if (mCategory != null && !mCategory.equals("All")) {
                BasicNameValuePair category = new BasicNameValuePair("category", '"' + mCategory + '"');
                params.add(category);
            }

            if (mPrice != null && !mPrice.equals("All")) {
                BasicNameValuePair price = new BasicNameValuePair("price", mPrice);
                params.add(price);
            }

            if (mKeyword != null && !mKeyword.equals("")) {
                BasicNameValuePair keyword = new BasicNameValuePair("keyword", mKeyword);
                params.add(keyword);
            }

            if (mListType == PROMOTED_LIST) {
                BasicNameValuePair promoted = new BasicNameValuePair("promoted", "1");
                params.add(promoted);
                json = jParser.makeHttpRequest("http://tempman.ie/dbss/Get_Items.php", "GET", params);
            } else if (mListType == BUY_LIST) {
                BasicNameValuePair promoted = new BasicNameValuePair("promoted", "0");
                params.add(promoted);
                json = jParser.makeHttpRequest("http://www.tempman.ie/dbss/Get_Items.php", "GET", params);
            } else if (mListType == SELLING_LIST) {
                BasicNameValuePair promoted = new BasicNameValuePair("promoted", "0");
                BasicNameValuePair profileid = new BasicNameValuePair("profileid", app.getString("ProfileID", ""));
                params.add(promoted);
                params.add(profileid);
                json = jParser.makeHttpRequest("http://www.tempman.ie/dbss/Get_Items.php", "GET", params);
            } else if (mListType == WATCHING_LIST) {
                BasicNameValuePair promoted = new BasicNameValuePair("promoted", "0");
                BasicNameValuePair sold = new BasicNameValuePair("sold", "1");
                BasicNameValuePair profileid = new BasicNameValuePair("profileid", app.getString("ProfileID", ""));
                params.add(profileid);
                params.add(promoted);
                params.add(sold);
                json = jParser.makeHttpRequest("http://www.tempman.ie/dbss/Get_Watched_List.php", "GET", params);
            } else {
                json = null;
            }

            try {
                String sEnd = json.getString("end");

                boolean end = false;
                if (sEnd.equals("true"))
                    end = true;

                products = json.getJSONArray("products");
                for (int i = 0; i < products.length(); i++) {
                    JSONObject c = products.getJSONObject(i);

                    // Storing each json item in variable
                    String id = c.getString("pid");
                    String name = c.getString("name");
                    String longimageid = c.getString("imageids");
                    List<String> imageids = Arrays.asList(longimageid.split(","));
                    String imageid = imageids.get(0);
                    boolean sold = false;
                    try {
                        sold = (c.getString("sold").equals("1"));
                    } catch (Exception e) {
                    }

                    final Item item = new Item(c.getString("pid"), c.getString("name"), c.getString("description"), c.getDouble("price"), c.getString("category"), c.getString("imageids"), c.getInt("pid"), imageid, sold, end);
                    items.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return items;
        }

        protected void onPostExecute(ArrayList<Item> items) {
            if (mRecyclerAdapter != null) {
                mRecyclerAdapter.mItems.addAll(items);
                mRecyclerAdapter.notifyDataSetChanged();
            }
        }
    }
}