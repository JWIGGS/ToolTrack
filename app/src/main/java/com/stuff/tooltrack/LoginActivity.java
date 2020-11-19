package com.stuff.tooltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    EditText editTextStudentID;
    EditText editTextStudentEmail;
    Button buttonLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextStudentID = findViewById(R.id.editTextStudentID);
        editTextStudentEmail = findViewById(R.id.editTextStudentEmail);
        buttonLogin = findViewById(R.id.buttonLogin);

    }


    public void onButtonLoginPressed(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}