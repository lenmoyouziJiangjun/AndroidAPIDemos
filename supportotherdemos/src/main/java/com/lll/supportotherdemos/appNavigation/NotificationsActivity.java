package com.lll.supportotherdemos.appNavigation;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.View;

import com.lll.supportotherdemos.R;

public class NotificationsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);
        ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onPostDirect(View view) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setTicker("Direct Notification")
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("Direct Notification")
                .setContentText("This will open the content viewer")
                .setAutoCancel(true)
                .setContentIntent(TaskStackBuilder.create(this)
                        .addParentStack(ContentViewActivity.class)
                        .addNextIntent(new Intent(this, ContentViewActivity.class)
                                .putExtra(ContentViewActivity.EXTRA_TEXT, "From Notification"))
                        .getPendingIntent(0, 0)
                );
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify("direct_tag", R.id.direct_notification, builder.getNotification());
    }


    public void onPostInterstitial(View v) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setTicker("Interstitial Notification")
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("Interstitial Notification")
                .setContentText("This will show a detail page")
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, InterstitialMessageActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK), 0));
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify("interstitial_tag", R.id.interstitial_notification, builder.getNotification());
    }
}
