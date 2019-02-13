package perno97.fiestaconnect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class NotificationListener extends NotificationListenerService implements  MediaSessionManager.OnActiveSessionsChangedListener{

    private static final int QUEUE_SIZE_THRESHOLD = 200;
    private static final String TAG = "notificationListener";

    public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
    public static final String TELEGRAM_PACK_NAME = "org.telegram.messenger";
    private static final String MESSENGER_PLUS_PACK_NAME = "org.telegram.plus";

    public static final String EXTRA_COMMAND = "command";
    public static final String NEXT_COMMAND_EXTRA = "next";
    public static final String FORCE_SHOW_COMMAND_EXTRA = "forceShow";
    public static final String REMOVE_CURRENT_NOTIFICATION_EXTRA = "removeCurrent";
    public static final String DELETE_NOTIFICATION_QUEUE_EXTRA = "deleteQueue";
    public static final String CHECK_SONG_EXTRA = "checkSong";
    public static final String STOPPED_SDL_COMMAND_EXTRA = "SdlStopped";
    public static final String STARTED_SDL_COMMAND_EXTRA = "SdlStarted";

    private static final String NOTHING_TO_SHOW_STRING = "Niente da mostrare.";

    private LinkedList<StatusBarNotification> notificationQueue = new LinkedList<>();
    private StatusBarNotification showingNotification;
    private String songTitle = "";
    private MediaSessionManager mgr;
    private MediaControllerCallback mediaControllerCallback;
    private boolean isSDLRunning;
    private List<MediaController> controllers;

    @Override
    public void onCreate() {
        super.onCreate();
        isSDLRunning = false;
        try {
            mgr = (MediaSessionManager) getApplicationContext().getSystemService(Context.MEDIA_SESSION_SERVICE);
            mgr.addOnActiveSessionsChangedListener(this, new ComponentName(getApplicationContext(), getClass()));
            mediaControllerCallback = new MediaControllerCallback();
            notificationQueue = new LinkedList<>();
            showingNotification = null;
        }
        catch (Exception e) {
            Log.e(TAG, "Probabilmente non autorizzato notification listener");
        }

    }

    @Override
    public void onDestroy() {
        if(mgr != null)
            mgr.removeOnActiveSessionsChangedListener(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras() != null && intent.getExtras().getString(EXTRA_COMMAND) != null)
            commandReceived(intent.getExtras().getString(EXTRA_COMMAND));

        return super.onStartCommand(intent, flags, startId);
    }

    private void commandReceived(String string) {
        switch (string){
            case NEXT_COMMAND_EXTRA:
                showNextNotification();
                break;
            case FORCE_SHOW_COMMAND_EXTRA:
                setShowingNotification(null);
                showNextNotification();
                break;
            case REMOVE_CURRENT_NOTIFICATION_EXTRA:
                removeCurrentNotification();
                break;
            case DELETE_NOTIFICATION_QUEUE_EXTRA:
                setNotificationQueue(notificationQueue = new LinkedList<>());
                break;
            case CHECK_SONG_EXTRA:
                checkSongPlaying();
                break;
            case STARTED_SDL_COMMAND_EXTRA:
                isSDLRunning = true;
                showSongTitle();
                break;
            case STOPPED_SDL_COMMAND_EXTRA:
                isSDLRunning = false;
                break;
            default:
                break;
        }
    }

    private void checkSongPlaying() {
        //Log.e(TAG, "CONTROLLO CANZONE IN RIPRODUZIONE");
        if (controllers != null) {
            for (MediaController c : controllers) {
                PlaybackState p = c.getPlaybackState();
                if (p != null && p.getState() == PlaybackState.STATE_PLAYING) {
                    //Log.e(TAG, "CANZONE IN RIPRODUZIONE PKG:" + c.getPackageName());
                    MediaMetadata meta = c.getMetadata();
                    if(meta != null) {
                        songTitle = meta.getString(MediaMetadata.METADATA_KEY_TITLE);
                        //Log.e(TAG, "FORZATO CONTROLLO TITOLO: " + songTitle);
                        if(isSDLRunning) showSongTitle();
                    }
                    else
                        songTitle = "";
                }
                else {
                    /*Log.e(TAG, c.getPackageName() + "NON IN RIPRODUZIONE");
                    if(p != null) {
                        Log.e(TAG, "PLAYBACKSTATE : " + p.getState());
                    }*/
                    songTitle = "";
                }
            }
        }
        else
            //Log.e(TAG,"NULL LISTA CONTROLLERS");
            songTitle = "";
    }

    private void setShowingNotification(StatusBarNotification sbn) {
        showingNotification = sbn;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if(isSDLRunning && (sbn.getPackageName().equals(WHATSAPP_PACK_NAME) || sbn.getPackageName().equals(TELEGRAM_PACK_NAME) || sbn.getPackageName().equals(MESSENGER_PLUS_PACK_NAME))) {
            if (showingNotification == null)
                showNotification(sbn);
            else {
                if (notificationQueue.size() >= QUEUE_SIZE_THRESHOLD)
                    setNotificationQueue(new LinkedList<>());
                if(notificationQueue.getLast().getKey() != sbn.getKey())
                    notificationQueue.add(sbn);
            }
        }
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

    public void showNextNotification() {
        showingNotification = null;
        if (notificationQueue.size() > 0) {
            showNotification(notificationQueue.poll());
        } else
            startService(SdlService.getIntent(getApplicationContext(), SdlService.TEXT_TO_SHOW_EXTRA, NOTHING_TO_SHOW_STRING));
    }

    public void removeCurrentNotification(){
        if(showingNotification != null)
            cancelNotification(showingNotification.getKey());
        showNextNotification();
    }

    public void setNotificationQueue(LinkedList<StatusBarNotification> notificationQueue) {
        this.notificationQueue = notificationQueue;
    }

    public class MediaControllerCallback extends MediaController.Callback {
        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            if(metadata != null)
                songTitle = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            else
                songTitle = "";

            Log.e(TAG,"METADATA CHANGED - TITLE: " + songTitle);
            if(isSDLRunning) showSongTitle();
        }

        @Override
        public void onPlaybackStateChanged(@Nullable PlaybackState state) {
            super.onPlaybackStateChanged(state);
            Log.e(TAG, "PLAYBACK STATE CHANGED");
            checkSongPlaying();
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            songTitle = "";
            if(isSDLRunning) showSongTitle();
        }
    }

    private void showSongTitle() {
        Log.e(TAG, "TITOLO INVIATO SDL: " + songTitle);
        startService(SdlService.getIntent(getApplicationContext(),SdlService.SONG_TITLE_EXTRA, songTitle));
    }

    @Override
    public void onActiveSessionsChanged(@Nullable List<MediaController> controllersReceived) {
        controllers = controllersReceived;
        if(controllers != null && !controllers.isEmpty()) {
            Log.e(TAG, "SIZE = " + controllers.size());
            for(MediaController c : controllers) {
                c.registerCallback(mediaControllerCallback);
                Log.e(TAG,"CALLBACK REGISTRATA PER --> " + c.getPackageName());
            }
        }
        else
            songTitle = "";
        Log.e(TAG, "ACTIVE SESSIONS CHANGED - TITLE: " + songTitle);
        if(isSDLRunning) showSongTitle();
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.e(TAG,"NOTIFICATION LISTENER CONNECTED");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.e(TAG,"NOTIFICATION LISTENER DISCONNECTED");
    }

    public static Intent getIntent(Context context, String command) {
        Intent intent = new Intent(context, NotificationListener.class);
        intent.putExtra(EXTRA_COMMAND, command);
        return intent;
    }
}