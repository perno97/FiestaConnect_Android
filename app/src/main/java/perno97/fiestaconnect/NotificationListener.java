package perno97.fiestaconnect;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

public class NotificationListener extends NotificationListenerService {

    private String TAG = "notificationListener";

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.e(TAG,"NOTIFICATION LISTENER CONNECTED");
        Toast.makeText(this, "NOTIFICATION LISTENER CONNECTED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.e(TAG, "NOTIFICATION RECEIVED");
        Toast.makeText(this, "NOTIFICATION RECEIVED", Toast.LENGTH_SHORT).show();
        startService(SdlService.getIntent(getApplicationContext(), "Notifica"));
        /*Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text;
        if(extras.getCharSequence("android.text") != null) {
            text = extras.getCharSequence("android.text").toString();
            startService(SdlService.getIntent(getApplicationContext(), "Titolo: " + title + ". Testo: " + text));
        }*/
    }
}