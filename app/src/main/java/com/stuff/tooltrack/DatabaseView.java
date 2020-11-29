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

public abstract class DatabaseView {

    private View view;
    private String key;
    private DatabaseReference ref;

    public DatabaseView(DatabaseReference refData, DataSnapshot snap, View v, String child){
        this.key = snap.getKey();
        this.view = v;
        this.ref = refData.child(child).child(key);
    }


    public View getView() {
        return view;
    }

    public abstract void updateView(boolean admin);

    public String getKey(){
        return key;
    }

    public DatabaseReference getRef(){
        return ref;
    }

    public void displayAlertView(Context context, View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        v.setBackgroundResource(R.drawable.layout_bg_rounded);
        v.setClipToOutline(true);

        builder.setView(v);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

}
