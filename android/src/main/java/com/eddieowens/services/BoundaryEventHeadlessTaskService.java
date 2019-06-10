package com.eddieowens.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.bridge.Arguments;

public class BoundaryEventHeadlessTaskService extends HeadlessJsTaskService {
    private static final int ID_SERVICE = 101;
    public static final String NOTIFICATION_CHANNEL_ID = "boundary_notification";
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(ID_SERVICE, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelName = "Luoghi d'interesse accanto a te";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);

        channel.setImportance(NotificationManager.IMPORTANCE_MIN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);

        return NOTIFICATION_CHANNEL_ID;
    }

    @Nullable
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();

        String placeName = "Rilevato luogo d'interesse";
        if(extras != null && extras.containsKey("name"))
            placeName = extras.getString("name");

        String placeId = "";
        if(extras != null && extras.containsKey("id"))
            placeId = extras.getString("id");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String packageName = getApplicationContext().getPackageName();
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        launchIntent.putExtra("notificationPlaceId", placeId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

        int iconResource = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(iconResource)
                .setContentTitle("Luogo nelle vicinanze")
                .setContentText(placeName + " nelle vicinanze")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        return new HeadlessJsTaskConfig(
                "OnBoundaryEvent",
                extras != null ? Arguments.fromBundle(extras) : null,
                5000,
                true);
    }
}
