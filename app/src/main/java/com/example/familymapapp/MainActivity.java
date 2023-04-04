package com.example.familymapapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.familymapapp.cache.DataCache;

import UserInterface.LoginFragment;
import UserInterface.MapsFragment;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener {
    String authtoken = null;
    private final String AUTHTOKEN_KEY = "AuthtokenKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve clickCount from SharedPreferences if it exists
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        //authtoken = sharedPreferences.getString(AUTHTOKEN_KEY, null);
        authtoken = null;

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentFrameLayout);

        if (fragment == null) {
            fragment = createFirstFragment();
            fragmentManager.beginTransaction().add(R.id.fragmentFrameLayout, fragment).commit();
        } else {
            if (fragment instanceof LoginFragment) {
                ((LoginFragment) fragment).registerListener(this);
            }
        }
    }

    private Fragment createFirstFragment() {
        Fragment fragment;
        if (authtoken == null) {
            //pass this.main as listener for notifyDone() method
            fragment = new LoginFragment(this);
        } else {
            fragment = new MapsFragment();
        }
        return fragment;
    }
    @Override
    public void notifyDone(String authtoken) {
        //switch views here
        this.authtoken = authtoken;

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = new MapsFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentFrameLayout, fragment)
                .commit();
    }


    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String savedAuthtoken = DataCache.getInstance().getAuthtoken();
        editor.putString(AUTHTOKEN_KEY, savedAuthtoken);
        editor.apply();
    }
}