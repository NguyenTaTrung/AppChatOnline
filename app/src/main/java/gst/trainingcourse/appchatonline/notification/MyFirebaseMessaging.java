package gst.trainingcourse.appchatonline.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import gst.trainingcourse.appchatonline.activities.GroupChatActivity;
import gst.trainingcourse.appchatonline.activities.MessageActivity;
import gst.trainingcourse.appchatonline.activities.ProfileActivity;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
            Token token = new Token(s);
            databaseReference.child(firebaseUser.getUid()).setValue(token);
        }
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        final String sented = remoteMessage.getData().get("sented");
        String user = remoteMessage.getData().get("user");

        final String groupname = remoteMessage.getData().get("groupname");
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        String currentUser = sharedPreferences.getString("currentuser", "none");
        String currentGroup = sharedPreferences.getString("currentgroup", "none");

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (groupname != null) {
                if (sented.equals(id) && groupname.equals("")) {
                    if (!currentUser.equals(user)) {
                        sendNotificationChat(remoteMessage);
                    }
                }
            } else {
                if (sented.equals(id)) {
                    sendNotificationAddFriend(remoteMessage);
                }
            }
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (groupname != null) {
                if (!groupname.equals("") && sented.equals(id)) {
//                    if (currentGroup.equals(groupname)) {
                        sendNotificationGroup(remoteMessage);
//                    }
                }
            }
        }

    }

    private void sendNotificationGroup(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String groupname = remoteMessage.getData().get("groupname");
        String imgurl = remoteMessage.getData().get("imgurl");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, GroupChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("groupName", groupname);
        bundle.putString("imgGroupUrl", imgurl);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManager.notify(i, builder.build());
    }

    private void sendNotificationAddFriend(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String introduce = remoteMessage.getData().get("introduce");
        String username = remoteMessage.getData().get("username");
        String imgurl = remoteMessage.getData().get("imgurl");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ProfileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", user);
        bundle.putString("username", username);
        bundle.putString("introduce", introduce);
        bundle.putString("imgUrl", imgurl);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManager.notify(i, builder.build());
    }

    private void sendNotificationChat(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("idUsers", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManager.notify(i, builder.build());
    }
}
