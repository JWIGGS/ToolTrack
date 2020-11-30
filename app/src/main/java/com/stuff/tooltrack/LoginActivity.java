package com.stuff.tooltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    EditText editTextStudentID;
    EditText editTextStudentEmail;
    Button buttonLogin;
    TextView textViewError;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextStudentID = findViewById(R.id.editTextStudentID);
        editTextStudentEmail = findViewById(R.id.editTextStudentEmail);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewError = findViewById(R.id.textViewError);

        Context context = getApplicationContext();

        user = new User(context);

        editTextStudentID.setText(user.getID());
        editTextStudentEmail.setText(user.getEmail());

    }


    public void onButtonLoginPressed(View view){
        user.setCredentials(editTextStudentID.getText().toString(), editTextStudentEmail.getText().toString());

        String error = user.getCredentialsError();

        if(error.isEmpty()) {

            user.saveCredentials();

            user.updateDatabaseCredentials();

            //launch new activity
            Intent intent = new Intent(this, ViewActivity.class);
            startActivity(intent);


        }
        else{
            //show the error
            textViewError.setText(error);
        }

    }

}