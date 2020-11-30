package com.stuff.tooltrack;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class Tool extends DatabaseView{

    private boolean available;
    private String name;
    private String rack;
    private String time;
    private String user;
    private String username;
    private int weight;

    private AlertDialog pokeDialog;
    private AlertDialog toolEditDialog;

    public Tool(DatabaseReference refFabLab, DataSnapshot snap, Context context){

        super(refFabLab, snap,LayoutInflater.from(context).inflate(R.layout.tool_view, null), "tools");

    }

    @Override
    protected void updateData(DataSnapshot snap) {

        //read the data from the snapshot and save it to the object variables
        available = snap.child("available").getValue().toString().equals("true");
        name = snap.child("name").getValue().toString();
        rack = snap.child("rack").getValue().toString();

        time = snap.child("time").getValue().toString();
        user = snap.child("user").getValue().toString();
        username = snap.child("username").getValue().toString();
        weight = Integer.parseInt(snap.child("weight").getValue().toString());
    }

    @Override
    protected void updateView(User user){

        //get the view from the DatabaseView
        View v = getView();

        //find elements
        TextView textViewToolName = v.findViewById(R.id.textViewToolName);
        TextView textViewToolUser = v.findViewById(R.id.textViewToolUser);
        TextView textViewToolTime = v.findViewById(R.id.textViewToolTime);
        ImageView imageViewToolStatus = v.findViewById(R.id.imageViewToolStatus);
        FloatingActionButton buttonToolEdit = v.findViewById(R.id.buttonToolEdit);
        FloatingActionButton buttonToolPoke = v.findViewById(R.id.buttonToolPoke);

        //set the title of the tool
        textViewToolName.setText(getName());

        //change the image based on the tools availability
        imageViewToolStatus.setImageResource(getAvailable()? android.R.drawable.presence_online: android.R.drawable.presence_offline);

        //check whether the tool is borrowed and if you borrowed the tool
        boolean isBorrowed = !getUser().isEmpty();
        boolean youBorrowed = getUser().equals(user.getID());

        //show the user and time details only if the tool is borrowed
        textViewToolUser.setVisibility(isBorrowed? View.VISIBLE: View.GONE);
        textViewToolTime.setVisibility(isBorrowed? View.VISIBLE: View.GONE);

        //updte the user and time details only if the tool is borrowed
        if(isBorrowed){
            textViewToolUser.setText("User: " + getUserName());
            textViewToolTime.setText("Borrowed: " + getTimePretty(getTime()));
        }

        //bold the name of the tool if you are the one who borrowed it
        textViewToolName.setTypeface(null, isBorrowed && youBorrowed? Typeface.BOLD: Typeface.NORMAL);

        //show the poke button if the tool is borrowed and you are not the one who borrowed it
        buttonToolPoke.setVisibility(isBorrowed && !youBorrowed? View.VISIBLE: View.GONE);

        //set the tool poke tag so that we know which button was pressed
        buttonToolPoke.setTag(getKey());

        //show the edit button if the user is an admin
        buttonToolEdit.setVisibility(user.isAdmin()? View.VISIBLE: View.GONE);

        //set the tool edit tag so we know which tool we are editing later on
        buttonToolEdit.setTag(getKey());
    }


    public void displayEditPopup(Context context, HashMap<String, Rack> rackMap){

        //create a popup view
        View popupView = LayoutInflater.from(context).inflate(R.layout.tool_edit_popup, null);

        //fine elements
        EditText editTextToolEditName = popupView.findViewById(R.id.editTextToolEditName);
        TextView textViewToolEditStatus = popupView.findViewById(R.id.textViewToolEditStatus);
        TextView textViewToolEditLocation = popupView.findViewById(R.id.textViewToolEditLocation);
        TextView textViewToolEditTime = popupView.findViewById(R.id.textViewToolEditTime);
        TextView textViewToolEditUser = popupView.findViewById(R.id.textViewToolEditUser);
        TextView textViewToolEditSliderTip = popupView.findViewById(R.id.textViewToolEditSliderTip);
        SeekBar seekBarToolEditWeight =  popupView.findViewById(R.id.seekBarToolEditWeight);
        Button buttonToolEditSave =  popupView.findViewById(R.id.buttonToolEditSave);

        //set values
        editTextToolEditName.setText(name);
        textViewToolEditStatus.setText("Status: "+(getAvailable()?"Available": "Not Available"));
        textViewToolEditLocation.setText("Location: "+rackMap.get(getRack()).getName());

        //check if the tool is borrowed
        boolean isBorrowed = !getUser().isEmpty();

        //show the user and time details only if the tool is borrowed
        textViewToolEditTime.setVisibility(isBorrowed? View.VISIBLE: View.GONE);
        textViewToolEditUser.setVisibility(isBorrowed? View.VISIBLE: View.GONE);

        //show the user and time details only if the tool is borrowed
        if(isBorrowed){
            textViewToolEditTime.setText(getTimePretty(getTime()));
            textViewToolEditUser.setText(getUserName()+" "+getUser());
        }

        //set the initial value of the seek bar and text view
        seekBarToolEditWeight.setProgress(getWeight());
        textViewToolEditSliderTip.setText("Weight Calibration: "+getWeight()+"g");

        //create a seek bar listener
        seekBarToolEditWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //update the value of the text view to display the current progress
                textViewToolEditSliderTip.setText("Weight Calibration: "+progress+"g");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //nobody cares
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //nobody cares
            }
        });

        //give the save button a tag so that we know which save button was pressed later on
        buttonToolEditSave.setTag(getKey());

        //display and save the dialog so that we can cancel it later on
        toolEditDialog = displayAlertView(context, popupView);

    }

    public void closeEditPopup(View v){

        //get the parent view of the save button
        View parentView = (View) v.getParent();

        //find elements
        EditText editTextToolEditName = parentView.findViewById(R.id.editTextToolEditName);
        SeekBar seekBarToolEditWeight = parentView.findViewById(R.id.seekBarToolEditWeight);

        //set the object variables to the values of the edit text and seek bar
        setName(editTextToolEditName.getText().toString());
        setWeight(seekBarToolEditWeight.getProgress());

        //close the dialog
        toolEditDialog.cancel();
    }

    public void displayPokePopup(Context context){

        //create a popup to poke users
        View popupView = LayoutInflater.from(context).inflate(R.layout.poke_edit_popup, null);

        //find elements
        TextView textViewPokeHint = popupView.findViewById(R.id.textViewPokeHint);
        EditText editTextPokeMessage = popupView.findViewById(R.id.editTextPokeMessage);
        Button buttonPokeSend = popupView.findViewById(R.id.buttonPokeSend);

        //set the hint value
        editTextPokeMessage.setText("Poke "+getUserName()+" regarding "+getName());

        //give the send button a tag so that we know which save button was pressed later on
        buttonPokeSend.setTag(getKey());

        //display and save the dialog so that we can cancel it later on
        pokeDialog = displayAlertView(context, popupView);

    }

    public void closePokePopup(View v, User user, Tool tool){

        //get the view of the send button
        View parentView = (View) v.getParent();

        //find elements
        EditText editTextPokeMessage = parentView.findViewById(R.id.editTextPokeMessage);

        //trigger a poke
        user.sendPoke(tool, editTextPokeMessage.getText().toString());

        //close the dialog
        pokeDialog.cancel();
    }


    public boolean getAvailable() {
        return available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

        //update the database
        getRef().child("name").setValue(name);
    }

    public int getWeight(){
        return weight;
    }

    public void setWeight(int weight){
        this.weight = weight;

        //update the database
        getRef().child("weight").setValue(weight);
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;

        //update the database
        getRef().child("rack").setValue(rack);
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
        getRef().child("time").setValue(time);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user, String username) {
        this.user = user;
        this.username = username;

        //update the database
        getRef().child("user").setValue(user);
        getRef().child("username").setValue(username);

    }

    public String getUserName() {
        return username;
    }




}
