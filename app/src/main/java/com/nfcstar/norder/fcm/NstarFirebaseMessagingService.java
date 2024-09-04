package com.nfcstar.norder.fcm;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nfcstar.norder.MainActivity;
import com.nfcstar.norder.R;
import com.nfcstar.norder.util.AlertDialogActivity;
import com.nfcstar.norder.util.Cnst;
import com.nfcstar.norder.util.Pref;
import com.nfcstar.norder.util.SimpleAlertDialog;
import com.nfcstar.norder.util.SoundPlayer;

public class NstarFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String notificationBody = remoteMessage.getNotification().getBody();
            if (remoteMessage.getNotification().getBody() != null) {
                sendNotification(notificationBody);
            }
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(NstarFcmWorker.class)
                .build();
        WorkManager.getInstance(this).beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM registration token with any
     * server-side account maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        SharedPreferences pref =  Pref.getInstance().init(getApplicationContext());
        Log.e("FCM 테스트", pref.getBoolean(Cnst.USEMSG,true)+"");
        if(pref.getBoolean(Cnst.USEMSG,true)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_IMMUTABLE);

            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ntable)
                            .setContentTitle("엔테이블")
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri);
            //.setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
                PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "My:Tag");
                wakeLock.acquire(5000);


                SoundPlayer mPlayer = SoundPlayer.create(getApplicationContext(), R.raw.alarm);
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Log.d("debug", "completed");
                    }
                });
                mPlayer.play();
            }

            notificationManager.notify(99 /* ID of notification */, notificationBuilder.build());

            // SimpleAlertDialog alertDialog = new SimpleAlertDialog(getApplicationContext(), messageBody);
            // alertDialog.show();
            AlertDialogActivity alertDialogActivity = AlertDialogActivity.alertDialogActivity;
            AlertDialog.Builder alertDialogBuilder = AlertDialogActivity.alertDialogBuilder;
            AlertDialog alertDialog = AlertDialogActivity.alertDialog;
            if (alertDialogActivity != null && alertDialog != null && alertDialogBuilder != null) {
                Log.e("이거", "뭐냐");
                // alertDialogActivity.alert(title, msg);
                alertDialogActivity.alert("", messageBody);
            } else { //If it doesn't work in the "if"
                Log.e("이거", "?!?");
                Intent intentDialog = new Intent(getApplicationContext(), AlertDialogActivity.class);
                intentDialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentDialog.putExtra("title", "");
                intentDialog.putExtra("body", messageBody);
                startActivity(intentDialog);
            }
        }
    }
}
