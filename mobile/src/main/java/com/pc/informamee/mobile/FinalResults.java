package com.pc.informamee.mobile;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.pc.informamee.R;
import com.pc.informamee.common.events.*;
import com.pc.informamee.common.requests.FindRequest;
import com.pc.informamee.common.requests.RequestNotDoneException;

import java.util.ArrayList;
import java.util.Date;

public class FinalResults extends AppCompatActivity {

    ListView listViewResults;

    public FinalResults() throws MalformedEventException {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_results);
        getSupportActionBar().setTitle(getString(R.string.SearchResult));
        listViewResults = findViewById(R.id.listViewResults);
        TextView textViewNoResults=findViewById(R.id.textViewResultStatus);

        ArrayList<Event> ToShow=(ArrayList<Event>)getIntent().getSerializableExtra("EventsResults");
        if (ToShow.isEmpty()){
            textViewNoResults.setText(R.string.NoResults);
            listViewResults.setVisibility(View.GONE);
            //textViewNoResults.setVisibility(View.VISIBLE);
        } else {
            textViewNoResults.setVisibility(View.GONE);
            listViewResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent InvolvedCapIntent = new Intent(FinalResults.this, ShowInvolvedCap.class);
                    Event SelectedEvent=(Event)listViewResults.getItemAtPosition(position);
                    InvolvedCapIntent.putExtra("involvedcap", SelectedEvent.getInvolvedCap());
                    startActivity(InvolvedCapIntent);
                }
            });
            MultiAdapter mAdapter = new MultiAdapter(FinalResults.this, ToShow);
            listViewResults.setAdapter(mAdapter);
        }
    }
}