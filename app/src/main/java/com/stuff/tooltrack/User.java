package com.stuff.tooltrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String id;
    private String email;
    private String name;
    private String rack = "";

    private static Context context;
    private static SharedPreferences sharedPref;
    private DatabaseReference refUser;
    private HashMap<String, String> inventory = new HashMap<String, String>();
    private HashMap<String, DataSnapshot> pokes = new HashMap<String, DataSnapshot>();
    private TextView textViewInventory;
    private TextView textViewPokes;
    private FloatingActionButton buttonPokes;


    public User(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences("com.stuff.tooltrack", context.MODE_PRIVATE);

        setID(getSavedID());
        setEmail(getSavedEmail());

        updateDatabaseReference();

        if(refUser!=null){
            //listen for user changes
            ValueEventListener userChangeListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    updateData(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //do stuff like log errors or what not
                }
            };
            refUser.addValueEventListener(userChangeListener);
        }
    }


    public String getCredentialsError(){

        int idValue = Integer.parseInt(id);

        String defaultEmail = context.getString(R.string.default_student_email);
        int defaultEmailLength = defaultEmail.length();

        if(isAdmin()) {
            return "";
        }
        else if(idValue <1000000 || idValue > 1099999){
            return context.getString(R.string.invalid_student_id);
        }
        else if(email.length()<defaultEmailLength+5 || !email.substring(email.length()-defaultEmailLength).equals(defaultEmail)){
            return context.getString(R.string.invalid_student_email);
        }

        return "";
    }

    public boolean hasValidCredentials(){
        return getCredentialsError().isEmpty();
    }



    public void saveCredentials(){
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("id", this.id);
        editor.putString("email", this.email);
        editor.apply();

    }

    private static String getSavedID(){
        return sharedPref.getString("id", "");
    }

    private static String getSavedEmail(){
        return sharedPref.getString("email", "");
    }

    public String getID(){
        return this.id;
    }

    public void setID(String id){
        this.id = id;

        updateDatabaseReference();
    }

    public String getEmail(){
        return this.email;
    }

    public String getName(){
        return this.name;
    }

    public void setEmail(String email){
        this.email = email;
        this.name = parseName();

        updateDatabaseReference();
    }

    public void setCredentials(String id, String email){
        setID(id);
        setEmail(email);
    }

    public boolean isAdmin(){
        return id.equals(context.getString(R.string.admin_id)) && email.equals(context.getString(R.string.admin_email));
    }

    private String parseName(){

        int defaultEmailLength = context.getString(R.string.default_student_email).length();
        String[] splitName = email.substring(0, email.length()-defaultEmailLength).split("[_]");
        String returnName = "";
        for(String word:splitName){
            if(!returnName.isEmpty()){
                returnName += " ";
            }
            returnName += word.substring(0,1).toUpperCase()+word.substring(1);
        }

        return returnName;
    }

    public String getRack(){
        return this.rack;
    }

    public void setRack(String rack){
        this.rack = rack;
    }

    public void updateDatabaseReference(){
        if(id!= null && email!= null && hasValidCredentials()){
            refUser = FirebaseDatabase.getInstance().getReference().child("users").child(id);
        }
    }

    public void updateDatabaseCredentials(){
        refUser.child("email").setValue(email);

    }

    public void borrowTool(Tool tool, String timestamp){

        tool.setTime(timestamp);
        tool.setUser(id, name);

        refUser.child("tools").child(tool.getKey()).setValue(timestamp);

    }

    public void returnTool(Tool tool, String timestamp){

        tool.setTime(timestamp);
        tool.setUser("", "");

        refUser.child("tools").child(tool.getKey()).removeValue();


        for(DataSnapshot pokeData: pokes.values()){
            if(pokeData.child("toolid").getValue().toString().equals(tool.getKey())){
                refUser.child("pokes").child(pokeData.getKey()).removeValue();
            }
        }


    }

    private void updateData(DataSnapshot snap){

        //update inventory
        inventory.clear();
        if(snap.child("tools").hasChildren()){
            for(DataSnapshot toolData: snap.child("tools").getChildren()) {
                inventory.put(toolData.getKey(), toolData.getValue().toString());
            }
        }

        if(textViewInventory!=null) {
            int borrowedItems = inventory.size();
            textViewInventory.setText(borrowedItems > 0 ? String.valueOf(borrowedItems) : "");
        }

        //update pokes
        pokes.clear();

        if(snap.child("pokes").hasChildren()){
            for(DataSnapshot pokeData: snap.child("pokes").getChildren()) {
                pokes.put(pokeData.getKey(), pokeData);
            }
        }

        if(textViewPokes!=null && buttonPokes!=null) {
            int pokeCount = pokes.size();

            textViewPokes.setVisibility(pokeCount>0? View.VISIBLE: View.GONE);
            buttonPokes.setVisibility(pokeCount>0? View.VISIBLE: View.GONE);

            textViewPokes.setText(pokeCount > 0 ? String.valueOf(pokeCount) : "");
        }
    }

    public void setTextViewInventory(TextView v){
        textViewInventory = v;
    }

    public void setPokeViews(TextView v, FloatingActionButton b){
        textViewPokes = v;
        buttonPokes = b;
    }




    public void displayInventoryPopup(Context context, HashMap<String, Tool> toolMap, HashMap<String, Rack> rackMap){

        View popupView = LayoutInflater.from(context).inflate(R.layout.inventory_view_popup, null);

        LinearLayout linearLayoutInventory = popupView.findViewById(R.id.linearLayoutInventory);
        TextView textViewInventoryStatus = popupView.findViewById(R.id.textViewInventoryStatus);

        if(inventory.isEmpty()) {
            textViewInventoryStatus.setText("You have not borrowed anything");
        }
        else{
            textViewInventoryStatus.setText("You have borrowed "+inventory.size()+" item"+(inventory.size()==1?"":"s")+".");

            for(Map.Entry<String, String> inventoryItem: inventory.entrySet()){

                Tool tool = toolMap.get(inventoryItem.getKey());
                View toolView = LayoutInflater.from(context).inflate(R.layout.inventory_tool_view, null);

                TextView textViewToolName = toolView.findViewById(R.id.textViewInventoryToolName);
                TextView textViewToolLocation = toolView.findViewById(R.id.textViewInventoryToolLocation);
                TextView textViewToolTime = toolView.findViewById(R.id.textViewInventoryToolTime);

                textViewToolName.setText(tool.getName());
                textViewToolLocation.setText("Location: "+rackMap.get(tool.getRack()).getName());
                textViewToolTime.setText("Time Borrowed: "+Tool.getTimePretty(tool.getTime()));

                linearLayoutInventory.addView(toolView);

            }

        }


        DatabaseView.displayAlertView(context, popupView);

    }

    public void displayPokePopup(Context context){
        View popupView = LayoutInflater.from(context).inflate(R.layout.poke_view_popup, null);

        LinearLayout linearLayoutPokes = popupView.findViewById(R.id.linearLayoutPokes);
        TextView textViewPokeStatus = popupView.findViewById(R.id.textViewPokeStatus);

        textViewPokeStatus.setText("You have been poked "+pokes.size()+" time"+(pokes.size()==1?"":"s")+". Returning the tool will remove your pokes.");


        for(DataSnapshot pokeData: pokes.values()){

            String message = pokeData.child("message").getValue().toString();
            String tool = pokeData.child("tool").getValue().toString();
            String username = pokeData.child("username").getValue().toString();
            String time = Tool.getTimePretty(pokeData.getKey());


            View pokeView = LayoutInflater.from(context).inflate(R.layout.poke_entry, null);

            TextView textViewPokeEntryTool = pokeView.findViewById(R.id.textViewPokeEntryTool);
            TextView textViewPokeEntrySender = pokeView.findViewById(R.id.textViewPokeEntrySender);
            TextView textViewPokeEntryContent = pokeView.findViewById(R.id.textViewPokeEntryContent);

            textViewPokeEntryTool.setText(tool);
            textViewPokeEntrySender.setText("From "+username+" on "+time);
            textViewPokeEntryContent.setText(message);

            linearLayoutPokes.addView(pokeView, 0);

        }



        DatabaseView.displayAlertView(context, popupView);
    }


    public void sendPoke(Tool tool, String message){
        HashMap<String, Object> pokeMessage = new HashMap<String, Object>();

        String timestamp = String.valueOf(System.currentTimeMillis());

        if(message.isEmpty()){
            message = "Please return this item as I would like to use it. Thank you.";
        }

        pokeMessage.put("message", message);
        pokeMessage.put("tool", tool.getName());
        pokeMessage.put("toolid", tool.getKey());
        pokeMessage.put("username", getName());

        refUser.getParent().child(tool.getUser()).child("pokes").child(timestamp).setValue(pokeMessage);


    }


}
