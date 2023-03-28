package com.example.familymapapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Communication.DataSyncTask;
import Communication.LoginTask;
import Communication.RegisterTask;
import Model.Person;
import Requests.LoginRequest;
import Requests.RegisterRequest;
import Results.LoginResult;

public class MainActivity extends AppCompatActivity {

    private static final String SUCCESS_KEY = "SuccessKey";
    private static final String AUTHTOKEN_KEY = "AuthtokenKey";
    private static final String FIRST_NAME_KEY = "FirstNameKey";
    private static final String LAST_NAME_KEY = "LastNameKey";
    private static final String PERSONID_KEY = "PersonIDKey";
    private static final String LOG_KEY = "MainActivity";
    private static final String MESSAGE_KEY = "MessageKey";
    private String gender = null;
    Person user = null;


    EditText serverHostNumber;
    EditText serverPortNumber;
    EditText username;
    EditText password;
    EditText firstName;
    EditText lastName;
    EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      if (authtoken == null)
        setContentView(R.layout.fragment_login);

         serverHostNumber = findViewById(R.id.serverHostField); serverHostNumber.setText("10.0.2.2");
         serverPortNumber = findViewById(R.id.serverPortField); serverPortNumber.setText("8080");
         username = findViewById(R.id.usernameField); username.setText("testing_andrew");
         password = findViewById(R.id.passwordField); password.setText("pw123");
         firstName = findViewById(R.id.firstNameField);
         lastName = findViewById(R.id.lastNameField);
         email = findViewById(R.id.emailField);

        serverHostNumber.addTextChangedListener(mTextWatcher);
        serverPortNumber.addTextChangedListener(mTextWatcher);
        username.addTextChangedListener(mTextWatcher);
        password.addTextChangedListener(mTextWatcher);
        firstName.addTextChangedListener(mTextWatcher);
        lastName.addTextChangedListener(mTextWatcher);
        email.addTextChangedListener(mTextWatcher);

        checkFieldsForEmptyValues();

        Button loginButton = findViewById(R.id.LoginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler loginButtonHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        boolean success = bundle.getBoolean(SUCCESS_KEY);
                        String authtoken = bundle.getString(AUTHTOKEN_KEY);
                        String personID = bundle.getString(PERSONID_KEY);
                        String errorMessage = bundle.getString(MESSAGE_KEY);
                        if (success) {
                            Log.println(Log.INFO, LOG_KEY, "Success");
                            Handler dataSyncHandler = new Handler(Looper.getMainLooper()){
                                @Override
                                public void handleMessage(Message message1) {
                                    Bundle dataSyncBundle = message1.getData();
                                    boolean success = dataSyncBundle.getBoolean(SUCCESS_KEY);
                                    String firstName = dataSyncBundle.getString(FIRST_NAME_KEY);
                                    String lastName = dataSyncBundle.getString(LAST_NAME_KEY);
                                    //cache this data
                                    if (success) {
                                        Log.println(Log.INFO, LOG_KEY, "DataSyncHandler success!");
                                    } else {
                                        Log.println(Log.INFO, LOG_KEY, "ERROR IN DATASYNC");
                                    }
                                    String welcomeString = getString(R.string.welcome, firstName, lastName);
                                    Toast.makeText(MainActivity.this, welcomeString, Toast.LENGTH_LONG).show();
                                    Log.println(Log.INFO, LOG_KEY, "Welcome back, " + firstName + " " + lastName);
                                }
                            };

                            DataSyncTask dataSyncTask = new DataSyncTask(dataSyncHandler, serverHostNumber.getText().toString(), serverPortNumber.getText().toString(), personID, authtoken);

                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            executor.submit(dataSyncTask);
                        } else {
                            Log.println(Log.ERROR, LOG_KEY, errorMessage);
                            String failString = getString(R.string.failure, errorMessage);
                            Toast.makeText(MainActivity.this, failString, Toast.LENGTH_LONG).show();
                        }
                    }
                };
                LoginTask loginTask = new LoginTask(loginButtonHandler, serverHostNumber.getText().toString(),
                        serverPortNumber.getText().toString(), username.getText().toString(), password.getText().toString());

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(loginTask);
            }
        });


        Button registerButton = findViewById(R.id.RegisterButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler registerButtonHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        boolean success = bundle.getBoolean(SUCCESS_KEY);
                        String errorMessage = bundle.getString(MESSAGE_KEY);
                        if (success) {
                            Log.println(Log.INFO, LOG_KEY, "Register Success");
                            Toast.makeText(MainActivity.this, R.string.success, Toast.LENGTH_LONG).show();
                        } else {
                            Log.println(Log.ERROR, LOG_KEY, errorMessage);
                            String failString = getString(R.string.failure, errorMessage);
                            Toast.makeText(MainActivity.this, failString, Toast.LENGTH_LONG).show();
                        }
                    }
                };



                RegisterRequest request = new RegisterRequest(username.getText().toString(), password.getText().toString(), email.getText().toString(), firstName.getText().toString(),
                        lastName.getText().toString(), gender);

                RegisterTask registerTask = new RegisterTask(registerButtonHandler, serverHostNumber.getText().toString(),
                        serverPortNumber.getText().toString(), request);

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(registerTask);
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
                    ((RadioButton) view).toggle();
                    Log.println(Log.INFO, LOG_KEY, "Female button hit");
                    gender = "F";
                break;
            case R.id.maleButton:
                if (checked)
                    ((RadioButton) view).toggle();
                    Log.println(Log.INFO, LOG_KEY, "Male button hit");
                    gender = "M";
                break;
        }
        checkFieldsForEmptyValues();
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // check Fields For Empty Values
            checkFieldsForEmptyValues();
        }
    };

    void checkFieldsForEmptyValues(){
        Button loginButton = (Button) findViewById(R.id.LoginButton);
        Button registerButton = (Button) findViewById(R.id.RegisterButton);

        //disable button if not all fields are entered
        if (!serverHostNumber.getText().toString().equals("")
        && !serverPortNumber.getText().toString().equals("")
        && !username.getText().toString().equals("")
        && !password.getText().toString().equals("")) {
            loginButton.setEnabled(true);
            if (!firstName.getText().toString().equals("")
                    && !lastName.getText().toString().equals("")
                    && !email.getText().toString().equals("")
                    && gender != null) {
                registerButton.setEnabled(true);
            } else {
                registerButton.setEnabled(false);
            }
        } else {
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
        }
    }
}