package com.stuff.tooltrack;

import com.google.firebase.database.FirebaseDatabase;

public class User {

    public String id;
    public String email;


    public User(String id, String email) {
        this.id = id;
        this.email = email;
    }

}
