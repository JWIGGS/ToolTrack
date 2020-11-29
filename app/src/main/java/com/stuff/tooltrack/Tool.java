package com.stuff.tooltrack;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Tool extends DatabaseView{

    private boolean available;
    private String name;
    private String rack;
    private String time;
    private String user;


    public Tool(DatabaseReference refFabLab, DataSnapshot snap, View view, String child){

        super(refFabLab, snap, view, child);

    }

    @Override
    protected void updateData(DataSnapshot snap) {
        available = snap.child("available").getValue().toString().equals("true");
        name = snap.child("name").getValue().toString();
        rack = snap.child("rack").getValue().toString();

        time = snap.child("time").getValue().toString();
        user = snap.child("user").getValue().toString();
    }

    @Override
    protected void updateView(User user){
        View v = getView();

        TextView textViewToolName = v.findViewById(R.id.textViewToolName);
        TextView textViewToolStatus = v.findViewById(R.id.textViewToolStatus);
        TextView textViewToolLocation = v.findViewById(R.id.textViewToolLocation);
        ImageView imageViewToolStatus = v.findViewById(R.id.imageViewToolStatus);
        FloatingActionButton buttonToolEdit = v.findViewById(R.id.buttonToolEdit);

        textViewToolName.setText(getName());

        textViewToolStatus.setText("Status: "+(getAvailable()?"available": "not available"));
        imageViewToolStatus.setImageResource(getAvailable()? android.R.drawable.presence_online: android.R.drawable.presence_offline);

        textViewToolLocation.setText("Location: "+getRack());

        buttonToolEdit.setVisibility(user.isAdmin()? View.VISIBLE: View.GONE);
        buttonToolEdit.setTag(getKey());
    }


    public void displayEditPopup(Context context){
        View popupView = LayoutInflater.from(context).inflate(R.layout.tool_edit_popup, null);

        EditText editTextToolEditName = popupView.findViewById(R.id.editTextToolEditName);

        editTextToolEditName.setText(name);

        displayAlertView(context, popupView);

    }


    public boolean getAvailable() {
        return available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        getRef().child("name").setValue(name);
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
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

    public void setUser(String user) {
        this.user = user;
        getRef().child("user").setValue(user);
    }




}
