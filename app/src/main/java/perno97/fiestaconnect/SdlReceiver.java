package perno97.fiestaconnect;

import android.content.Context;
import android.content.Intent;

import com.smartdevicelink.transport.SdlBroadcastReceiver;

public class SdlReceiver extends SdlBroadcastReceiver {

    @Override
    public void onSdlEnabled(Context context, Intent intent) {
        //Use the provided intent but set the class to the SdlService
        intent.setClass(context, SdlService.class);
        context.startService(NotificationListener.getIntent(context, NotificationListener.DELETE_NOTIFICATION_QUEUE));
        context.startService(intent);
    }


    @Override
    public Class<? extends SdlRouterService> defineLocalSdlRouterClass() {
        //Return a local copy of the SdlRouterService located in your project
        return perno97.fiestaconnect.SdlRouterService.class;
    }
}