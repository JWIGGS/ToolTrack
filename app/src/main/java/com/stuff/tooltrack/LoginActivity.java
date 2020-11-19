package com.stuff.tooltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    Integer studentID;
    String studentEmail;

    Context context;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextStudentID = findViewById(R.id.editTextStudentID);
        editTextStudentEmail = findViewById(R.id.editTextStudentEmail);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewError = findViewById(R.id.textViewError);

        context = getApplicationContext();
        sharedPref = context.getSharedPreferences("com.stuff.tooltrack.saves", context.MODE_PRIVATE);
        editor = sharedPref.edit();


        editTextStudentID.setText(Integer.toString(sharedPref.getInt("id", Integer.parseInt(getString(R.string.default_student_id)))));
        editTextStudentEmail.setText(sharedPref.getString("email", getString(R.string.default_student_email)));

    }


    public void onButtonLoginPressed(View view){

        String error = "";

        //TODO: check if id and email are valid

        studentID = Integer.parseInt(editTextStudentID.getText().toString());
        studentEmail = editTextStudentEmail.getText().toString();

        String defaultEmail = getString(R.string.default_student_email);
        Integer defaultEmailLength = defaultEmail.length();

        if(studentID<1000000 || studentID > 1099999){
            error = getString(R.string.invalid_student_id);
        }
        else if(studentEmail.length()<defaultEmailLength+5 || !studentEmail.substring(studentEmail.length()-defaultEmailLength).equals(defaultEmail)){
            error = getString(R.string.invalid_student_email);
        }


        if(error == "") {

            //save the id and email to shared preferences

            editor.putInt("id", studentID);
            editor.putString("email", studentEmail);
            editor.apply();

            //launch new activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);


        }
        else{
            //show the error
            textViewError.setText(error);
        }

    }

}