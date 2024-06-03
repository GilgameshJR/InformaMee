package com.pc.informamee.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.pc.informamee.R;
import com.pc.informamee.common.events.Event;
import com.pc.informamee.common.requests.FindRequest;
import com.pc.informamee.common.requests.FindRequestCurrent;
import com.pc.informamee.common.requests.RequestNotDoneException;

import java.util.ArrayList;

import static com.pc.informamee.mobile.RequestUploader.REQUESTNOTDONESTRING;
import static com.pc.informamee.mobile.RequestUploader.UPLOAD_OK;

public class AlertPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ListView listView;
    TextView TextViewResultStatus;
    Handler hanl;
    int CurrCap;
    ImageView refr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_page);
        listView = findViewById(R.id.listViewResults);
        TextViewResultStatus= findViewById(R.id.textViewResultStatus);
        refr= findViewById(R.id.imageViewRefresh);
        TextViewResultStatus.setText(R.string.Loading);
        listView.setVisibility(View.GONE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Allerte");

        hanl=new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message inputMessage) {
                int State=inputMessage.what;
                if (State==UPLOAD_OK) {
                    FindRequest Result=(FindRequest)inputMessage.obj;
                    try {
                        ArrayList<Event> ToShow=Result.getResult();
                        if (ToShow.isEmpty()){
                            TextViewResultStatus.setText((CharSequence)("Nessuna allerta al CAP "+CurrCap+" nelle prossime 24h"));
                        }
                        else {
                            MultiAdapter adapter = new MultiAdapter(AlertPage.this, ToShow);
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent InvolvedCapIntent=new Intent(AlertPage.this, ShowInvolvedCap.class);
                                    Event SelectedEvent=(Event)listView.getItemAtPosition(position);
                                    InvolvedCapIntent.putExtra("involvedcap", SelectedEvent.getInvolvedCap());
                                    startActivity(InvolvedCapIntent);
                                }
                            });
                            TextViewResultStatus.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                        }
                    } catch (RequestNotDoneException e) {
                        AlertDialogOKBuilder.ShowAlert(AlertPage.this, REQUESTNOTDONESTRING);
                        TextViewResultStatus.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        TextViewResultStatus.setText(R.string.Error);
                    }
                }
                else {
                    RequestUploader.ShowErrorMessage(AlertPage.this, State);
                    TextViewResultStatus.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                    TextViewResultStatus.setText(R.string.Error);
                }
            }

        };

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        CurrCap=getIntent().getIntExtra("cap", -1);
        FindRequestCurrent CurrFindRequest=new FindRequestCurrent(CurrCap);
        RequestUploader FU=new RequestUploader(CurrFindRequest, hanl, AlertPage.this);
        FU.Upload();

        refr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CurrCap=getIntent().getIntExtra("cap", -1);
                FindRequestCurrent CurrFindRequest=new FindRequestCurrent(CurrCap);
                RequestUploader FU=new RequestUploader(CurrFindRequest, hanl, AlertPage.this);
                FU.Upload();
            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.alert_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_changeCap) {
            // Handle the camera action
            SharedPreferences AppPreferences=AlertPage.this.getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor Edited=AppPreferences.edit();
            Edited.remove("cap");
            Edited.apply();
            startActivity(new Intent(AlertPage.this, MainActivity.class));
        } else if (id == R.id.nav_research) {
            startActivity(new Intent(AlertPage.this, ResearchPage.class));

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}


//TODO: mappa