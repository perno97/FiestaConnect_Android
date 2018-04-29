package perno97.fiestaconnect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;

public class NotificationListener extends NotificationListenerService {

    private String TAG = "notificationListener";
    public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
    public static final String TELEGRAM_PACK_NAME = "org.telegram.messenger";
    public static final String EXTRA_COMMAND = "command";
    public static final String NEXT_COMMAND_EXTRA = "next";
    public static final String FORCE_SHOW_COMMAND_EXTRA = "force-show";
    private static final String NOTHING_TO_SHOW_STRING = "Niente da mostrare.";

    private LinkedList<StatusBarNotification> notificationQueue;
    private boolean showingNotification;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras().getString(EXTRA_COMMAND) != null)
            commandReceived(intent.getExtras().getString(EXTRA_COMMAND));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationQueue = new LinkedList<>();
        showingNotification = false;
    }

    private void commandReceived(String string) {
        switch (string){
            case NEXT_COMMAND_EXTRA:
                showNextNotification();
                break;
            case FORCE_SHOW_COMMAND_EXTRA:
                setShowingNotification(false);
                showNextNotification();
                break;
            default:
                break;
        }
    }

    private void setShowingNotification(boolean b) {
        showingNotification = b;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.e(TAG,"NOTIFICATION LISTENER CONNECTED");
        Toast.makeText(this, R.string.toastNotList, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.e(TAG, "NOTIFICATION RECEIVED");
        if(sbn.getPackageName().equals(WHATSAPP_PACK_NAME) || sbn.getPackageName().equals(TELEGRAM_PACK_NAME)) {
            if(showingNotification == false)
                showNotification(sbn);
            else
                notificationQueue.add(sbn);
        }
    }

    private void showNotification(StatusBarNotification sbn) {
        showingNotification = true;
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text;
        if(extras.getCharSequence("android.text") != null) {
            text = extras.getCharSequence("android.text").toString();
            Log.e(TAG, title + text);
            startService(SdlService.getIntent(getApplicationContext(), title + "\n\n" + text));
        }
    }

    public void showNextNotification(){
        showingNotification = false;
        if(notificationQueue.size() != 0){
            showNotification(notificationQueue.poll());
        }
        else
            startService(SdlService.getIntent(getApplicationContext(), NOTHING_TO_SHOW_STRING));
    }

    public static Intent getIntent(Context context, String extraType, String extra) {
        Intent intent = new Intent(context, NotificationListener.class);
        intent.putExtra(extraType, extra);
        return intent;
    }
}