package perno97.fiestaconnect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

public class NotificationListener extends NotificationListenerService {


    private static final int QUEUE_SIZE_THRESHOLD = 200;
    private String TAG = "notificationListener";

    private static final String PLAY_MUSIC_PACK_NAME = "com.google.android.music";
    public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
    public static final String TELEGRAM_PACK_NAME = "org.telegram.messenger";
    private static final String YOUTUBE_PACK_NAME = "com.google.android.youtube";
    private static final String SPOTIFY_PACK_NAME = "com.spotify.music";
    private static final String MESSENGER_PLUS_PACK_NAME = "org.telegram.plus";

    public static final String EXTRA_COMMAND = "command";
    public static final String NEXT_COMMAND_EXTRA = "next";
    public static final String FORCE_SHOW_COMMAND_EXTRA = "forceShow";
    public static final String REMOVE_CURRENT_NOTIFICATION_EXTRA = "removeCurrent";
    public static final String DELETE_NOTIFICATION_QUEUE_EXTRA = "deleteQueue";
    public static final String CHECK_SONG_EXTRA = "checkSong";

    private static final String NOTHING_TO_SHOW_STRING = "Niente da mostrare.";

    private LinkedList<StatusBarNotification> notificationQueue;
    private StatusBarNotification showingNotification;
    private String songTitle;
    private String reproducingSongNotificationId;
    private MediaSessionManager mMediaSessionManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras() != null && intent.getExtras().getString(EXTRA_COMMAND) != null)
            commandReceived(intent.getExtras().getString(EXTRA_COMMAND));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        reset();
    }

    private void commandReceived(String string) {
        switch (string){
            case NEXT_COMMAND_EXTRA:
                showNextNotification();
                break;
            case FORCE_SHOW_COMMAND_EXTRA:
                setShowingNotification(null);
                showNextNotification();
                checkSong();
                break;
            case REMOVE_CURRENT_NOTIFICATION_EXTRA:
                removeCurrentNotification();
                break;
            case DELETE_NOTIFICATION_QUEUE_EXTRA:
                reset();
                break;
            case CHECK_SONG_EXTRA:
                showSongTitle();
            default:
                break;
        }
    }

    private void setShowingNotification(StatusBarNotification sbn) {
        showingNotification = sbn;
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
        if(sbn.getPackageName().equals(WHATSAPP_PACK_NAME) || sbn.getPackageName().equals(TELEGRAM_PACK_NAME) || sbn.getPackageName().equals(MESSENGER_PLUS_PACK_NAME)) {
            if (showingNotification == null)
                showNotification(sbn);
            else {
                if (notificationQueue.size() > QUEUE_SIZE_THRESHOLD)
                    setNotificationQueue(new LinkedList<StatusBarNotification>());
                notificationQueue.add(sbn);
            }
        }
        checkSong();
        /*if(sbn.getPackageName().equals(PLAY_MUSIC_PACK_NAME) || sbn.getPackageName().equals(YOUTUBE_PACK_NAME) || sbn.getPackageName().equals(SPOTIFY_PACK_NAME)){
            //songTitle = sbn.getNotification().extras.getString("android.title");
            songTitle = sbn.getNotification().extras.getCharSequence("android.title").toString();
            reproducingSongNotificationId = sbn.getKey();
            showSongTitle();
        }*/
    }

    private void checkSong(){
        List<MediaController> controllers = mMediaSessionManager.getActiveSessions(new ComponentName(this, this.getClass()));
        if(controllers.size() > 0) {
            Log.e(TAG, "SIZE = " + controllers.size());
            MediaMetadata metadata = controllers.get(0).getMetadata();
            if(metadata != null)
                songTitle = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            else
                songTitle = "";
        }
        else
            songTitle = "";
        Log.e(TAG, "TITLE = " + songTitle);
        showSongTitle();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.e(TAG, "NOTIFICATION REMOVED");
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            checkSong();
        }).start();
        //checkSong();
    }

    private void showSongTitle() {
        startService(SdlService.getIntent(getApplicationContext(),SdlService.SONG_TITLE_EXTRA, songTitle));
    }

    private void showNotification(StatusBarNotification sbn) {
        showingNotification = sbn;
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text;
        if(extras.getCharSequence("android.text") != null) {
            text = extras.getCharSequence("android.text").toString();
            Log.e(TAG, title + text);
            startService(SdlService.getIntent(getApplicationContext(), SdlService.NOTIFICATION_TEXT_EXTRA, title + "\n\n" + text));
        }
    }

    public void showNextNotification(){
        showingNotification = null;
        if(notificationQueue.size() > 0){
            showNotification(notificationQueue.poll());
        }
        else
            startService(SdlService.getIntent(getApplicationContext(), SdlService.TEXT_TO_SHOW_EXTRA, NOTHING_TO_SHOW_STRING));
    }

    private void reset() {
        notificationQueue = new LinkedList<>();
        showingNotification = null;
        mMediaSessionManager = this.getSystemService(MediaSessionManager.class);
    }

    public static Intent getIntent(Context context, String command) {
        Intent intent = new Intent(context, NotificationListener.class);
        intent.putExtra(EXTRA_COMMAND, command);
        return intent;
    }

    public void removeCurrentNotification(){
        if(showingNotification != null)
            cancelNotification(showingNotification.getKey());
        showNextNotification();
    }

    public void setNotificationQueue(LinkedList<StatusBarNotification> notificationQueue) {
        this.notificationQueue = notificationQueue;
    }
}