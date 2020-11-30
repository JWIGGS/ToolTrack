package com.stuff.tooltrack;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Tool extends DatabaseView{

    private boolean available;
    private String name;
    private String rack;
    private String time;
    private String user;
    private String username;
    private AlertDialog pokeDialog;


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
        username = snap.child("username").getValue().toString();
    }

    @Override
    protected void updateView(User user){
        View v = getView();

        TextView textViewToolName = v.findViewById(R.id.textViewToolName);
        TextView textViewToolUser = v.findViewById(R.id.textViewToolUser);
        TextView textViewToolTime = v.findViewById(R.id.textViewToolTime);
        ImageView imageViewToolStatus = v.findViewById(R.id.imageViewToolStatus);
        FloatingActionButton buttonToolEdit = v.findViewById(R.id.buttonToolEdit);
        FloatingActionButton buttonToolPoke = v.findViewById(R.id.buttonToolPoke);

        textViewToolName.setText(getName());

        imageViewToolStatus.setImageResource(getAvailable()? android.R.drawable.presence_online: android.R.drawable.presence_offline);

        boolean isBorrowed = !getUser().isEmpty();
        boolean youBorrowed = getUser().equals(user.getID());

        textViewToolUser.setVisibility(isBorrowed? View.VISIBLE: View.GONE);
        textViewToolTime.setVisibility(isBorrowed? View.VISIBLE: View.GONE);

        if(isBorrowed) {
            textViewToolUser.setText("User: " + getUserName());
            textViewToolTime.setText("Borrowed: " + getTimePretty(getTime()));
        }

        textViewToolName.setTypeface(null,isBorrowed && youBorrowed? Typeface.BOLD: Typeface.NORMAL);
        buttonToolPoke.setVisibility(isBorrowed && !youBorrowed? View.VISIBLE: View.GONE);
        buttonToolPoke.setTag(getKey());

        buttonToolEdit.setVisibility(user.isAdmin()? View.VISIBLE: View.GONE);
        buttonToolEdit.setTag(getKey());
    }


    public void displayEditPopup(Context context){
        View popupView = LayoutInflater.from(context).inflate(R.layout.tool_edit_popup, null);

        EditText editTextToolEditName = popupView.findViewById(R.id.editTextToolEditName);

        editTextToolEditName.setText(name);

        displayAlertView(context, popupView);

    }

    public void displayPokePopup(Context context){
        View popupView = LayoutInflater.from(context).inflate(R.layout.poke_edit_popup, null);

        TextView textViewPokeHint = popupView.findViewById(R.id.textViewPokeHint);
        EditText editTextPokeMessage = popupView.findViewById(R.id.editTextPokeMessage);
        Button buttonPokeSend = popupView.findViewById(R.id.buttonPokeSend);

        buttonPokeSend.setTag(getKey());
        pokeDialog = displayAlertView(context, popupView);



    }

    public void closePokePopup(){
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

    public static String getTimePretty(String timestamp){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(timestamp));
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMM, HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        return dateFormat.format(cal.getTime());
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
        getRef().child("user").setValue(user);

        this.username = username;
        getRef().child("username").setValue(username);
    }

    public String getUserName() {
        return username;
    }




}
