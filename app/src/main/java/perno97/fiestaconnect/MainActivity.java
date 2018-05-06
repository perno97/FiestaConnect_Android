package perno97.fiestaconnect;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

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
                send();
                break;
            default:
                break;
        }
    }

    private void send() {
        EditText toSend = (EditText) findViewById(R.id.txtToSend);
        startService(SdlService.getIntent(getApplicationContext(), SdlService.TEXT_TO_SHOW_EXTRA, toSend.getText().toString()));
    }
}
