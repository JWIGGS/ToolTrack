package com.stuff.tooltrack;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;


/*
The DatabaseView class is a class that holds both a reference to a specific location in the firebase
database as well as a specific view that it will update whenever new data is received.

 */
public abstract class DatabaseView {

    private View view;
    private String key;
    private DatabaseReference ref;

    public DatabaseView(DatabaseReference refFabLab, DataSnapshot snap, View v, String child){
        this.key = snap.getKey();
        this.view = v;
        this.ref = refFabLab.child(child).child(key);

        updateData(snap);
    }


    public View getView() {
        return view;
    }

    public DatabaseReference getRef(){
        return ref;
    }

    public String getKey(){
        return key;
    }

    public void update(DataSnapshot data, User user){
        /*
        This function should be called whenever new data is received at this reference and it will
        update the data as well as the view according to the functions that will be implemented by
        children classes.
         */

        updateData(data);
        updateView(user);
    };

    protected abstract void updateData(DataSnapshot snap);
    protected abstract void updateView(User user);

    public static AlertDialog displayAlertView(Context context, View v){
        /*
        This function creates a popup alert that when provided with a view will show respectively
        and return the dialog that has been created so that it can be cancelled in the future.
         */

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //makes the dialog nice and pretty with rounded edges
        v.setBackgroundResource(R.drawable.layout_bg_rounded);
        v.setClipToOutline(true);

        builder.setView(v);
        AlertDialog dialog = builder.create();
        dialog.show();

        //helps with the edge rounding
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    public static String getTimePretty(String timestamp){

        //a nice helper function to format timestamps into pretty dates
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Long.parseLong(timestamp));
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMM, HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        return dateFormat.format(cal.getTime());
    }

}
