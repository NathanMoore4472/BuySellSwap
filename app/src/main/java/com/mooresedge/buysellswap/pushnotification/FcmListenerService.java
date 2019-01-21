package com.mooresedge.buysellswap.pushnotification;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.applozic.mobicomkit.api.notification.MobiComPushReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mooresedge.buysellswap.MainActivity;
import com.mooresedge.buysellswap.R;


public class FcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "ApplozicGcmListener";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.i(TAG,"Message data:"+remoteMessage.getData());

        if(remoteMessage.getData().size()>0)
        {
            if (MobiComPushReceiver.isMobiComPushNotification(remoteMessage.getData())) {
                MobiComPushReceiver.processMessageAsync(this, remoteMessage.getData());
                return;
            }
        }
        if(remoteMessage.getNotification().getBody() != null)
        {
            sendNotification(remoteMessage);
        }

    }

    private void sendNotification(RemoteMessage message) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_shopping_cart_white_24dp)
               .setColor(getColor(R.color.primary_dark))
                .setContentTitle("Buy Sell Swap!")
                .setContentText(message.getNotification().getBody())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message.getNotification().getBody()))
                .setAutoCancel(true)
                .setGroup("Group")
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if(message.getNotification().getTitle() != null)
        {
            notificationBuilder.setContentTitle(message.getNotification().getTitle());
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}