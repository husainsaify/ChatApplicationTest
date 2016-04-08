package com.hackerkernel.chatapplication.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class MyPreferenceManager {
    private String TAG = MyPreferenceManager.class.getSimpleName();

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context context;

    // Sharedpref file name
    private static final String PREF_NAME = "androidhive_gcm";

    // All Shared Preferences Keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_NOTIFICATIONS = "notifications";

    public MyPreferenceManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void addNotification(String notification){
        String oldNotificaiton = getNotifications();
        if(oldNotificaiton != null){
            oldNotificaiton += "|" + notification;
        }else{
            oldNotificaiton = notification;
        }

        editor.putString(KEY_NOTIFICATIONS,oldNotificaiton);
        editor.apply();
    }

    public String getNotifications(){
        return pref.getString(KEY_NOTIFICATIONS,null);
    }

    public void clear(){
        editor.clear();
        editor.apply();
    }
}
