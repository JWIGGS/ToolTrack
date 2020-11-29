package com.stuff.tooltrack;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.FirebaseDatabase;

public class User {

    private String id;
    private String email;
    private String name;
    private String rack = "";

    private static Context context;
    private static SharedPreferences sharedPref;

    public User(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences("com.stuff.tooltrack", context.MODE_PRIVATE);

        setID(getSavedID());
        setEmail(getSavedEmail());
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

    public boolean checkValidCredentials(){
        return !getCredentialsError().isEmpty();
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

        return email.substring(0, email.length()-defaultEmailLength);
    }

    public String getRack(){
        return this.rack;
    }

    public void setRack(String rack){
        this.rack = rack;
    }

}
