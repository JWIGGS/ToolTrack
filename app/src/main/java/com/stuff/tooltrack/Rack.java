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
    private long unlocked;


    public Rack(DatabaseReference refData, DataSnapshot snap, View view, String child){

        super(refData, snap, view, child);

        name = snap.child("name").getValue().toString();
        unlocked = (long) snap.child("unlocked").getValue();

        /* this code doesnt work. we somehow need to decode the 0: tool0, 1: tool1 from firebase into our list here
        if(rack.child("tools").hasChildren()) {
            for (DataSnapshot itemData : rack.child("tools").getChildren()) {
                tools.add(itemData.getValue().toString());
            }
        }
        */
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUnlocked() {
        return unlocked;
    }

    public void updateView(boolean admin){

        View v = getView();

        TextView textViewRackName = v.findViewById(R.id.textViewRackName);
        FloatingActionButton buttonRackEdit = v.findViewById(R.id.buttonRackEdit);

        textViewRackName.setText(getName());

        buttonRackEdit.setVisibility(admin? View.VISIBLE: View.GONE);
        buttonRackEdit.setTag(getKey());

    }


    public void displayEditPopup(Context context){

        View popupView = LayoutInflater.from(context).inflate(R.layout.rack_edit_popup, null);

        EditText editTextRackEditName = popupView.findViewById(R.id.editTextRackEditName);

        editTextRackEditName.setText(name);

        displayAlertView(context, popupView);

    }


}
