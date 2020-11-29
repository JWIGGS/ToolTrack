package com.stuff.tooltrack;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rack extends DatabaseView{

    private String name;
    private List<String> tools;
    private String unlocked;


    public Rack(DatabaseReference refData, DataSnapshot snap, View view, String child){

        super(refData, snap, view, child);

    }


    @Override
    protected void updateData(DataSnapshot snap) {
        name = snap.child("name").getValue().toString();
        unlocked = snap.child("unlocked").getValue().toString();

        /* this code doesnt work. we somehow need to decode the 0: tool0, 1: tool1 from firebase into our list here
        if(rack.child("tools").hasChildren()) {
            for (DataSnapshot itemData : rack.child("tools").getChildren()) {
                tools.add(itemData.getValue().toString());
            }
        }
        */

    }

    @Override
    protected void updateView(boolean admin){

        View v = getView();

        TextView textViewRackName = v.findViewById(R.id.textViewRackName);
        FloatingActionButton buttonRackEdit = v.findViewById(R.id.buttonRackEdit);
        FloatingActionButton buttonRackLock = v.findViewById(R.id.buttonRackLock);

        textViewRackName.setText(getName());

        buttonRackEdit.setVisibility(admin? View.VISIBLE: View.GONE);
        buttonRackEdit.setTag(getKey());

        buttonRackLock.setImageResource(isLocked()? android.R.drawable.ic_secure: android.R.drawable.ic_partial_secure);
        buttonRackLock.setTag(getKey());


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


    private void unlock(User user){
        getRef().child("unlocked").setValue(user.getID());
    }

    private void lock(User user){
        if(unlocked == user.getID()){
            getRef().child("unlocked").setValue("");
        }
    }




}
