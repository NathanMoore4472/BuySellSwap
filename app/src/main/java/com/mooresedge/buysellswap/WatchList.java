package com.mooresedge.buysellswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nathan on 03/04/2016.
 */
public class WatchList {

    static SharedPreferences app = App.getInstance().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);

    public static class RemoveFromWatchList extends AsyncTask<String, String, String[]>{

        private String mId;
        private String[] details;
        private final int WATCHING = 2;
        String SenderID;

        RemoveFromWatchList(String id)
        {
            mId = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SenderID = app.getString("ProfileID", "");
            try {
                details = new GetMemberDetails(SenderID).execute().get();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected String[] doInBackground(String... strings) {

            String watchList = details[WATCHING];
            String[] parts = watchList.split(",");
            ArrayList<String> list = new ArrayList<>(Arrays.asList(parts));

            for(int i = 0; i < list.size(); i++)
            {
                if(mId.equals(list.get(i)))
                {
                    list.remove(i);
                }
            }

            String output = "";
            for(int i = 0; i < list.size(); i++)
            {
                output += list.get(i);
                if(i != list.size()-1)
                    output += ",";
            }

            JSONParser jsonParser = new JSONParser();
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("ID", SenderID));
            params.add(new BasicNameValuePair("Watching", output));
            jsonParser.makeHttpRequest("http://www.tempman.ie/dbss/Update_Member_Details.php", "GET", params);

            return new String[0];
        }
    }

    public static class AddToWatchList extends AsyncTask<String, String, String[]>{

        String mId;
        String[] details;
        final int WATCHING = 2;

        AddToWatchList(String id){mId = id;}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                String Sid = app.getString("ProfileID", "");
                details = new GetMemberDetails(Sid).execute().get();
            }catch (Exception e){
                e.printStackTrace();
            }
        }


        @Override
        protected String[] doInBackground(String... strings) {

            String watchList = details[WATCHING];
            String[] parts = watchList.split(",");
            ArrayList<String> list = new ArrayList<>(Arrays.asList(parts));
            list.add(mId);

            String output = "";
            for(int i = 0; i < list.size(); i++)
            {
                output += list.get(i);
                if(i != list.size()-1)
                    output += ",";
            }

            JSONParser jsonParser = new JSONParser();
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("ID", app.getString("ProfileID", "")));
            params.add(new BasicNameValuePair("Watching", output));
            jsonParser.makeHttpRequest("http://www.tempman.ie/dbss/Update_Member_Details.php", "GET", params);

            return new String[0];
        }
    }
}