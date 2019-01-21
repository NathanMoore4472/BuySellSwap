package com.mooresedge.buysellswap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.feed.TopicDetail;
import com.applozic.mobicomkit.uiwidgets.async.ApplozicConversationCreateTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.people.channel.Conversation;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import com.google.android.gms.ads.AdView;


public class ItemActivity extends android.support.v7.app.AppCompatActivity {

    String ProductID;
    String[] ProductDetails;
    String PosterID = "";
    Menu menu;

    SharedPreferences app = App.getInstance().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        //get the product ID from the intent bundle
        Intent intent = getIntent();
        String pid = intent.getExtras().getString("position");
        ProductID = pid;

        //setup the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get the items details
        try {
            getItem GI = new getItem(pid);
            GI.execute();

        }catch(Exception e){
            e.printStackTrace();
        }

        //Setup Chat with the current item as the Topic/Subject
        final ImageView contactButton = (ImageView) findViewById(R.id.contactbutton);
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(app.getBoolean("SignedIn", false)) {

                    if(PosterID.equals(app.getString("ProfileID", "")))
                        Toast.makeText(ItemActivity.this, "You cannot open a chat to yourself, silly", Toast.LENGTH_SHORT).show();
                    else {
                        ApplozicConversationCreateTask applozicConversationCreateTask = null;

                        ApplozicConversationCreateTask.ConversationCreateListener conversationCreateListener = new ApplozicConversationCreateTask.ConversationCreateListener() {
                            @Override
                            public void onSuccess(Integer conversationId, Context context) {

                                //For launching the  one to one  chat
                                Intent intent = new Intent(context, ConversationActivity.class);
                                intent.putExtra("itemChat", true);
                                intent.putExtra(ConversationUIService.USER_ID, PosterID);//RECEIVER USERID
                                intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                                intent.putExtra(ConversationUIService.CONVERSATION_ID, conversationId);
                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(Exception e, Context context) {

                            }
                        };
                        Conversation conversation = buildConversation();
                        applozicConversationCreateTask = new ApplozicConversationCreateTask(ItemActivity.this, conversationCreateListener, conversation);
                        applozicConversationCreateTask.execute((Void) null);
                    }
                }
                else
                    Toast.makeText(ItemActivity.this, "You must be signed to message someone", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Conversation buildConversation() {
        //Title and subtitles are required if you are enabling the view for particular context.

        TopicDetail topic = new TopicDetail();
        topic.setTitle(ProductDetails[0]);//Your Topic title
        topic.setSubtitle(ProductDetails[2]);//Put Your Topic subtitle
        List<String> imageids = Arrays.asList(ProductDetails[3].split(","));
        topic.setLink("http://www.tempman.ie/dbss/images/" + imageids.get(0) + ".jpg");
        topic.setKey1("Price:");
        topic.setValue1("£" + ProductDetails[1]);

        //Create Conversation.
        Conversation conversation = new Conversation();

        //SET UserId for which you want to launch chat or conversation

        conversation.setTopicId(ProductID);
        conversation.setUserId(PosterID);
        conversation.setTopicDetail(topic.getJson());
        return conversation;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.menu = menu;
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sold)
        {
            Thread thread = new Thread() {
                @Override
                public void run()
                {
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("pid", ProductID));
                    params.add(new BasicNameValuePair("sold", "1"));
                    new JSONParser().makeHttpRequest("http://www.tempman.ie/dbss/update_sold.php",
                            "GET", params);

                    finish();
                }
            };
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
            Toast.makeText(this, "Item marked as sold!", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(id == R.id.action_edit_item)
        {
            Intent edit = new Intent(ItemActivity.this, MainActivity.class);
            edit.putExtra("Name", "EditListing");
            edit.putExtra("pid", ProductID);
            edit.putExtra("Title", ProductDetails[0]);
            edit.putExtra("Price", ProductDetails[1]);
            edit.putExtra("Description", ProductDetails[2]);
            edit.putExtra("ImageIDs", ProductDetails[3]);
            startActivity(edit);
            return true;
        }
        else if(id == R.id.action_report_listing)
        {
            Toast.makeText(this, "This feature will be added in future", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(id == R.id.action_add_to_watchlist)
        {
            new WatchList.AddToWatchList(ProductID).execute();
            Toast.makeText(this, "Item added to Watchlist", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public class getItem extends AsyncTask<String, String, String[]> {
        ProgressDialog pDialog = new ProgressDialog(ItemActivity.this);

        String pid = "";

        public String url_item_details = "http://www.tempman.ie/dbss/Get_Item_Details.php";

        getItem(String pid) {
            this.pid = pid;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {

            String[] Strings = new String[7];
            // Check for success tag
            int success;
            JSONParser jsonParser = new JSONParser();
            try {
                // Building Parameters
                List<NameValuePair> params2 = new ArrayList<NameValuePair>();
                params2.add(new BasicNameValuePair("pid", pid));

                // getting item details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        url_item_details, "GET", params2);


                // json success tag
                success = json.getInt("success");
                if (success == 1) {
                    // successfully received product details
                    JSONArray productObj = json
                            .getJSONArray("product");
                    JSONObject item = productObj.getJSONObject(0);

                    // product with this pid found
                    // Edit Text
                    Strings[0] = item.getString("name");
                    Strings[1] = item.getString("price");
                    Strings[2] = item.getString("description");
                    Strings[3] = item.getString("imageids");
                    Strings[4] = item.getString("profileid");
                    PosterID = Strings[4];
                    Strings[5] = item.getString("promoted");
                    Strings[6] = item.getString(("sold"));
                    ProductDetails = Strings;

                } else {
                    // item was not found
                    Toast.makeText(ItemActivity.this, "Item was not found", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return Strings;
        }

        protected void onPostExecute(final String[] Strings) {
            pDialog.dismiss();

            String longimageids = Strings[3];
            List<String> imageids = Arrays.asList(longimageids.split(","));
            int imageCount = imageids.size();

            ImageViewPagerAdapter imageViewPagerAdapter = new ImageViewPagerAdapter(ItemActivity.this, imageCount, imageids);
            ViewPager viewPager = (ViewPager) findViewById(R.id.item_view_pager);
            viewPager.setAdapter(imageViewPagerAdapter);


            CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
            collapsingToolbar.setTitle(Strings[0]);

            TextView Price = (TextView) findViewById(R.id.item_activity_price);
            if (Double.parseDouble(Strings[1]) == 0.00 || Double.parseDouble(Strings[1]) == 0)
                Price.setText("FREE");
            else
                Price.setText("£" + Strings[1]);

            TextView Description = (TextView) findViewById(R.id.item_activity_description);
            Description.setText(Strings[2]);

            if(PosterID.equals(app.getString("ProfileID" ,"")))
                getMenuInflater().inflate(R.menu.menu_item_activity_seller, menu);
            else if (app.getBoolean("SignedIn", false))
                getMenuInflater().inflate(R.menu.menu_item_activity_buyer, menu);
        }
    }

    static class ImageViewPagerAdapter extends PagerAdapter{

        Context mContext;
        LayoutInflater mLayoutInflater;
        int mSize;
        List<String> mImageids;

        public ImageViewPagerAdapter(Context context, int size, List<String> imageids)
        {
            mContext = context;
            mLayoutInflater = (LayoutInflater.from(mContext));
            mSize = size;
            mImageids = imageids;
        }

        @Override
        public int getCount()
        {
            return mSize;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position){

            View itemview = mLayoutInflater.inflate(R.layout.image_pager_layout, container, false);
            ImageView imageView = (ImageView) itemview.findViewById(R.id.pagerViewImageView);

            Picasso.with(mContext)
                    .load("http://www.tempman.ie/dbss/images/" + mImageids.get(position) + ".jpg")
                    .error(R.drawable.ic_emoticon).into(imageView);
            container.addView(itemview);

            return itemview;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}
