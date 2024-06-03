package com.pc.informamee.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pc.informamee.R;
import com.pc.informamee.common.requests.CapCheckRequest;
import com.pc.informamee.common.requests.RequestNotDoneException;

import static com.pc.informamee.mobile.RequestUploader.REQUESTNOTDONESTRING;
import static com.pc.informamee.mobile.RequestUploader.UPLOAD_OK;

public class MainActivity extends AppCompatActivity {
    private EditText capText;
    public static final String PREFS_NAME = "com.pc.informamee.mobile_preferences";
    private SharedPreferences AppPreferences;
    Handler hanl;
    boolean LockConfirmBtn=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int StoredCapValue=AppPreferences.getInt("cap", -1);
        if (StoredCapValue==-1){
            setContentView(R.layout.activity_main);
            getSupportActionBar().setTitle(getString(R.string.UpperBarTitle));
            hanl=new Handler(Looper.getMainLooper())
            {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    int State=msg.what;
                    LockConfirmBtn=false;
                    if (State==UPLOAD_OK) {
                        CapCheckRequest CheckedReq=(CapCheckRequest)msg.obj;
                        try {
                            Boolean CapIsValid= CheckedReq.getResult();
                            if (CapIsValid) {
                                SharedPreferences.Editor editor = AppPreferences.edit();
                                int CheckedCap = CheckedReq.getCAP();
                                editor.putInt("cap", CheckedCap);
                                editor.apply();
                                Intent APIntent = new Intent(MainActivity.this, AlertPage.class);
                                APIntent.putExtra("cap", CheckedCap);
                                startActivity(APIntent);
                                MainActivity.this.finish();
                            }
                            else AlertDialogOKBuilder.ShowAlert(MainActivity.this, "Il CAP inserito non esiste");
                        } catch (RequestNotDoneException e) {
                            AlertDialogOKBuilder.ShowAlert(MainActivity.this, REQUESTNOTDONESTRING);
                        }
                    }
                    else RequestUploader.ShowErrorMessage(MainActivity.this, State);
                }
            };
            capText = findViewById(R.id.editTextCAP);
            Button btnConferma = findViewById(R.id.buttonconferma);
            btnConferma.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (LockConfirmBtn) {
                        AlertDialogOKBuilder.ShowAlert(MainActivity.this, "Attendi... stiamo inoltrando la richiesta al server");
                        return;
                    }
                    String CapStr=capText.getText().toString();
                    CapStr=CapStr.trim();
                        try {
                            int CapInt = Integer.parseInt(CapStr);
                            CapCheckRequest CurrCapCheckRequest=new CapCheckRequest(CapInt);
                            RequestUploader CCUploader=new RequestUploader(CurrCapCheckRequest, hanl, MainActivity.this);
                            LockConfirmBtn=true;
                            CCUploader.Upload();
                        }
                        catch (NumberFormatException e) {
                            AlertDialogOKBuilder.ShowAlert(MainActivity.this, "CAP non valido. Il CAP è costituito da soli numeri");
                        }
                    }
            });
        } else {
            Intent APIntent = new Intent(MainActivity.this, AlertPage.class);
            APIntent.putExtra("cap",StoredCapValue);
            startActivity(APIntent);
            MainActivity.this.finish();
        }
    }
}
