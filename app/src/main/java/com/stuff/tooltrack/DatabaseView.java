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

    public DatabaseView(DatabaseReference refFabLab, DataSnapshot snap, View v, String child){
        this.key = snap.getKey();
        this.view = v;
        this.ref = refFabLab.child(child).child(key);

        updateData(snap);
    }


    public View getView() {
        return view;
    }

    public void update(DataSnapshot data, User user){
        updateData(data);
        updateView(user);
    };

    protected abstract void updateData(DataSnapshot snap);
    protected abstract void updateView(User user);

    public String getKey(){
        return key;
    }

    public DatabaseReference getRef(){
        return ref;
    }

    public static AlertDialog displayAlertView(Context context, View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        v.setBackgroundResource(R.drawable.layout_bg_rounded);
        v.setClipToOutline(true);

        builder.setView(v);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;

    }

}
