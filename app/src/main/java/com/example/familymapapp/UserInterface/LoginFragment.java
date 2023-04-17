package com.example.familymapapp.UserInterface;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.familymapapp.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Communication.DataSyncTask;
import Communication.LoginTask;
import Communication.RegisterTask;
import Model.Person;
import Requests.RegisterRequest;

public class LoginFragment extends Fragment {
    public LoginFragment(Listener listener) {
        this.listener = listener;
    }
    private Listener listener;

    public interface Listener {
        void notifyDone(String authtoken);
    }
    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    //Static keys
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
    RadioGroup genderButtons;
    Button loginButton;
    Button registerButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        setHasOptionsMenu(false);

        serverHostNumber = view.findViewById(R.id.serverHostField); serverHostNumber.setText("10.0.2.2");
        serverPortNumber = view.findViewById(R.id.serverPortField); serverPortNumber.setText("8080");
        username = view.findViewById(R.id.usernameField); username.setText("testing_andrew");
        password = view.findViewById(R.id.passwordField); password.setText("pw123");
        firstName = view.findViewById(R.id.firstNameField);
        lastName = view.findViewById(R.id.lastNameField);
        email = view.findViewById(R.id.emailField);
        genderButtons = view.findViewById(R.id.genderButtons);

        serverHostNumber.addTextChangedListener(mTextWatcher);
        serverPortNumber.addTextChangedListener(mTextWatcher);
        username.addTextChangedListener(mTextWatcher);
        password.addTextChangedListener(mTextWatcher);
        firstName.addTextChangedListener(mTextWatcher);
        lastName.addTextChangedListener(mTextWatcher);
        email.addTextChangedListener(mTextWatcher);
        genderButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                //boolean checked = ((RadioButton) view).isChecked();
                // Check which radio button was clicked
                switch(checkedId) {
                    case R.id.femaleButton:
                        Log.println(Log.INFO, LOG_KEY, "Female button hit");
                        gender = "F";
                        break;
                    case R.id.maleButton:
                        Log.println(Log.INFO, LOG_KEY, "Male button hit");
                        gender = "M";
                        break;
                }
                checkFieldsForEmptyValues();
            }
        });

        loginButton = view.findViewById(R.id.LoginButton);
        registerButton = view.findViewById(R.id.RegisterButton);
        checkFieldsForEmptyValues();

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
                                        listener.notifyDone(authtoken);
                                    } else {
                                        Log.println(Log.INFO, LOG_KEY, "ERROR IN DATASYNC");
                                    }
                                    String welcomeString = getString(R.string.welcome, firstName, lastName);
                                    Toast.makeText(getActivity(), welcomeString, Toast.LENGTH_LONG).show();
                                    Log.println(Log.INFO, LOG_KEY, "Welcome back, " + firstName + " " + lastName);
                                }
                            };

                            DataSyncTask dataSyncTask = new DataSyncTask(dataSyncHandler, serverHostNumber.getText().toString(), serverPortNumber.getText().toString(), personID, authtoken);

                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            executor.submit(dataSyncTask);
                        } else {
                            Log.println(Log.ERROR, LOG_KEY, errorMessage);
                            String failString = getString(R.string.failure, errorMessage);
                            Toast.makeText(getActivity(), failString, Toast.LENGTH_LONG).show();
                        }
                    }
                };
                LoginTask loginTask = new LoginTask(loginButtonHandler, serverHostNumber.getText().toString(),
                        serverPortNumber.getText().toString(), username.getText().toString(), password.getText().toString());

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(loginTask);
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler registerButtonHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message message) {
                        Bundle bundle = message.getData();
                        boolean success = bundle.getBoolean(SUCCESS_KEY);
                        String errorMessage = bundle.getString(MESSAGE_KEY);
                        String authtoken = bundle.getString(AUTHTOKEN_KEY);
                        if (success) {
                            listener.notifyDone(authtoken);
                            Log.println(Log.INFO, LOG_KEY, "Register Success");
                            Toast.makeText(getActivity(), R.string.success, Toast.LENGTH_LONG).show();
                        } else {
                            Log.println(Log.ERROR, LOG_KEY, errorMessage);
                            String failString = getString(R.string.failure, errorMessage);
                            Toast.makeText(getActivity(), failString, Toast.LENGTH_LONG).show();
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

        return view;
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
            Log.println(Log.INFO, LOG_KEY, "TextChange worked");
            // check Fields For Empty Values
            checkFieldsForEmptyValues();
        }
    };

    void checkFieldsForEmptyValues(){
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