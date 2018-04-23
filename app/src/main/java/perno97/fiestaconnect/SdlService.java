package perno97.fiestaconnect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.widget.Toast;

import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
import com.smartdevicelink.proxy.interfaces.IProxyListenerALM;
import com.smartdevicelink.proxy.rpc.AddCommandResponse;
import com.smartdevicelink.proxy.rpc.AddSubMenuResponse;
import com.smartdevicelink.proxy.rpc.Alert;
import com.smartdevicelink.proxy.rpc.AlertManeuverResponse;
import com.smartdevicelink.proxy.rpc.AlertResponse;
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
import com.smartdevicelink.proxy.rpc.OnKeyboardInput;
import com.smartdevicelink.proxy.rpc.OnLanguageChange;
import com.smartdevicelink.proxy.rpc.OnLockScreenStatus;
import com.smartdevicelink.proxy.rpc.OnPermissionsChange;
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
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
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

import java.util.ArrayList;

public class SdlService extends Service implements IProxyListenerALM {

    private static final Integer CORRID_TEXT_INTENT = 0;
    private static final Integer CORRID_SUBSCRIBE_ASSISTANT = 1;
    private static final Integer CORRID_SUBSCRIBE_SEEKRIGHT = 2;
    private static final Integer CORRID_ALERT = 3;
    private static final Integer CORRID_SUBSCRIBE_SEEKLEFT = 4;
    private static final int BTN_NEXT_ID = 0;

    //The proxy handles communication between the application and SDL
    private SdlProxyALM proxy = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean forceConnect = intent !=null && intent.getBooleanExtra(TransportConstants.FORCE_TRANSPORT_CONNECTED, false);
        String txtExtra = null;

        if(intent != null)
            txtExtra = intent.getStringExtra(Intent.EXTRA_TEXT);

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

        if(txtExtra != null && proxy != null) {
            try {
                ArrayList<SoftButton> buttons = new ArrayList<>();
                SoftButton b = new SoftButton();
                b.setText("Succ.");
                b.setSoftButtonID(BTN_NEXT_ID);
                b.setType(SoftButtonType.SBT_TEXT);
                buttons.add(b);
                ScrollableMessage message = new ScrollableMessage();
                message.setScrollableMessageBody(txtExtra);
                message.setCorrelationID(CORRID_TEXT_INTENT);
                message.setSoftButtons(buttons);
                proxy.sendRPCRequest(message);
            } catch (SdlException e) {
                e.printStackTrace();
            }
        }

        //use START_STICKY because we want the SDLService to be explicitly started and stopped as needed.
        return START_STICKY;
    }

    public static Intent getIntent(Context context, String text){
        Intent intent = new Intent(context, SdlService.class);
        intent.putExtra(Intent.EXTRA_TEXT,text);
        return intent;
    }

    @Override
    public void onDestroy() {
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

        super.onDestroy();
    }

    @Override
    public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {
        //Stop the service
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
                SubscribeButton subscribeOkRequest = new SubscribeButton();
                subscribeOkRequest.setButtonName(ButtonName.OK);
                subscribeOkRequest.setCorrelationID(CORRID_SUBSCRIBE_ASSISTANT);
                SubscribeButton subscribeSeekRightRequest = new SubscribeButton();
                subscribeSeekRightRequest.setButtonName(ButtonName.SEEKRIGHT);
                subscribeSeekRightRequest.setCorrelationID(CORRID_SUBSCRIBE_SEEKRIGHT);
                SubscribeButton subscribeSeekLeftRequest = new SubscribeButton();
                subscribeSeekLeftRequest.setButtonName(ButtonName.SEEKLEFT);
                subscribeSeekLeftRequest.setCorrelationID(CORRID_SUBSCRIBE_SEEKLEFT);
                try {
                    proxy.show(getString(R.string.app_name), "", TextAlignment.CENTERED,0);
                    proxy.sendRPCRequest(subscribeOkRequest);
                    proxy.sendRPCRequest(subscribeSeekRightRequest);
                } catch (SdlException e) {
                    //TODO notificare errore
                }
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

    private void alert(String toShow){
        Alert alert = new Alert();
        alert.setAlertText1(toShow);
        alert.setAlertText2("");
        alert.setAlertText3("");
        alert.setTtsChunks(null);
        alert.setDuration(3000);
        alert.setPlayTone(false);
        alert.setProgressIndicator(false);
        alert.setCorrelationID(CORRID_ALERT);
        try {
            proxy.sendRPCRequest(alert);
        } catch (SdlException e) {
            //TODO notificare errore
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
        switch (notification.getButtonName()) {
            case OK:
                startActivity(new Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case SEEKLEFT:
                Toast.makeText(this, "PRECEDENTE", Toast.LENGTH_SHORT).show();
                Intent intentPrevious = new Intent(Intent.ACTION_MEDIA_BUTTON);
                synchronized (this) {
                    intentPrevious.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                    sendOrderedBroadcast(intentPrevious, null);

                    intentPrevious.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
                    sendOrderedBroadcast(intentPrevious, null);
                }
                break;
            case SEEKRIGHT:
                Toast.makeText(this, "PROSSIMA", Toast.LENGTH_SHORT).show();
                Intent intentNext = new Intent(Intent.ACTION_MEDIA_BUTTON);
                synchronized (this) {
                    intentNext.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
                    sendOrderedBroadcast(intentNext, null);

                    intentNext.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
                    sendOrderedBroadcast(intentNext, null);
                }
                break;
            case CUSTOM_BUTTON:
                switch (notification.getCustomButtonID()) {
                    case BTN_NEXT_ID:
                        startService(NotificationListener.getIntent(this, NotificationListener.EXTRA_COMMAND, NotificationListener.NEXT_COMMAND_EXTRA));
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onOnButtonPress(OnButtonPress notification) {
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
    // Inherited methods from IProxyListenerALM
}
