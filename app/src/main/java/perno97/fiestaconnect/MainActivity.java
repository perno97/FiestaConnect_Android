package perno97.fiestaconnect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If we are connected to a module we want to start our SdlService
        SdlReceiver.queryForConnectedService(this);
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
        EditText title = (EditText) findViewById(R.id.txtTitle);
        EditText subtitle = (EditText) findViewById(R.id.txtSubtitle);
        startService(SdlService.getIntent(this, title.getText().toString(), subtitle.getText().toString()));
    }
}
