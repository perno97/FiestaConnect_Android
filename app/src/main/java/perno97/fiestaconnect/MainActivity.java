package perno97.fiestaconnect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If we are connected to a module we want to start our SdlService
        SdlReceiver.queryForConnectedService(getApplicationContext());
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnSend:
                send(SdlService.TEXT_TO_SHOW_EXTRA);
                break;
            case R.id.btnNotifSettings:
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                break;
            case R.id.btnSpeak:
                send(SdlService.TEXT_TO_SPEAK_EXTRA);
                break;
            default:
                break;
        }
    }

    private void send(String contentType) {
        EditText toSend = findViewById(R.id.txtToSend);
        startService(SdlService.getIntent(getApplicationContext(), contentType, toSend.getText().toString()));
        //startService(NotificationListener.getIntent(getApplicationContext(), NotificationListener.CHECK_SONG_EXTRA));
    }
}
