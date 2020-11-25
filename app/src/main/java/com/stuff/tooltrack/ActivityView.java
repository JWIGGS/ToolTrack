package com.stuff.tooltrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ActivityView extends AppCompatActivity {

    LinearLayout linearLayout;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refTools = database.getReference().child("tools");
    DataSnapshot currentDatabase;

    HashMap<String, View> layoutMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        layoutMap = new HashMap<String, View>();

        linearLayout = findViewById(R.id.linearLayoutView);

        //add a listener that will fire every time the database changes
        ValueEventListener changeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentDatabase = dataSnapshot;
                updateLayout();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //do stuff like log errors or what not
            }
        };
        refTools.addValueEventListener(changeListener);


    }


    //initialize and update the layouts
    public void updateLayout(){

        for(DataSnapshot tool: currentDatabase.getChildren()){

            String key = tool.getKey();

            View editLayout;

            if(layoutMap.containsKey(key)) {
                editLayout = layoutMap.get(key);
            }
            else{
                editLayout = LayoutInflater.from(this).inflate(R.layout.item_view, null);
                layoutMap.put(tool.getKey(), editLayout);

                linearLayout.addView(editLayout);
            }

            TextView textViewName = editLayout.findViewById(R.id.textViewName);;
            TextView textViewStatus = editLayout.findViewById(R.id.textViewStatus);
            TextView textViewLocation = editLayout.findViewById(R.id.textViewLocation);
            ImageView imageViewStatus = editLayout.findViewById(R.id.imageViewStatus);


            textViewName.setText(tool.child("name").getValue().toString());
            //textViewName.setText(tool.getKey());

            boolean available = tool.child("available").getValue().toString()=="true";
            textViewStatus.setText("Status: "+(available?"available": "not available"));
            imageViewStatus.setColorFilter(available? R.color.teal_200: R.color.design_default_color_error);

            textViewLocation.setText("Location: "+tool.child("rack").getValue().toString());


        }



    }


}