package com.stuff.tooltrack;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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


    public Rack(DatabaseReference refFabLab, DataSnapshot snap, View view, String child){

        super(refFabLab, snap, view, child);

    }


    @Override
    protected void updateData(DataSnapshot snap) {
        name = snap.child("name").getValue().toString();
        unlocked = snap.child("unlocked").getValue().toString();
    }

    @Override
    protected void updateView(User user){

        View v = getView();

        TextView textViewRackName = v.findViewById(R.id.textViewRackName);
        FloatingActionButton buttonRackEdit = v.findViewById(R.id.buttonRackEdit);
        FloatingActionButton buttonRackLock = v.findViewById(R.id.buttonRackLock);

        textViewRackName.setText(getName());

        buttonRackEdit.setVisibility(user.isAdmin()? View.VISIBLE: View.GONE);
        buttonRackEdit.setTag(getKey());

        buttonRackLock.setImageResource(isLocked()? android.R.drawable.ic_secure: android.R.drawable.ic_partial_secure);
        buttonRackLock.setTag(getKey());

        String userRack = user.getRack();
        buttonRackLock.setVisibility(userRack.equals(getKey()) || userRack.isEmpty()? View.VISIBLE: View.GONE);

    }

    public void addTool(Tool tool){

        LinearLayout rackLinearLayout = getView().findViewById(R.id.linearLayoutRack);
        rackLinearLayout.addView(tool.getView());

        toolMap.put(tool.getKey(), tool);
        updateCurrentAvailability(avail);
    }

    private void updateCurrentAvailability(HashMap<String, Boolean> map){
        if(!toolMap.isEmpty()) {
            for (Tool tool : toolMap.values()) {
                map.put(tool.getKey(), tool.getAvailable());
            }

        }
    }


    public void displayEditPopup(Context context){

        View popupView = LayoutInflater.from(context).inflate(R.layout.rack_edit_popup, null);

        EditText editTextRackEditName = popupView.findViewById(R.id.editTextRackEditName);

        editTextRackEditName.setText(name);

        displayAlertView(context, popupView);

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLocked(){
        return unlocked.isEmpty();
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
        getRef().child("unlocked").setValue(user.getID());
        user.setRack(getKey());
        updateCurrentAvailability(availPrev);
    }

    public void lock(User user){

        updateCurrentAvailability(avail);

        String timestamp = String.valueOf(System.currentTimeMillis());


        if(unlocked.equals(user.getID())){
            getRef().child("unlocked").setValue("");
            user.setRack("");

            //update history log
            HashMap<String, Object> historyChanges = new HashMap<String, Object>();

            for(Tool tool: toolMap.values()){
                String key = tool.getKey();
                boolean toolAvail = avail.get(key);
                boolean toolAvailPrev = availPrev.get(key);

                if(toolAvail != toolAvailPrev){
                    historyChanges.put(tool.getKey(), toolAvail? "return": "borrow");

                    if(toolAvail){
                        user.returnTool(tool, timestamp);
                    }
                    else{
                        user.borrowTool(tool, timestamp);
                    }


                }


            }

            //push history changes
            if(!historyChanges.isEmpty()){

                HashMap<String, Object> historyEntry = new HashMap<String, Object>();

                historyEntry.put("user", user.getID());
                historyEntry.put("tools", historyChanges);

                getRef().getRoot().child("history").child(timestamp).setValue(historyEntry);
            }




        }
    }




}
