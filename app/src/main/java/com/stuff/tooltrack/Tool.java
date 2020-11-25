package com.stuff.tooltrack;

import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Tool {

    private View toolView;
    private String key;

    private Boolean available;
    private String name;
    private String rack;
    private Integer time;
    private Integer user;

    public Tool(DataSnapshot tool, View view){
        available = (Boolean) tool.child("available").getValue();
        name = tool.child("name").getValue().toString();
        rack = tool.child("rack").getValue().toString();

        time = Integer.parseInt(tool.child("time").getValue().toString());
        user = Integer.parseInt(tool.child("user").getValue().toString());

        key = tool.getKey();
        toolView = view;
    }

    public Boolean getAvailable() {
        return available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public View getToolView() {
        return toolView;
    }




    public static void createNewTool(DatabaseReference refData, String key){

        Map<String, Object> newTool = new HashMap<>();
        newTool.put("available", true);
        newTool.put("name", "Default Tool");
        newTool.put("rack", "rack0");
        newTool.put("time", 0);
        newTool.put("user", 0);

        refData.child("tools").child(key).setValue(newTool);
    }



}
