package com.pc.informamee.mobile;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pc.informamee.R;

import java.util.ArrayList;

public class ShowInvolvedCap extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_involved_cap);
        getSupportActionBar().setTitle("Cap coinvolti");
        ArrayList<Integer> InvolvedCap=getIntent().getIntegerArrayListExtra("involvedcap");
        assert InvolvedCap != null;
        ArrayAdapter CapAdapter= new ArrayAdapter(this, R.layout.cap_row, R.id.involvedCapTextView, InvolvedCap);
        ListView InvolvedCapList= findViewById(R.id.involvedCapListView);
        InvolvedCapList.setAdapter(CapAdapter);
    }
}