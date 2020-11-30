package com.stuff.tooltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HistoryActivity extends AppCompatActivity {

    LinearLayout linearLayoutHistory;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refHistory = database.getReference().child("history");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        linearLayoutHistory = findViewById(R.id.linearLayourHistory);

        //get history once
        ValueEventListener historySingleListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateHistoryView(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //do stuff like log errors or what not
            }
        };
        refHistory.addListenerForSingleValueEvent(historySingleListener);
    }

    public void updateHistoryView(DataSnapshot snap){

        for(DataSnapshot entry: snap.getChildren()){

            String timestamp = entry.getKey();
            String username = entry.child("username").getValue().toString();
            String user = entry.child("user").getValue().toString();

            View entryView = LayoutInflater.from(this).inflate(R.layout.history_entry, null);

            TextView textViewHistoryEntryUser = entryView.findViewById(R.id.textViewHistoryEntryUser);
            TextView textViewHistoryEntryTime = entryView.findViewById(R.id.textViewHistoryEntryTime);
            LinearLayout linearLayourHistoryStatus = entryView.findViewById(R.id.linearLayourHistoryStatus);

            textViewHistoryEntryUser.setText(username+" "+user);
            textViewHistoryEntryTime.setText(Tool.getTimePretty(timestamp));


            for(DataSnapshot toolEntry: entry.child("tools").getChildren()) {

                String tool = toolEntry.getKey();
                String status = toolEntry.getValue().toString();

                View statusView = LayoutInflater.from(this).inflate(R.layout.history_tool_status, null);

                TextView textViewHistoryToolStatus = statusView.findViewById(R.id.textViewHistoryToolStatus);

                textViewHistoryToolStatus.setText(tool + " "+status);


                linearLayourHistoryStatus.addView(statusView, 0);
            }

            linearLayoutHistory.addView(entryView, 0);







        }


    }
}