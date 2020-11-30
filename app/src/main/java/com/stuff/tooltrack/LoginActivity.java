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

    //initalize elements variables
    EditText editTextUserID;
    EditText editTextUserEmail;
    Button buttonLogin;
    Button buttonAutofillAdmin;
    Button buttonAutofillStudent;
    TextView textViewError;

    //initialize a user
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //find elements
        editTextUserID = findViewById(R.id.editTextUserID);
        editTextUserEmail = findViewById(R.id.editTextUserEmail);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonAutofillAdmin = findViewById(R.id.buttonAutofillAdmin);
        buttonAutofillStudent = findViewById(R.id.buttonAutofillStudent);
        textViewError = findViewById(R.id.textViewError);

        //create a new user object in the application
        user = new User(getApplicationContext());

        //set the initial values of the edit texts
        editTextUserID.setText(user.getID());
        editTextUserEmail.setText(user.getEmail());

    }


    public void onButtonLoginPressed(View view){

        //save the values of the edit text as credentials in the suer object
        user.setCredentials(editTextUserID.getText().toString(), editTextUserEmail.getText().toString());

        //request the errors of the credentials
        String error = user.getCredentialsError();

        if(error.isEmpty()) {

            //save the credentials to the user object
            user.saveCredentials();

            //saave the credentials to the database
            user.updateDatabaseCredentials();

            //launch the view activity
            Intent intent = new Intent(this, ViewActivity.class);
            startActivity(intent);
        }
        else{
            //display the credential errors
            textViewError.setText(error);
        }

    }


    public void onStudentButtonPressed(View v){
        //this button is for ease of access to fill in testing values to the log in faster
        editTextUserID.setText(getString(R.string.autofill_student_id));
        editTextUserEmail.setText(getString(R.string.autofill_student_email));
    }

    public void onAdminButtonPressed(View v){
        //this button is for ease of access to fill in testing values to the log in faster
        editTextUserID.setText(getString(R.string.admin_id));
        editTextUserEmail.setText(getString(R.string.admin_email));
    }

}