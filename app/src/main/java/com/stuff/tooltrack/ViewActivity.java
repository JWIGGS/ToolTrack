package com.stuff.tooltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ViewActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    TextView textViewInventoryAmount;
    TextView textViewPokeAmount;
    FloatingActionButton buttonHistory;
    FloatingActionButton buttonPokes;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference refData = database.getReference();
    DatabaseReference refFabLab = refData.child("fablab");


    HashMap<String, Tool> toolMap;
    HashMap<String, Rack> rackMap;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        //initialize variables
        toolMap = new HashMap<String, Tool>();
        rackMap = new HashMap<String, Rack>();

        //find elements
        linearLayout = findViewById(R.id.linearLayoutMain);
        textViewInventoryAmount = findViewById(R.id.textViewInventoryAmount);
        textViewPokeAmount = findViewById(R.id.textViewPokeAmount);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonPokes = findViewById(R.id.buttonPokes);

        //create a new user
        user = new User(getApplicationContext());
        user.setTextViewInventory(textViewInventoryAmount);
        user.setPokeViews(textViewPokeAmount, buttonPokes);

        //invalid credentials
        if(!user.hasValidCredentials()){
            finish();
        }

        //hide history button if you are not admin
        buttonHistory.setVisibility(user.isAdmin()? View.VISIBLE: View.GONE);


        //listen for fab lab changes
        ValueEventListener fablabChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //update values
                updateRacks(dataSnapshot.child("racks"));
                updateTools(dataSnapshot.child("tools"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //do stuff like log errors or what not
            }
        };
        refFabLab.addValueEventListener(fablabChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //lock all the racks
        for(Rack rack: rackMap.values()){
            rack.lock(user);
        }

    }

    //kill the activity and basically flush this one so that current values arent cached
    // (or something like that)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    //initialize and update the rack layouts
    public void updateRacks(DataSnapshot rackSnapshot){

        //for each rack
        for(DataSnapshot rackData: rackSnapshot.getChildren()) {

            String key = rackData.getKey();
            Rack rack;
            if (rackMap.containsKey(key)) {
                //if the rack exists then update it
                rack = rackMap.get(key);
            }
            else {
                //rack doesn't exist so create a new rack
                rack = new Rack(refFabLab, rackData, LayoutInflater.from(this).inflate(R.layout.rack_view, null), "racks");
                rackMap.put(key, rack);

                //put rack layout inside main layout
                linearLayout.addView(rack.getView());
            }

            rack.update(rackData, user);

        }

    }

    //initialize and update the tools layouts
    public void updateTools(DataSnapshot toolSnapshot){

        //for every tool
        for(DataSnapshot toolData: toolSnapshot.getChildren()){

            String key = toolData.getKey();
            Tool tool;

            if(toolMap.containsKey(key)) {
                //tool exists so update it
                tool = toolMap.get(key);
            }
            else{
                //tool doesnt exist so create a new tool
                tool = new Tool(refFabLab, toolData, LayoutInflater.from(this).inflate(R.layout.tool_view, null), "tools");
                toolMap.put(key, tool);

                //put tool layout inside the rack layout
                rackMap.get(tool.getRack()).addTool(tool);

            }

            tool.update(toolData, user);


        }

    }



    public void onToolEditButtonPressed(View v){
        toolMap.get(v.getTag()).displayEditPopup(this, rackMap);
    }

    public void onToolEditSavePressed(View v){
        toolMap.get(v.getTag()).closeEditPopup(v);
    }

    public void onToolPokeButtonPressed(View v){
        toolMap.get(v.getTag()).displayPokePopup(this);
    }

    public void onPokeSendButtonPressed(View v){
        toolMap.get(v.getTag()).closePokePopup(v, user, toolMap.get(v.getTag()));
    }

    public void onRackLockButtonPressed(View v){
        rackMap.get(v.getTag()).toggleLocked(user);
    }

    public void onRackEditButtonPressed(View v){
        rackMap.get(v.getTag()).displayEditPopup(this);
    }

    public void onRackEditSavePressed(View v){
        rackMap.get(v.getTag()).closeEditPopup(v);
    }

    public void onInventoryButtonPressed(View v){
        user.displayInventoryPopup(this, toolMap, rackMap);
    }

    public void onPokeButtonPressed(View v){
        user.displayPokePopup(this);
    }

    public void onHistoryButtonPressed(View v){
        //launch new history activity
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }





}