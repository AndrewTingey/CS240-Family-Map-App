package com.example.familymapapp.UserInterface;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.familymapapp.R;

public class TestActivity extends AppCompatActivity {
    private final String LOG_TAG = "TextActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.println(Log.INFO, LOG_TAG, "Creating Test Activity");
        setContentView(R.layout.activity_test);
    }
}