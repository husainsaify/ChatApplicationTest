package com.hackerkernel.chatapplication.gcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;

import com.hackerkernel.chatapplication.R;
import com.hackerkernel.chatapplication.app.Config;
import com.hackerkernel.chatapplication.app.MyApplication;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class NotificationUtils {
    private static final String TAG = NotificationUtils.class.getSimpleName();
    private Context context;

    public NotificationUtils(){
    }

    public NotificationUtils(Context context){
        this.context = context;
    }

    public void showNotificationMessage(String title, String message, String timeStamp, Intent intent) {
        showNotificationMessage(title, message, timeStamp, intent, null);
    }

    public void showNotificationMessage(String title,String message,String timestamp,Intent intent,String imageUrl){
        //check for empty push notification
        if (TextUtils.isEmpty(message))
            return;

        int icon = R.mipmap.ic_launcher;
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //if image is not empty
        if (!TextUtils.isEmpty(imageUrl)){
            if (imageUrl.length() > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()){
                //download image from URL
                Bitmap bitmap = getBitmapFromURL(imageUrl);

                if (bitmap != null){
                    showBigNotification(bitmap,mBuilder,icon,title,message,timestamp,pendingIntent,alarmSound);
                }else{
                    showSmallNotification(mBuilder,icon,title,message,timestamp,pendingIntent,alarmSound);
                }
            }
        }else{
            showSmallNotification(mBuilder,icon,title,message,timestamp,pendingIntent,alarmSound);
        }
    }
    private void showSmallNotification(NotificationCompat.Builder mBuilder, int icon, String title, String message, String timestamp, PendingIntent pendingIntent, Uri alarmSound) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        if (Config.appendNotificationMessages){
            //store notification in Shared prefernces
            MyApplication.getInstance().getPrefManager().addNotification(message);

            //get the old notificatio from shared pref
            String oldNotification = MyApplication.getInstance().getPrefManager().getNotifications();

            List<String> messages = Arrays.asList(oldNotification.split("\\|"));
            for (int i = 0; i < messages.size(); i++) {
                inboxStyle.addLine(messages.get(i));
            }
        }else {
            inboxStyle.addLine(message);
        }

        //make notification
        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSound(alarmSound)
                .setStyle(inboxStyle)
                .setWhen(getTimeMilliSec(timestamp))
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon))
                .setContentText(message)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_ID,notification);
    }

    private void showBigNotification(Bitmap bitmap, NotificationCompat.Builder mBuilder, int icon, String title, String message, String timestamp, PendingIntent pendingIntent, Uri alarmSound) {
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title)
                .setSummaryText(Html.fromHtml(message).toString())
                .bigPicture(bitmap);

        Notification notification;
        notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSound(alarmSound)
                .setStyle(bigPictureStyle)
                .setWhen(getTimeMilliSec(timestamp))
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon))
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NOTIFICATION_ID_BIG_IMAGE,notification);
    }

    private Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    * Method to check if the app is in background or not
    * */
    public static boolean isAppIsInBackground(Context context){
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH){
            List<ActivityManager.RunningAppProcessInfo> runningProcess = am.getRunningAppProcesses();

            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcess){
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                    for (String activeProcess : processInfo.pkgList){
                        if (activeProcess.equals(context.getPackageName())){
                            isInBackground = false;
                        }
                    }
                }
            }
        }else{
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())){
                isInBackground = false;
            }
        }
       return isInBackground;
    }

    public static void clearNotifications(){
        NotificationManager manager = (NotificationManager) MyApplication.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    private long getTimeMilliSec(String timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timestamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
