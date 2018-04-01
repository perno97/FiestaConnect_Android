package perno97.fiestaconnect;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

public class NotificationListener extends NotificationListenerService {

    private String TAG = "notificationListener";
    public static final String WHATSAPP_PACK_NAME = "com.whatsapp";

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.e(TAG,"NOTIFICATION LISTENER CONNECTED");
        Toast.makeText(this, R.string.toastNotList, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.e(TAG, "NOTIFICATION RECEIVED");
        if(sbn.getPackageName().equals(WHATSAPP_PACK_NAME))
            showNotification(sbn);
    }

    private void showNotification(StatusBarNotification sbn) {
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text;
        if(extras.getCharSequence("android.text") != null) {
            text = extras.getCharSequence("android.text").toString();
            startService(SdlService.getIntent(getApplicationContext(), "Titolo: " + title + ". \nTesto: " + text));
        }
    }
}