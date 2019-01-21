package com.mooresedge.buysellswap;

import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nathan on 03/04/2016.
 */
public class GetMemberDetails extends AsyncTask <String, String, String[]> {

    String mId;
    String[] Details = new String[10];

    public GetMemberDetails(String Id){
        mId = Id;
    }

    @Override
    protected String[] doInBackground(String... strings) {

        //create the parser
        JSONParser jParser = new JSONParser();
        JSONObject json;
        final List<NameValuePair> params = new ArrayList<NameValuePair>();

        //setup the parameters to send
        BasicNameValuePair pid = new BasicNameValuePair("pid", mId);
        params.add(pid);

        //make the HTTP request
        json = jParser.makeHttpRequest("http://tempman.ie/dbss/Get_Member_Details.php", "GET", params);
        JSONArray details;

        try{
            //get the response from the query
            details = json.getJSONArray("details");
            JSONObject o = details.getJSONObject(0);

            //take the json data and put the data into a string array.
                Details[0] = o.getString("ID");
                Details[1] = o.getString("Name");
                Details[2] = o.getString("Watching");
                Details[3] = o.getString("Sign_Up_Date");
                Details[4] = o.getString("No_Of_Sales");
                Details[5] = o.getString("No_Of_Buys");
                Details[6] = o.getString("Gender");
                Details[7] = o.getString("Age");
                Details[8] = o.getString("Date_Of_Birth");
                Details[9] = o.getString("Notifications");
        }catch (Exception e){e.printStackTrace();}

        //return the details array
        return Details;
    }
}
