package perno97.fiestaconnect;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;

import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
import com.smartdevicelink.proxy.interfaces.IProxyListenerALM;
import com.smartdevicelink.proxy.rpc.AddCommandResponse;
import com.smartdevicelink.proxy.rpc.AddSubMenuResponse;
import com.smartdevicelink.proxy.rpc.AlertManeuverResponse;
import com.smartdevicelink.proxy.rpc.AlertResponse;
import com.smartdevicelink.proxy.rpc.ButtonPressResponse;
import com.smartdevicelink.proxy.rpc.ChangeRegistrationResponse;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteCommandResponse;
import com.smartdevicelink.proxy.rpc.DeleteFileResponse;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteSubMenuResponse;
import com.smartdevicelink.proxy.rpc.DiagnosticMessageResponse;
import com.smartdevicelink.proxy.rpc.DialNumberResponse;
import com.smartdevicelink.proxy.rpc.EndAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.GenericResponse;
import com.smartdevicelink.proxy.rpc.GetDTCsResponse;
import com.smartdevicelink.proxy.rpc.GetInteriorVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.GetSystemCapabilityResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.GetWayPointsResponse;
import com.smartdevicelink.proxy.rpc.ListFilesResponse;
import com.smartdevicelink.proxy.rpc.OnAudioPassThru;
import com.smartdevicelink.proxy.rpc.OnButtonEvent;
import com.smartdevicelink.proxy.rpc.OnButtonPress;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnDriverDistraction;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnHashChange;
import com.smartdevicelink.proxy.rpc.OnInteriorVehicleData;
import com.smartdevicelink.proxy.rpc.OnKeyboardInput;
import com.smartdevicelink.proxy.rpc.OnLanguageChange;
import com.smartdevicelink.proxy.rpc.OnLockScreenStatus;
import com.smartdevicelink.proxy.rpc.OnPermissionsChange;
import com.smartdevicelink.proxy.rpc.OnRCStatus;
import com.smartdevicelink.proxy.rpc.OnStreamRPC;
import com.smartdevicelink.proxy.rpc.OnSystemRequest;
import com.smartdevicelink.proxy.rpc.OnTBTClientState;
import com.smartdevicelink.proxy.rpc.OnTouchEvent;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.OnWayPointChange;
import com.smartdevicelink.proxy.rpc.PerformAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.PerformInteractionResponse;
import com.smartdevicelink.proxy.rpc.PutFileResponse;
import com.smartdevicelink.proxy.rpc.ReadDIDResponse;
import com.smartdevicelink.proxy.rpc.ResetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.ScrollableMessage;
import com.smartdevicelink.proxy.rpc.ScrollableMessageResponse;
import com.smartdevicelink.proxy.rpc.SendHapticDataResponse;
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetInteriorVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
import com.smartdevicelink.proxy.rpc.Show;
import com.smartdevicelink.proxy.rpc.ShowConstantTbtResponse;
import com.smartdevicelink.proxy.rpc.ShowResponse;
import com.smartdevicelink.proxy.rpc.SliderResponse;
import com.smartdevicelink.proxy.rpc.SoftButton;
import com.smartdevicelink.proxy.rpc.SpeakResponse;
import com.smartdevicelink.proxy.rpc.StreamRPCResponse;
import com.smartdevicelink.proxy.rpc.SubscribeButton;
import com.smartdevicelink.proxy.rpc.SubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.SystemRequestResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.UpdateTurnListResponse;
import com.smartdevicelink.proxy.rpc.enums.ButtonName;
import com.smartdevicelink.proxy.rpc.enums.Language;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;
import com.smartdevicelink.proxy.rpc.enums.SoftButtonType;
import com.smartdevicelink.proxy.rpc.enums.TextAlignment;
import com.smartdevicelink.transport.TransportConstants;
import com.smartdevicelink.util.CorrelationIdGenerator;

import java.util.ArrayList;
import java.util.List;

public class SdlService extends Service implements IProxyListenerALM {

    private static final String TAG = "SdlService";

    public static final String EXTRA_TYPE = "type";
    private static final String EXTRA_CONTENT = "content";
    public static final String SONG_TITLE_EXTRA = "songTitle";
    public static final String TEXT_TO_SHOW_EXTRA = "textToShow";
    public static final String TEXT_TO_SPEAK_EXTRA = "textToSpeak";
    public static final String NOTIFICATION_TEXT_EXTRA = "notificationText";

    private static final String BTN_NEXT_STRING = "Succ.";
    private static final String BTN_PLAY_PAUSE_STRING = "Play";
    private static final Integer NOTIFICATION_MESSAGE_CORR_ID = CorrelationIdGenerator.generateId();
    private static final String BTN_NOTIFICATION_STRING = "Notifiche";
    private static final String BTN_DELETE_STRING = "Rimuovi";
    private static final String CLEAR_NOTIFICATION_QUEUE_CMD_STRING = "Reset coda notifiche";
    private static final String CHECK_SONG_PLAYING_TITLE_CMD_STRING = "Controlla titolo canzone in riproduzione";

    private static final int BTN_NEXT_ID = 0;
    private static final int BTN_PLAY_PAUSE_ID = 1;
    private static final int BTN_NOTIFICATION_ID = 2;
    private static final int BTN_DELETE_ID = 3;
    private static final int CLEAR_NOTIFICATION_QUEUE_CMD_ID = 4;
    private static final int CHECK_SONG_PLAYING_TITLE_CMD_ID = 5;
    private static final String CHANNEL_ID = "12345";
    private static final String CHANNEL_NAME = "Default channel";
    private static final int NOTIFICATION_ID = 44;
    private static final String START_MESSAGE = "FiestaConnect";


    //The proxy handles communication between the application and SDL
    private SdlProxyALM proxy = null;
    private String mainText1 = "";
    private String mainText2 = "";
    private String mainText3 = "";
    private Context context;
    private NotificationChannel notificationChannel;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean forceConnect = intent !=null && intent.getBooleanExtra(TransportConstants.FORCE_TRANSPORT_CONNECTED, false);//TODO update

        if (proxy == null) {
            try {
                //Create a new proxy using Bluetooth transport
                //The listener, app name,
                //whether or not it is a media app and the applicationId are supplied.
                proxy = new SdlProxyALM(this.getBaseContext(),this, getString(R.string.app_name), true, Language.IT_IT, Language.IT_IT, "8675309");
            } catch (SdlException e) {
                //There was an error creating the proxy
                if (proxy == null) {
                    //Stop the SdlService
                    stopSelf();
                }
            }
        }else if(forceConnect){
            proxy.forceOnConnected();
        }

        manageIntent(intent);

        if(proxy != null) {
            if(mainText1.length() == 0 && mainText2.length() == 0 && mainText3.length() == 0)
                mainText1 = START_MESSAGE;
            try {
                proxy.show(mainText1, mainText2, mainText3, null, null, null, null, TextAlignment.CENTERED,CorrelationIdGenerator.generateId());
            } catch (SdlException e) {
                Log.e(TAG, "ERRORE VISUALIZZAZIONE TESTO");
            }
        }

        //use START_STICKY because we want the SDLService to be explicitly started and stopped as needed.
        return START_STICKY;
    }

    private void manageIntent(Intent intent) {
        String txtExtra = null;
        String notificationToShow = null;
        String txtToSpeak = null;
        if(intent != null && intent.getExtras() != null && intent.getExtras().getString(EXTRA_TYPE) != null && intent.getExtras().getString(EXTRA_CONTENT)!= null){
            switch (intent.getExtras().getString(EXTRA_TYPE)){
                case TEXT_TO_SHOW_EXTRA:
                    txtExtra = intent.getExtras().getString(EXTRA_CONTENT);
                    break;
                case TEXT_TO_SPEAK_EXTRA:
                    txtToSpeak = intent.getExtras().getString(EXTRA_CONTENT);
                    break;
                case NOTIFICATION_TEXT_EXTRA:
                    notificationToShow = intent.getExtras().getString(EXTRA_CONTENT);
                    Log.e("SdlService", "MAIN TEXT1: " + mainText1);
                    break;
                case SONG_TITLE_EXTRA:
                    String title = intent.getExtras().getString(EXTRA_CONTENT);
                    try {
                        if (title.length() > 15 && title.length() <= 30) {
                            mainText1 = title.substring(0, 16);
                            mainText2 = title.substring(16);
                            mainText3 = "";
                        } else if (title.length() > 30) {
                            mainText1 = title.substring(0, 16);
                            mainText2 = title.substring(16, 31);
                            mainText3 = title.substring(31);
                        } else {
                            mainText1 = title;
                            mainText2 = "";
                            mainText3 = "";
                        }
                    } catch (IndexOutOfBoundsException e) {
                        mainText1 = "Out of bound";
                        mainText2 = String.valueOf(title.length());
                        mainText3 = "15 o 30";
                    }
                default:
                    break;
            }
        }

        if(notificationToShow != null && proxy != null) {
            ArrayList<SoftButton> buttons = new ArrayList<>();
            SoftButton buttonNext = new SoftButton();
            buttonNext.setText(BTN_NEXT_STRING);
            buttonNext.setSoftButtonID(BTN_NEXT_ID);
            buttonNext.setType(SoftButtonType.SBT_TEXT);
            buttons.add(buttonNext);

            SoftButton buttonDelete = new SoftButton();
            buttonDelete.setText(BTN_DELETE_STRING);
            buttonDelete.setSoftButtonID(BTN_DELETE_ID);
            buttonDelete.setType(SoftButtonType.SBT_TEXT);
            buttons.add(buttonDelete);

            ScrollableMessage notification = new ScrollableMessage();
            notification.setScrollableMessageBody(notificationToShow);
            notification.setCorrelationID(NOTIFICATION_MESSAGE_CORR_ID);
            notification.setSoftButtons(buttons);
            try {
                proxy.sendRPCRequest(notification);
            } catch (SdlException e) {
                e.printStackTrace();
            }
        }
        else if(txtExtra != null && proxy != null){
            ScrollableMessage message = new ScrollableMessage();
            message.setScrollableMessageBody(txtExtra);
            message.setCorrelationID(CorrelationIdGenerator.generateId());
            try {
                proxy.sendRPCRequest(message);
            } catch (SdlException e) {
                e.printStackTrace();
            }
        }
        else if(txtToSpeak != null && proxy != null){
            try {
                proxy.speak(txtToSpeak, CorrelationIdGenerator.generateId());
            } catch (SdlException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeSharedPreference(boolean value){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(getString(R.string.running), value);
        editor.apply();
    }

    public static Intent getIntent(Context context, String contentType, String content){
        Intent intent = new Intent(context, SdlService.class);
        intent.putExtra(EXTRA_TYPE, contentType);
        intent.putExtra(EXTRA_CONTENT, content);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        context.startService(NotificationListener.getIntent(context, NotificationListener.STARTED_SDL_COMMAND_EXTRA));
        writeSharedPreference(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            notificationManager.createNotificationChannel(notificationChannel);
            Notification serviceNotification = new Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(START_MESSAGE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.ic_launcher))
            .setContentText("Running")
            .setColor(Color.BLACK)
            .setChannelId(CHANNEL_ID)
                    .build();
            startForeground(NOTIFICATION_ID, serviceNotification);
        }
    }

    @Override
    public void onDestroy() {
        writeSharedPreference(false);
        //Dispose of the proxy
        if (proxy != null) {
            try {
                proxy.dispose();
            } catch (SdlException e) {
                e.printStackTrace();
            } finally {
                proxy = null;
            }
        }

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager!=null){ //If this is the only notification on your channel
                notificationManager.deleteNotificationChannel(notificationChannel.getId());
            }
            stopForeground(true);
        }

        context.startService(NotificationListener.getIntent(context, NotificationListener.STOPPED_SDL_COMMAND_EXTRA));
        super.onDestroy();
    }

    @Override
    public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {
        //Stop the service
        context.startService(NotificationListener.getIntent(context, NotificationListener.STOPPED_SDL_COMMAND_EXTRA));
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onOnHMIStatus(OnHMIStatus notification) {
        switch(notification.getHmiLevel()) {
            case HMI_FULL:
                //TODO send welcome message, addcommands, subscribe to buttons ect
                //Sottoscrizione ai bottoni
                SubscribeButton subscribeOkRequest = new SubscribeButton();
                subscribeOkRequest.setButtonName(ButtonName.OK);
                subscribeOkRequest.setCorrelationID(CorrelationIdGenerator.generateId());
                SubscribeButton subscribeSeekRightRequest = new SubscribeButton();
                subscribeSeekRightRequest.setButtonName(ButtonName.SEEKRIGHT);
                subscribeSeekRightRequest.setCorrelationID(CorrelationIdGenerator.generateId());
                SubscribeButton subscribeSeekLeftRequest = new SubscribeButton();
                subscribeSeekLeftRequest.setButtonName(ButtonName.SEEKLEFT);
                subscribeSeekLeftRequest.setCorrelationID(CorrelationIdGenerator.generateId());

                //Softbuttons
                List<SoftButton> softButtonList = new ArrayList<>();
                SoftButton playPauseSoftButton = new SoftButton();
                playPauseSoftButton.setType(SoftButtonType.SBT_TEXT);
                playPauseSoftButton.setText(BTN_PLAY_PAUSE_STRING);
                playPauseSoftButton.setSoftButtonID(BTN_PLAY_PAUSE_ID);
                SoftButton forceShowNotificationSoftButton = new SoftButton();
                forceShowNotificationSoftButton.setType(SoftButtonType.SBT_TEXT);
                forceShowNotificationSoftButton.setText(BTN_NOTIFICATION_STRING);
                forceShowNotificationSoftButton.setSoftButtonID(BTN_NOTIFICATION_ID);
                softButtonList.add(playPauseSoftButton);
                softButtonList.add(forceShowNotificationSoftButton);

                //Invio rpc request
                Show show = new Show();
                show.setSoftButtons(softButtonList);
                show.setCorrelationID(CorrelationIdGenerator.generateId());
                try {
                    proxy.sendRPCRequest(show);
                    proxy.sendRPCRequest(subscribeOkRequest);
                    proxy.sendRPCRequest(subscribeSeekRightRequest);
                    proxy.sendRPCRequest(subscribeSeekLeftRequest);
                    proxy.addCommand(CLEAR_NOTIFICATION_QUEUE_CMD_ID,CLEAR_NOTIFICATION_QUEUE_CMD_STRING,CorrelationIdGenerator.generateId());
                    proxy.addCommand(CHECK_SONG_PLAYING_TITLE_CMD_ID, CHECK_SONG_PLAYING_TITLE_CMD_STRING, CorrelationIdGenerator.generateId());
                } catch (SdlException e) {
                    Log.e(TAG, "ERRORE IN HMI STATUS CHANGED");
                }
                startService(NotificationListener.getIntent(context, NotificationListener.CHECK_SONG_EXTRA));
                break;
            case HMI_LIMITED:
                break;
            case HMI_BACKGROUND:
                break;
            case HMI_NONE:
                break;
            default:
        }
    }

    @Override
    public void onServiceEnded(OnServiceEnded serviceEnded) {

    }

    @Override
    public void onServiceNACKed(OnServiceNACKed serviceNACKed) {

    }

    @Override
    public void onOnStreamRPC(OnStreamRPC notification) {

    }

    @Override
    public void onStreamRPCResponse(StreamRPCResponse response) {

    }

    @Override
    public void onError(String info, Exception e) {

    }

    @Override
    public void onGenericResponse(GenericResponse response) {

    }

    @Override
    public void onOnCommand(OnCommand notification) {
        switch (notification.getCmdID()){
            case CLEAR_NOTIFICATION_QUEUE_CMD_ID:
                startService(NotificationListener.getIntent(context, NotificationListener.DELETE_NOTIFICATION_QUEUE_EXTRA));
                break;
            case CHECK_SONG_PLAYING_TITLE_CMD_ID:
                startService(NotificationListener.getIntent(context, NotificationListener.CHECK_SONG_EXTRA));
                break;
            default:
                break;
        }
    }

    @Override
    public void onAddCommandResponse(AddCommandResponse response) {

    }

    @Override
    public void onAddSubMenuResponse(AddSubMenuResponse response) {

    }

    @Override
    public void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse response) {

    }

    @Override
    public void onAlertResponse(AlertResponse response) {

    }

    @Override
    public void onDeleteCommandResponse(DeleteCommandResponse response) {

    }

    @Override
    public void onDeleteInteractionChoiceSetResponse(DeleteInteractionChoiceSetResponse response) {

    }

    @Override
    public void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {

    }

    @Override
    public void onPerformInteractionResponse(PerformInteractionResponse response) {

    }

    @Override
    public void onResetGlobalPropertiesResponse(ResetGlobalPropertiesResponse response) {

    }

    @Override
    public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {

    }

    @Override
    public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {

    }

    @Override
    public void onShowResponse(ShowResponse response) {

    }

    @Override
    public void onSpeakResponse(SpeakResponse response) {

    }

    @Override
    public void onOnButtonEvent(OnButtonEvent notification) {
    }

    @Override
    public void onOnButtonPress(OnButtonPress notification) {
        switch (notification.getButtonName()) {
            case OK:
                startActivity(new Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case SEEKLEFT:
                if(getSystemService(AudioManager.class) != null) {
                    getSystemService(AudioManager.class).dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                    getSystemService(AudioManager.class).dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                }
                break;
            case SEEKRIGHT:
                if(getSystemService(AudioManager.class) != null) {
                    getSystemService(AudioManager.class).dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
                    getSystemService(AudioManager.class).dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
                }
                break;
            case CUSTOM_BUTTON:
                switch (notification.getCustomButtonName()) {
                    case BTN_NEXT_ID:
                        startService(NotificationListener.getIntent(context, NotificationListener.NEXT_COMMAND_EXTRA));
                        break;
                    case BTN_DELETE_ID:
                        startService(NotificationListener.getIntent(context, NotificationListener.REMOVE_CURRENT_NOTIFICATION_EXTRA));
                        break;
                    case BTN_PLAY_PAUSE_ID:
                        if(getSystemService(AudioManager.class) != null) {
                            getSystemService(AudioManager.class).dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                            getSystemService(AudioManager.class).dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
                        }
                        break;
                    case BTN_NOTIFICATION_ID:
                        startService(NotificationListener.getIntent(context, NotificationListener.FORCE_SHOW_COMMAND_EXTRA));
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onSubscribeButtonResponse(SubscribeButtonResponse response) {

    }

    @Override
    public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {

    }

    @Override
    public void onOnPermissionsChange(OnPermissionsChange notification) {

    }

    @Override
    public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {

    }

    @Override
    public void onUnsubscribeVehicleDataResponse(UnsubscribeVehicleDataResponse response) {

    }

    @Override
    public void onGetVehicleDataResponse(GetVehicleDataResponse response) {

    }

    @Override
    public void onOnVehicleData(OnVehicleData notification) {

    }

    @Override
    public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {

    }

    @Override
    public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {

    }

    @Override
    public void onOnAudioPassThru(OnAudioPassThru notification) {

    }

    @Override
    public void onPutFileResponse(PutFileResponse response) {

    }

    @Override
    public void onDeleteFileResponse(DeleteFileResponse response) {

    }

    @Override
    public void onListFilesResponse(ListFilesResponse response) {

    }

    @Override
    public void onSetAppIconResponse(SetAppIconResponse response) {

    }

    @Override
    public void onScrollableMessageResponse(ScrollableMessageResponse response) {

    }

    @Override
    public void onChangeRegistrationResponse(ChangeRegistrationResponse response) {

    }

    @Override
    public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {

    }

    @Override
    public void onOnLanguageChange(OnLanguageChange notification) {

    }

    @Override
    public void onOnHashChange(OnHashChange notification) {

    }

    @Override
    public void onSliderResponse(SliderResponse response) {

    }

    @Override
    public void onOnDriverDistraction(OnDriverDistraction notification) {

    }

    @Override
    public void onOnTBTClientState(OnTBTClientState notification) {

    }

    @Override
    public void onOnSystemRequest(OnSystemRequest notification) {

    }

    @Override
    public void onSystemRequestResponse(SystemRequestResponse response) {

    }

    @Override
    public void onOnKeyboardInput(OnKeyboardInput notification) {

    }

    @Override
    public void onOnTouchEvent(OnTouchEvent notification) {

    }

    @Override
    public void onDiagnosticMessageResponse(DiagnosticMessageResponse response) {

    }

    @Override
    public void onReadDIDResponse(ReadDIDResponse response) {

    }

    @Override
    public void onGetDTCsResponse(GetDTCsResponse response) {

    }

    @Override
    public void onOnLockScreenNotification(OnLockScreenStatus notification) {

    }

    @Override
    public void onDialNumberResponse(DialNumberResponse response) {

    }

    @Override
    public void onSendLocationResponse(SendLocationResponse response) {

    }

    @Override
    public void onShowConstantTbtResponse(ShowConstantTbtResponse response) {

    }

    @Override
    public void onAlertManeuverResponse(AlertManeuverResponse response) {

    }

    @Override
    public void onUpdateTurnListResponse(UpdateTurnListResponse response) {

    }

    @Override
    public void onServiceDataACK(int dataSize) {

    }

    @Override
    public void onGetWayPointsResponse(GetWayPointsResponse response) {

    }

    @Override
    public void onSubscribeWayPointsResponse(SubscribeWayPointsResponse response) {

    }

    @Override
    public void onUnsubscribeWayPointsResponse(UnsubscribeWayPointsResponse response) {

    }

    @Override
    public void onOnWayPointChange(OnWayPointChange notification) {

    }

    @Override
    public void onGetSystemCapabilityResponse(GetSystemCapabilityResponse response) {

    }

    @Override
    public void onGetInteriorVehicleDataResponse(GetInteriorVehicleDataResponse response) {

    }

    @Override
    public void onButtonPressResponse(ButtonPressResponse response) {

    }

    @Override
    public void onSetInteriorVehicleDataResponse(SetInteriorVehicleDataResponse response) {

    }

    @Override
    public void onOnInteriorVehicleData(OnInteriorVehicleData notification) {

    }

    @Override
    public void onSendHapticDataResponse(SendHapticDataResponse response) {

    }

    @Override
    public void onOnRCStatus(OnRCStatus notification) {

    }

}
