package com.stuff.tooltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("test/value");

    EditText editTextValue;
    Button buttonPush;
    TextView textViewDisplay;

    TextView textViewDisplay2;
    TextView textViewDisplay3;

    String studentID;
    String studentEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextValue = findViewById(R.id.editTextValue);
        buttonPush = findViewById(R.id.buttonPush);
        textViewDisplay = findViewById(R.id.textViewDisplay);

        textViewDisplay2 = findViewById(R.id.textViewDisplay2);
        textViewDisplay3 = findViewById(R.id.textViewDisplay3);


        ValueEventListener valueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textViewDisplay.setText("Value: "+dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //do stuff like log errors or what not
            }
        };
        myRef.addValueEventListener(valueListener);

        if(getIntent().getExtras() != null){
            studentID = getIntent().getStringExtra("id");
            studentEmail = getIntent().getStringExtra("email");

            textViewDisplay2.setText(studentID);
            textViewDisplay3.setText(studentEmail);
        }
        else{
            textViewDisplay2.setText("error");
            textViewDisplay3.setText("error");
            //onBackPressed();
        }


    }



    public void onButtonPushPressed(View view){
        myRef.setValue(editTextValue.getText().toString());
    }






}