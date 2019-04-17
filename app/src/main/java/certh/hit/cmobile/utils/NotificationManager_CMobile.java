package certh.hit.cmobile.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import certh.hit.cmobile.HomeActivity;
import certh.hit.cmobile.R;
import certh.hit.cmobile.service.LocationService;

/**
 * Created by anmpout on 25/02/2019
 */
public class NotificationManager_CMobile extends BroadcastReceiver {
    private static final String CHANNEL_ID = "core.schoox.players.MUSIC_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;
    public static final String ACTION_PAUSE = "core.schoox.players.pause";
    public static final String ACTION_PLAY = "core.schoox.players.play";
    public static final String ACTION_STOP = "core.schoox.players.stop";
    private final LocationService mService;
    private final NotificationManager mNotificationManager;
    private final PendingIntent mPlayIntent;
    private final PendingIntent mPauseIntent;
    private final PendingIntent mStopIntent;
    private final int mNotificationColor;
    private String notificationTitle;
    private String notificationSubtitle;

    private boolean mStarted = false;

    /**
     * Instantiates a new  broadcast receiver
     *
     * @param service
     * @throws RemoteException
     */
    public NotificationManager_CMobile(LocationService service) throws RemoteException {
        mService = service;
        mNotificationColor = R.attr.colorPrimary;
        mNotificationManager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
        String pkg = mService.getPackageName();
        mPauseIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mPlayIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        mStopIntent = PendingIntent.getBroadcast(mService, REQUEST_CODE,
                new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    /**
     * Posts the notification and starts tracking the session to keep it
     * updated. The notification will automatically be removed if the session is
     * destroyed before {@link #stopNotification} is called.
     */
    public void startNotification(String notificationTitle, String notificationSubtitle) {
        this.notificationTitle = notificationTitle;
        this.notificationSubtitle = notificationSubtitle;
        if (!mStarted) {
            Notification notification = createNotification();
            if (notification != null) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ACTION_PAUSE);
                filter.addAction(ACTION_PLAY);
                filter.addAction(ACTION_STOP);
                mService.registerReceiver(this, filter);
                mService.startForeground(NOTIFICATION_ID, notification);
                mStarted = true;
            }
        } else {
            Notification notification = createNotification();
            if (notification != null) {
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            }
        }
    }

    /**
     * Removes the notification and stops tracking the session. If the session
     * was destroyed this has no effect.
     */
    public void stopNotification() {
        if (mStarted) {
            mStarted = false;
            try {
                mNotificationManager.cancel(NOTIFICATION_ID);
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore if the receiver is not registered.
            }
            mService.stopForeground(true);
        }
    }

    /**
     * callback for play/pause/stop
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_PAUSE:
                //mService.pause();
                break;
            case ACTION_PLAY:
               // mService.play();
                break;
            case ACTION_STOP:
//
//                mService.stop();
//                mService.finishAudioPlayer();
                break;
            default:
        }
    }

    /**
     * added the open intent to notification
     *
     * @return
     */
    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, HomeActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mService, REQUEST_CODE, openUI,
                PendingIntent.FLAG_CANCEL_CURRENT);

    }

    /**
     * creates the notification as the service is running
     *
     * @return
     */
    private Notification createNotification() {
        String iconUrl = "http://storage.googleapis.com/automotive-media/album_art_2.jpg";
        String fetchArtUrl = null;
        Bitmap art = null;
        art = BitmapFactory.decodeResource(mService.getResources(),
                R.drawable.ic_c_mobile_logo_master_white);


        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mService, CHANNEL_ID);

       // final int playPauseButtonPosition = addActions(notificationBuilder);
        notificationBuilder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                // show only play/pause in compact view
                .setShowCancelButton(true)
                .setCancelButtonIntent(mStopIntent))
                .setDeleteIntent(mStopIntent)
                .setSmallIcon(R.drawable.ic_c_mobile_logo_master_white)
                .setVisibility(android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent())
                .setContentTitle(notificationTitle)
                .setContentText(notificationSubtitle)
                .setLargeIcon(art);



        notificationBuilder.setLargeIcon(art);
        mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        setNotificationPlaybackState(notificationBuilder);
        return notificationBuilder.build();
    }

    /**
     * adds play & pause button to notification
     *
     * @param notificationBuilder
     * @return
     */
    private int addActions(final android.support.v4.app.NotificationCompat.Builder notificationBuilder) {

        int playPauseButtonPosition = 0;
// Play or pause button, depending on the current state.
        final String label;
        final int icon;
        final PendingIntent intent;
//        if (mService.isPlaying()) {
//            label = "pause";
//            icon = R.drawable.pause;
//            intent = mPauseIntent;
//        } else {
            label = "play";
            icon = R.drawable.ic_c_mobile_logo_master_cmyk;
            intent = mPlayIntent;
  //      }
        notificationBuilder.addAction(new android.support.v4.app.NotificationCompat.Action(icon, label, intent));
//        notificationBuilder.addAction(new android.support.v4.app.NotificationCompat.Action(R.drawable.stop, "stop", mStopIntent));

        return playPauseButtonPosition;
    }

    /**
     * Sets playback state.
     *
     * @param builder the builder
     */
    private void setNotificationPlaybackState(android.support.v4.app.NotificationCompat.Builder builder) {
        if (mService == null || !mStarted) {
            mService.stopForeground(true);
            return;
        }
        // Make sure that the notification can be dismissed by the user when we are not playing:
        builder.setOngoing(true);
    }


    /**
     * Creates Notification Channel. This is required in Android O+ to display notifications.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            "notification_channel",
                            NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription("notification_channel_description");

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
