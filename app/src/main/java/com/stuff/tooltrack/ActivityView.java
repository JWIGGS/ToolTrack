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

public class ActivityView extends AppCompatActivity {

    LinearLayout linearLayout;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refTools = database.getReference().child("tools");
    DataSnapshot currentDatabase;

    private boolean initialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

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


    //update the layouts and initialize them if it doesnt have any
    public void updateLayout(){

        if(!initialized){
            initialized = true;

            for(DataSnapshot tool: currentDatabase.getChildren()){

                View addLayout = LayoutInflater.from(this).inflate(R.layout.item_view, null);

                TextView textViewName = addLayout.findViewById(R.id.textViewName);
                textViewName.setText(tool.child("name").getValue().toString());

                boolean available = tool.child("available").getValue().toString()=="true";
                TextView textViewStatus = addLayout.findViewById(R.id.textViewStatus);
                textViewStatus.setText("Status: "+(available?"available": "not available"));
                ImageView imageViewStatus = addLayout.findViewById(R.id.imageViewStatus);
                imageViewStatus.setColorFilter(available? R.color.teal_200: R.color.design_default_color_error);
                //the colors do not display properly

                TextView textViewLocation = addLayout.findViewById(R.id.textViewLocation);
                textViewLocation.setText("Location: "+tool.child("rack").getValue().toString());


                linearLayout.addView(addLayout);

            }
        }
        else{
            //ummm im tired so ill figure this out later
            //also i think you need to save the names of the children and the id's
            // accosiated with the layouts you just created so that you can propely
            // reference the layouts you want to edit later on
            //also i think you are going to need to do
            // for(){
            // if(the element exists){create element}
            // update element
            // }
        }


    }


}