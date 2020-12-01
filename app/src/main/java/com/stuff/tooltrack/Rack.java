package com.stuff.tooltrack;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rack extends DatabaseView{

    private String name;
    private String unlocked;

    private HashMap<String, Tool> toolMap = new HashMap<String, Tool>();
    private HashMap<String, Boolean> availPrev = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> avail = new HashMap<String, Boolean>();

    private AlertDialog rackEditPopup;

    public Rack(DatabaseReference refFabLab, DataSnapshot snap, View v, String child){
        super(refFabLab, snap, v, child);
    }


    @Override
    protected void updateData(DataSnapshot snap) {
        //read the data from the snapshot and save it to out object variables
        name = snap.child("name").getValue().toString();
        unlocked = snap.child("unlocked").getValue().toString();
    }

    @Override
    protected void updateView(User user){

        //get the view from out DatabaseView
        View v = getView();

        //find the elements in our layout
        TextView textViewRackName = v.findViewById(R.id.textViewRackName);
        FloatingActionButton buttonRackEdit = v.findViewById(R.id.buttonRackEdit);
        FloatingActionButton buttonRackLock = v.findViewById(R.id.buttonRackLock);

        Log.i("UPDATE VIEW CHECK", getName());

        //set the name of the rack
        textViewRackName.setText(getName());

        //set the visibility of the rack edit button depending on whether the user is an admin
        buttonRackEdit.setVisibility(user.isAdmin()? View.VISIBLE: View.GONE);

        //give the rack edit button a tag so that we can figure out which edit button was pressed later on
        buttonRackEdit.setTag(getKey());

        /*
        determine whether the rack lock button is clickable
        the rack button is clickable if
            1. nobody has opened the rack
            OR
            2. you are the one who has opened the rack

         this is to prevent you from locking and unlocking racks that another user has opened
         */
        String userRack = user.getRack();
        boolean clickable = userRack.equals(getKey()) || getLockedUser().equals(userRack);
        buttonRackLock.setClickable(clickable);
        buttonRackLock.setAlpha(clickable? 1: .25f);

        //change the visuals of our rack lock button
        buttonRackLock.setImageResource(isLocked()? android.R.drawable.ic_secure: android.R.drawable.ic_partial_secure);


        int color = R.color.bright_blue;
        if(userRack.equals(getKey())){
            color = R.color.orange;
        }
        else if(!getLockedUser().equals(userRack)){
            color = R.color.gray;
        }

        buttonRackLock.setBackgroundTintList(buttonRackLock.getContext().getColorStateList(color));

        //give the rack lock button a tag so that we can figure out which lock button was pressed later on
        buttonRackLock.setTag(getKey());
    }

    public void addTool(Tool tool){

        //get the linear layout of the rack
        LinearLayout rackLinearLayout = getView().findViewById(R.id.linearLayoutRack);

        //add the tool to the rack
        rackLinearLayout.addView(tool.getView());

        //store a reference to the tool so that we can use it later on
        toolMap.put(tool.getKey(), tool);
    }

    private HashMap<String, Boolean> updateCurrentAvailability(){
        //this function updates avail or availPrev
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        //iterate through the tools on our rack
        for (Tool tool : toolMap.values()) {

            //put the availability of each tool into the map
            map.put(tool.getKey(), tool.getAvailable());
        }

        return map;

    }


    public void displayEditPopup(Context context){

        //create a new popup view for editing the rack
        View popupView = LayoutInflater.from(context).inflate(R.layout.rack_edit_popup, null);

        //find elements
        EditText editTextRackEditName = popupView.findViewById(R.id.editTextRackEditName);
        Button buttonRackEditSave = popupView.findViewById(R.id.buttonRackEditSave);

        //set the name of the rack
        editTextRackEditName.setText(name);

        //set the tag of the save button so that we can use it later
        buttonRackEditSave.setTag(getKey());

        //display the popup and save a reference to it so we can cancel it later
        rackEditPopup = displayAlertView(context, popupView);

    }

    public void closeEditPopup(View v){

        //get the parent view of the save button
        View parentView = (View) v.getParent();

        //find elements
        EditText editTextRackEditName = parentView.findViewById(R.id.editTextRackEditName);

        //set the rack value to the value of the edit text
        setName(editTextRackEditName.getText().toString());

        //close the popup
        rackEditPopup.cancel();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        getRef().child("name").setValue(name);
    }

    public boolean isLocked(){
        return unlocked.isEmpty();
    }

    public String getLockedUser(){
        return unlocked;
    }

    public void toggleLocked(User user){
        if(isLocked()){
            unlock(user);
        }
        else{
            lock(user);
        }
    }


    public void unlock(User user){
        //set the value of the rack unlock to the current user who unlocked it
        getRef().child("unlocked").setValue(user.getID());

        //set the rack of the user that it has opened this rack
        user.setRack(getKey());

        //store the current availability of the tools so that we can use it later
        availPrev = updateCurrentAvailability();
    }

    public void lock(User user){

        //if the user locking it is the same user who unlocked it
        if(unlocked.equals(user.getID())){

            //get the current availability of the tools
            avail = updateCurrentAvailability();

            //grab the time
            String timestamp = String.valueOf(System.currentTimeMillis());

            //reset the rack to be locked
            getRef().child("unlocked").setValue("");

            //reset the user rack to not be opening anything
            user.setRack("");

            //update history log
            HashMap<String, Object> historyChanges = new HashMap<String, Object>();

            //iterate through the tools in this rack
            for(Tool tool: toolMap.values()){

                //get the key of the tool
                String key = tool.getKey();

                //get the present and past availability of the tool
                boolean toolAvail = avail.get(key);
                boolean toolAvailPrev = availPrev.get(key);

                //compare the availability and check for a difference
                if(toolAvail != toolAvailPrev){

                    //if the tool is now available, that means the user just put it back
                    if(toolAvail){

                        //make the user return the current tool
                        user.returnTool(tool, timestamp);

                        //update the history
                        historyChanges.put(tool.getName(), "returned");
                    }
                    else{

                        //ask the user to borrow the tool
                        user.borrowTool(tool, timestamp);

                        //update the history
                        historyChanges.put(tool.getName(), "borrowed");
                    }
                }


            }

            //push history changes
            if(!historyChanges.isEmpty()){

                //we need a new map to store a bunch of variables to push
                HashMap<String, Object> historyEntry = new HashMap<String, Object>();

                //store history data
                historyEntry.put("user", user.getID());
                historyEntry.put("username", user.getName());
                historyEntry.put("tools", historyChanges);

                //push history data
                getRef().getRoot().child("history").child(timestamp).setValue(historyEntry);
            }




        }
    }




}
