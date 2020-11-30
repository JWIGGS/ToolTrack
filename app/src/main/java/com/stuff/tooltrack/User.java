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

    //elements to update
    private TextView textViewInventory;
    private TextView textViewPokes;
    private FloatingActionButton buttonPokes;


    public User(Context context) {

        //create a user with a specific context
        this.context = context;

        //initialize shared preferences
        sharedPref = context.getSharedPreferences("com.stuff.tooltrack", context.MODE_PRIVATE);

        //set the id and email based on saved values
        setID(getSavedID());
        setEmail(getSavedEmail());

        //create a database reference if one does not exist
        updateDatabaseReference();

        //the user might be null if the credentials are not valid
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
        //checks the current id and email values to make sure they are valid
        int idValue = Integer.parseInt(id);

        String defaultEmail = context.getString(R.string.default_student_email);
        int defaultEmailLength = defaultEmail.length();

        //admins have special rules, so we check first and quickly get that out of the way
        if(isAdmin()) {
            return "";
        }
        else if(idValue <1000000 || idValue > 1099999){
            return context.getString(R.string.invalid_student_id);
        }
        else if(email.length()<defaultEmailLength+5 || !email.substring(email.length()-defaultEmailLength).equals(defaultEmail)){
            return context.getString(R.string.invalid_student_email);
        }

        //no errors
        return "";
    }

    public boolean hasValidCredentials(){
        return getCredentialsError().isEmpty();
    }



    public void saveCredentials(){

        //save the id and email to storage
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
        //turns an email with the format firstname_middlename_anothername_last_name into a name.
        //assumes that your first name is first

        //split the name based on the delimiter "_"
        int defaultEmailLength = context.getString(R.string.default_student_email).length();
        String[] splitName = email.substring(0, email.length()-defaultEmailLength).split("[_]");
        String returnName = "";

        //for each word in the name
        for(String word:splitName){
            if(!returnName.isEmpty()){
                returnName += " ";
            }

            //capitalize the first letter
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

        //the reference is only set in the event that you have valid credentials
        if(id!= null && email!= null && hasValidCredentials()){
            refUser = FirebaseDatabase.getInstance().getReference().child("users").child(id);
        }
    }

    public void updateDatabaseCredentials(){
        refUser.child("email").setValue(email);
    }

    public void borrowTool(Tool tool, String timestamp){

        //sets the tool values
        tool.setTime(timestamp);
        tool.setUser(id, name);

        //sets the user value to say that you took a tool
        refUser.child("tools").child(tool.getKey()).setValue(timestamp);
    }

    public void returnTool(Tool tool, String timestamp){

        //reset the tool values
        tool.setTime(timestamp);
        tool.setUser("", "");

        //removes the tool from your user
        refUser.child("tools").child(tool.getKey()).removeValue();

        //iterate through your pokes
        for(DataSnapshot pokeData: pokes.values()){

            //remove the poke if the toolid is the same as the one you returned
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

        //set the value of the little number based on the inventory amount
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

        //set the value of the little number based on the poke amount
        //also hide the button if there are no pokes
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

        //create a popup view
        View popupView = LayoutInflater.from(context).inflate(R.layout.inventory_view_popup, null);

        //find elements
        LinearLayout linearLayoutInventory = popupView.findViewById(R.id.linearLayoutInventory);
        TextView textViewInventoryStatus = popupView.findViewById(R.id.textViewInventoryStatus);


        if(inventory.isEmpty()) {

            //empty inventrory text
            textViewInventoryStatus.setText("You have not borrowed anything");
        }
        else{

            //inventory text
            textViewInventoryStatus.setText("You have borrowed "+inventory.size()+" item"+(inventory.size()==1?"":"s")+".");

            //iterate through all items in inventory
            for(Map.Entry<String, String> inventoryItem: inventory.entrySet()){

                //get the current tool
                Tool tool = toolMap.get(inventoryItem.getKey());

                //create another view to show tool details
                View toolView = LayoutInflater.from(context).inflate(R.layout.inventory_tool_view, null);

                //find elements
                TextView textViewToolName = toolView.findViewById(R.id.textViewInventoryToolName);
                TextView textViewToolLocation = toolView.findViewById(R.id.textViewInventoryToolLocation);
                TextView textViewToolTime = toolView.findViewById(R.id.textViewInventoryToolTime);

                //set values
                textViewToolName.setText(tool.getName());
                textViewToolLocation.setText("Location: "+rackMap.get(tool.getRack()).getName());
                textViewToolTime.setText("Time Borrowed: "+DatabaseView.getTimePretty(tool.getTime()));

                //add tool to inventory layout
                linearLayoutInventory.addView(toolView);

            }

        }

        //display popup view
        DatabaseView.displayAlertView(context, popupView);

    }

    public void displayPokePopup(Context context){

        //create a new popup view
        View popupView = LayoutInflater.from(context).inflate(R.layout.poke_view_popup, null);

        //find elements
        LinearLayout linearLayoutPokes = popupView.findViewById(R.id.linearLayoutPokes);
        TextView textViewPokeStatus = popupView.findViewById(R.id.textViewPokeStatus);

        //set poke numbers
        textViewPokeStatus.setText("You have been poked "+pokes.size()+" time"+(pokes.size()==1?"":"s")+".");

        //for every poke entry
        for(DataSnapshot pokeData: pokes.values()){

            //get values of poke data
            String message = pokeData.child("message").getValue().toString();
            String tool = pokeData.child("tool").getValue().toString();
            String username = pokeData.child("username").getValue().toString();
            String time = DatabaseView.getTimePretty(pokeData.getKey());

            //create new view to display poke entry
            View pokeView = LayoutInflater.from(context).inflate(R.layout.poke_entry, null);

            //find elements
            TextView textViewPokeEntryTool = pokeView.findViewById(R.id.textViewPokeEntryTool);
            TextView textViewPokeEntrySender = pokeView.findViewById(R.id.textViewPokeEntrySender);
            TextView textViewPokeEntryContent = pokeView.findViewById(R.id.textViewPokeEntryContent);

            //set values
            textViewPokeEntryTool.setText(tool);
            textViewPokeEntrySender.setText("From "+username+" on "+time);
            textViewPokeEntryContent.setText(message);

            //add entry to the top of the layout
            linearLayoutPokes.addView(pokeView, 0);

        }

        //display poke view dialog
        DatabaseView.displayAlertView(context, popupView);
    }


    public void sendPoke(Tool tool, String message){

        //create a map to store poke values
        HashMap<String, Object> pokeMessage = new HashMap<String, Object>();

        //default meseage
        if(message.isEmpty()){
            message = "Please return this item as I would like to use it. Thank you.";
        }

        //put values into poke message
        pokeMessage.put("message", message);
        pokeMessage.put("tool", tool.getName());
        pokeMessage.put("toolid", tool.getKey());
        pokeMessage.put("username", getName());

        //push poke message onto whoever is using the tool
        String timestamp = String.valueOf(System.currentTimeMillis());
        refUser.getParent().child(tool.getUser()).child("pokes").child(timestamp).setValue(pokeMessage);


    }


}
