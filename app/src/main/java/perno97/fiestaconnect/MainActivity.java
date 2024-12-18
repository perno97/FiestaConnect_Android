package perno97.fiestaconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateTextView();

        //If we are connected to a module we want to start our SdlService
        SdlReceiver.queryForConnectedService(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTextView();
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnSend:
                if(isRunning())
                    send(SdlService.TEXT_TO_SHOW_EXTRA);
                break;
            case R.id.btnNotifSettings:
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                break;
            case R.id.btnSpeak:
                if(isRunning())
                    send(SdlService.TEXT_TO_SPEAK_EXTRA);
                break;
            case R.id.btnRefresh:
                updateTextView();
                break;
            default:
                break;
        }
    }

    private void updateTextView() {
        TextView t = findViewById(R.id.txtRunning);
        if(isRunning())
            t.setText(R.string.textSdlRunning);
        else
            t.setText(R.string.textSdlNotRunning);
    }

    private void send(String contentType) {
        EditText toSend = findViewById(R.id.txtToSend);
        startService(SdlService.getIntent(getApplicationContext(), contentType, toSend.getText().toString()));
    }

    private boolean isRunning(){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getBoolean(getString(R.string.running), false);
    }
}
