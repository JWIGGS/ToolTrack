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

    EditText editTextUserID;
    EditText editTextUserEmail;
    Button buttonLogin;
    Button buttonAutofillAdmin;
    Button buttonAutofillStudent;
    TextView textViewError;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUserID = findViewById(R.id.editTextUserID);
        editTextUserEmail = findViewById(R.id.editTextUserEmail);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonAutofillAdmin = findViewById(R.id.buttonAutofillAdmin);
        buttonAutofillStudent = findViewById(R.id.buttonAutofillStudent);
        textViewError = findViewById(R.id.textViewError);

        Context context = getApplicationContext();

        user = new User(context);

        editTextUserID.setText(user.getID());
        editTextUserEmail.setText(user.getEmail());

    }


    public void onButtonLoginPressed(View view){
        user.setCredentials(editTextUserID.getText().toString(), editTextUserEmail.getText().toString());

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


    public void onStudentButtonPressed(View v){
        editTextUserID.setText(getString(R.string.autofill_student_id));
        editTextUserEmail.setText(getString(R.string.autofill_student_email));
    }

    public void onAdminButtonPressed(View v){
        editTextUserID.setText(getString(R.string.admin_id));
        editTextUserEmail.setText(getString(R.string.admin_email));
    }

}