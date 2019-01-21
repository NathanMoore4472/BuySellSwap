package com.mooresedge.buysellswap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.facebook.FacebookSdk;
import com.pushbots.push.Pushbots;

/**
 * Created by Nathan on 21/12/2016.
 */

public class SplashScreenActivity extends Activity {
    private final int SPLASH_WAIT_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_layout);

        FacebookSdk.sdkInitialize(this);
        //Pushbots.sharedInstance().init(this);

            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                        Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                }
            }, SPLASH_WAIT_TIME);
        }
}
