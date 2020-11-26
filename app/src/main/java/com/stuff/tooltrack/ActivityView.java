package com.stuff.tooltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class ActivityView extends AppCompatActivity {

    LinearLayout linearLayout;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refData = database.getReference();
    DatabaseReference refTools = refData.child("tools");
    DataSnapshot currentDatabase;

    HashMap<String, Tool> toolMap;
    HashMap<String, Rack> rackMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        toolMap = new HashMap<String, Tool>();
        rackMap = new HashMap<String, Rack>();

        linearLayout = findViewById(R.id.linearLayoutMain);

        //listen for tool changes
        ValueEventListener changeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentDatabase = dataSnapshot;
                updateRacks(dataSnapshot.child("racks"));
                updateTools(dataSnapshot.child("tools"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //do stuff like log errors or what not
            }
        };
        refData.addValueEventListener(changeListener);


    }


    public void updateRacks(DataSnapshot rackSnapshot){

        for(DataSnapshot rackData: rackSnapshot.getChildren()) {

            String key = rackData.getKey();
            Rack rack;

            if (rackMap.containsKey(key)) {
                rack = rackMap.get(key);
            } else {

                //create rack layout
                View rackLayout = LayoutInflater.from(this).inflate(R.layout.rack_view, null);

                //create a new rack
                rack = new Rack(rackData, rackLayout);
                rackMap.put(key, rack);

                //put rack layout inside main layout
                linearLayout.addView(rackLayout);
            }

            updateRackLayout(rack, rack.getRackView());

        }

    }

    public void updateRackLayout(Rack rack, View rackLayout){
        TextView textViewRackName = rackLayout.findViewById(R.id.textViewRackName);

        textViewRackName.setText(rack.getName());
    }


    //initialize and update the layouts
    public void updateTools(DataSnapshot toolSnapshot){

        for(DataSnapshot toolData: toolSnapshot.getChildren()){

            String key = toolData.getKey();
            Tool tool;

            if(toolMap.containsKey(key)) {
                tool = toolMap.get(key);
            }
            else{
                //create tool layout
                View toolLayout = LayoutInflater.from(this).inflate(R.layout.tool_view, null);

                //create a new tool
                tool = new Tool(toolData, toolLayout);
                toolMap.put(key, tool);

                //put tool layout inside the rack layout
                LinearLayout rackLinearLayout = rackMap.get(tool.getRack()).getRackView().findViewById(R.id.linearLayoutRack);
                rackLinearLayout.addView(toolLayout);
            }

            updateToolLayout(tool, tool.getToolView());


        }

    }

    public void updateToolLayout(Tool tool, View toolLayout){
        TextView textViewToolName = toolLayout.findViewById(R.id.textViewToolName);;
        TextView textViewToolStatus = toolLayout.findViewById(R.id.textViewToolStatus);
        TextView textViewToolLocation = toolLayout.findViewById(R.id.textViewToolLocation);
        ImageView imageViewToolStatus = toolLayout.findViewById(R.id.imageViewToolStatus);
        FloatingActionButton buttonToolEdit = toolLayout.findViewById(R.id.buttonToolEdit);

        textViewToolName.setText(tool.getName());

        textViewToolStatus.setText("Status: "+(tool.getAvailable()?"available": "not available"));
        imageViewToolStatus.setImageResource(tool.getAvailable()? android.R.drawable.presence_online: android.R.drawable.presence_offline);

        textViewToolLocation.setText("Location: "+tool.getRack());

        buttonToolEdit.setTag(tool.getKey());
    }


    public void onToolEditButtonPressed(View v){

        Tool tool = toolMap.get(v.getTag());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = LayoutInflater.from(this).inflate(R.layout.tool_edit_popup, null);

        EditText editTextToolEditName = popupView.findViewById(R.id.editTextToolEditName);

        editTextToolEditName.setText(tool.getName());


        popupView.setBackgroundResource(R.drawable.layout_bg_rounded);
        popupView.setClipToOutline(true);

        builder.setView(popupView);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


    }




}