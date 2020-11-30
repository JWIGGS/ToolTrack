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

    //initialize database variables
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refHistory = database.getReference().child("history");

    //initialize element variables
    LinearLayout linearLayoutHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //find elements
        linearLayoutHistory = findViewById(R.id.linearLayourHistory);

        //get history once
        ValueEventListener historySingleListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //update the history views
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

        //iterate through all entries of the history
        for(DataSnapshot entry: snap.getChildren()){

            //retrieve data from snapshot
            String timestamp = entry.getKey();
            String username = entry.child("username").getValue().toString();
            String user = entry.child("user").getValue().toString();

            //create a view for each entry
            View entryView = LayoutInflater.from(this).inflate(R.layout.history_entry, null);

            //find elements in the new entry view
            TextView textViewHistoryEntryUser = entryView.findViewById(R.id.textViewHistoryEntryUser);
            TextView textViewHistoryEntryTime = entryView.findViewById(R.id.textViewHistoryEntryTime);
            LinearLayout linearLayourHistoryStatus = entryView.findViewById(R.id.linearLayourHistoryStatus);

            //set the values of elements in the entry view
            textViewHistoryEntryUser.setText(username+" "+user);
            textViewHistoryEntryTime.setText(DatabaseView.getTimePretty(timestamp));

            //multiple tools can be logged within each entry, so we iterate through the tools as well
            for(DataSnapshot toolEntry: entry.child("tools").getChildren()) {

                //retrieve the current tool data
                String tool = toolEntry.getKey();
                String status = toolEntry.getValue().toString();

                //create a view for each tool
                View statusView = LayoutInflater.from(this).inflate(R.layout.history_tool_status, null);

                //find elements in the tool view
                TextView textViewHistoryToolStatus = statusView.findViewById(R.id.textViewHistoryToolStatus);

                //set the values of the elements in the tool view
                textViewHistoryToolStatus.setText(tool + " "+status);

                //add this new tool view to the top of the linear layout in the entry view
                linearLayourHistoryStatus.addView(statusView, 0);
            }

            //add this new entry view to the top of the linear layout in the activity
            linearLayoutHistory.addView(entryView, 0);



        }


    }
}