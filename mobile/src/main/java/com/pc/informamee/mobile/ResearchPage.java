package com.pc.informamee.mobile;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pc.informamee.R;
import com.pc.informamee.common.CapParseException;
import com.pc.informamee.common.MultiCapParser;
import com.pc.informamee.common.events.Event;
import com.pc.informamee.common.requests.*;

import java.util.ArrayList;
import java.util.Calendar;

import static com.pc.informamee.mobile.RequestUploader.*;
import static java.util.Calendar.*;

public class ResearchPage extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {

    EditText etDate;
    EditText etTime;
    TimePickerDialog timePickerDialog;
    Switch CapSwitch;
    EditText etCap;
    Spinner spDanger;
    Button bSearch;
    CheckBox AtmosfericoCheckBox;
    CheckBox TerroristoCheckBox;
    CheckBox SismicoCheckBox;
    boolean LockConfirmBtn=false;

    Calendar calendar;
    private Handler hanl;

    //SEARCH BUTTON ENABLER
    private boolean TimeSet=false;
    private boolean DateSet=false;
    private void TimeSetSearchButtonStateHandler()
    {
        TimeSet=true;
        bSearch.setEnabled(DateSet);
    }
    private void DateSetSearchButtonStateHandler()
    {
        DateSet=true;
        bSearch.setEnabled(TimeSet);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_research_page);
        getSupportActionBar().setTitle(getString(R.string.EventSearchTitle));

        etDate = findViewById(R.id.editTextDate);
        etTime = findViewById(R.id.editTextHour);
        CapSwitch= findViewById(R.id.switchCAP);
        etCap= findViewById(R.id.editTextSearchCap);
        spDanger= findViewById(R.id.spinnerDanger);
        bSearch= findViewById(R.id.buttonSearch);
        AtmosfericoCheckBox=findViewById(R.id.AtmosfericoCheckBox);
        TerroristoCheckBox=findViewById(R.id.TerroristoCheckBox);
        SismicoCheckBox=findViewById(R.id.SismicoCheckBox);

        calendar = getInstance();

//DATEPICKER
        final DatePickerDialog datePickerDialog = new DatePickerDialog(ResearchPage.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String date = day + "/" + (month + 1) + "/" + year;
                calendar.set(year, month, day);
                etDate.setText(date);
                DateSetSearchButtonStateHandler();
            }
        }, calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH));

        etDate.setOnTouchListener(new EditText.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                datePickerDialog.show();
                return false;
            }
        });

    //TIMEPICKER
        timePickerDialog = new TimePickerDialog(ResearchPage.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
                StringBuilder HourToPrintBuilder=new StringBuilder(5);
                HourToPrintBuilder.append(hourOfDay).append(':');
                if (minutes<10)
                    HourToPrintBuilder.append(0);
                HourToPrintBuilder.append(minutes);
                etTime.setText(HourToPrintBuilder.toString());
                calendar.set(HOUR_OF_DAY, hourOfDay);
                calendar.set(MINUTE, minutes);
                TimeSetSearchButtonStateHandler();
            }
        }, calendar.get(HOUR_OF_DAY), calendar.get(MINUTE), true);

        etTime.setOnTouchListener(new EditText.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                timePickerDialog.show();
                return false;
            }
        });

        //DANGER SELECTOR
        ArrayAdapter<CharSequence> danAdapter= ArrayAdapter.createFromResource(this, R.array.danger, android.R.layout.simple_spinner_item );
        danAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDanger.setAdapter(danAdapter);
        spDanger.setOnItemSelectedListener(this);

        //CAP EDIT TEXT VISIBLE-NOT VISIBLE
        CapSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (CapSwitch.isChecked()){
                    etCap.setVisibility(View.VISIBLE);
                }
                else{
                    etCap.setVisibility(View.INVISIBLE);
                }
            }
        });

        hanl=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message inputMessage) {
                int State=inputMessage.what;
                LockConfirmBtn=false;
                if (State==UPLOAD_OK) {
                    FindRequest Result=(FindRequest)inputMessage.obj;
                    ArrayList<Event> ResultEvents;
                    try {
                        ResultEvents = Result.getResult();
                        Intent FRIntent=new Intent(ResearchPage.this, FinalResults.class);
                        FRIntent.putExtra("EventsResults", ResultEvents);
                        startActivity(FRIntent);
                    } catch (RequestNotDoneException e) {
                        if (Result instanceof FindRequestCap)
                        {
                            FindRequestCap FRCResult=(FindRequestCap) Result;
                            ArrayList<Integer> InvalidCAPs=FRCResult.getInvalidCAPs();
                            if (InvalidCAPs==null) {
                                AlertDialogOKBuilder.ShowAlert(ResearchPage.this, REQUESTNOTDONESTRING);
                            }
                            else{
                                if (InvalidCAPs.size()==1) {
                                    AlertDialogOKBuilder.ShowAlert(ResearchPage.this, "Il CAP "+InvalidCAPs.get(0).toString()+" non è valido");
                                }
                                else {
                                    StringBuilder CurrStringBuilder = new StringBuilder(100);
                                    for (Integer Cap : InvalidCAPs) {
                                        CurrStringBuilder.append(Cap);
                                        CurrStringBuilder.append(' ');
                                    }
                                    AlertDialogOKBuilder.ShowAlert(ResearchPage.this, "I CAP " + CurrStringBuilder.toString() + "non sono validi");
                                }
                            }
                        }
                        else {
                            AlertDialogOKBuilder.ShowAlert(ResearchPage.this, REQUESTNOTDONESTRING);
                        }
                    }
                }
                else {
                    RequestUploader.ShowErrorMessage(ResearchPage.this, State);
                }
            }
        };

        //SEARCH BUTTON CLICK: REQUEST GENERATION
        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LockConfirmBtn) {
                    AlertDialogOKBuilder.ShowAlert(ResearchPage.this, "Attendi... stiamo inoltrando la richiesta al server");
                    return;
                }
                boolean isWeather = AtmosfericoCheckBox.isChecked();
                boolean isSeismic = SismicoCheckBox.isChecked();
                boolean isTerrorist = TerroristoCheckBox.isChecked();
                if ((isWeather || isSeismic || isTerrorist)) {
                    FindRequest Req;
                    int Danger=spDanger.getSelectedItemPosition();
                    if (CapSwitch.isChecked()) {
                        try {
                            ArrayList<Integer> CAPList = MultiCapParser.ParseCap(etCap.getText().toString());
                            if (Danger == 0) {
                                Req = new FindRequestCap(calendar.getTime(), CAPList, isWeather, isTerrorist, isSeismic);
                            } else {
                                Req = new FindRequestCap(calendar.getTime(), CAPList, isWeather, isTerrorist, isSeismic, Danger);
                            }
                            RequestUploader FU = new RequestUploader(Req, hanl, ResearchPage.this);
                            FU.Upload();
                            LockConfirmBtn=true;
                        } catch (CapParseException e) {
                            AlertDialogOKBuilder.ShowAlert(ResearchPage.this, MultiCapParser.CAPNOTNUMBERSSTRING);
                        }
                    } else {
                        if (Danger == 0) {
                            Req = new FindRequestGlobal(calendar.getTime(), isWeather, isTerrorist, isSeismic);
                        } else {
                            Req = new FindRequestGlobal(calendar.getTime(), isWeather, isTerrorist, isSeismic, Danger);
                        }
                        RequestUploader FU = new RequestUploader(Req, hanl, ResearchPage.this);
                        FU.Upload();
                        LockConfirmBtn=true;
                    }
                } else {
                    AlertDialogOKBuilder.ShowAlert(ResearchPage.this, "Seleziona almeno un tipo di evento");
                }
            }
        });
    }



    //ANDROID STANDARD
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

/*
    private TextWatcher dateTimeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String dateInput= etDate.getText().toString().trim();
            String timeInput= etTime.getText().toString().trim();

            bSearch.setEnabled(!dateInput.isEmpty() && !timeInput.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

       etDate.addTextChangedListener(dateTimeTextWatcher);
       etTime.addTextChangedListener(dateTimeTextWatcher);
*/

}

