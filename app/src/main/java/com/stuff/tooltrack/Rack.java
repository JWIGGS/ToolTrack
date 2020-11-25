package com.stuff.tooltrack;

import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rack {

    private View rackView;
    private String key;

    private String name;
    private List<String> tools;
    private Integer unlocked;


    public Rack(DataSnapshot rack, View view){
        name = rack.child("name").getValue().toString();

        /* this code doesnt work. we somehow need to decode the 0: tool0, 1: tool1 from firebase into our list here
        if(rack.child("tools").hasChildren()) {
            for (DataSnapshot itemData : rack.child("tools").getChildren()) {
                tools.add(itemData.getValue().toString());
            }
        }
         */

        key = rack.getKey();
        rackView = view;
    }


    public View getRackView() {
        return rackView;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUnlocked() {
        return unlocked;
    }


}
