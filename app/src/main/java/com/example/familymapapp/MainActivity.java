package com.example.familymapapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import Model.Person;

public class MainActivity extends AppCompatActivity {

    Person user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      if (authtoken == null)
        setContentView(R.layout.fragment_login);

        EditText serverHostNumber = findViewById(R.id.serverHostField);
        EditText serverPortNumber = findViewById(R.id.serverPortField);
        EditText username = findViewById(R.id.usernameField);
        EditText password = findViewById(R.id.passwordField);

        Button loginButton = findViewById(R.id.LoginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("SH" + serverHostNumber.getText().toString());
                System.out.println("SP" + serverPortNumber.getText().toString());
                System.out.println("UN" + username.getText().toString());
                System.out.println("PW" + password.getText().toString());
            }
        });
    }

    public void onGenderButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.femaleButton:
                if (checked)
                    // Pirates are the best
                    ((RadioButton) view).toggle();
                    System.out.println("Female Button hit");
                break;
            case R.id.maleButton:
                if (checked)
                    // Ninjas rule
                    ((RadioButton) view).toggle();
                    System.out.println("Male Button hit");
                break;
        }
    }



}