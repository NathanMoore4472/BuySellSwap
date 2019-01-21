package com.mooresedge.buysellswap;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.google.firebase.iid.FirebaseInstanceId;


/**
 * Created by Nathan on 11/06/2016.
 */
public class App extends MultiDexApplication {


    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Stetho.initializeWithDefaults(this);

        String registrationId = FirebaseInstanceId.getInstance().getToken();
        Log.i("REGID", "Found Registration Id:" + registrationId);

    }

    public static App getInstance() {
        return instance;
    }
}