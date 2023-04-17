package com.example.familymapapp.UserInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.familymapapp.R;
import com.example.familymapapp.cache.DataCache;

import com.example.familymapapp.UserInterface.LoginFragment;
import com.example.familymapapp.UserInterface.MapsFragment;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener, MapsFragment.Listener {
    String authtoken = null;
    private final String AUTHTOKEN_KEY = "AuthtokenKey";
    private final String HAS_MENU_KEY  = "MenuKey";
    private final String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authtoken = DataCache.getInstance().getAuthtoken();
        Iconify.with(new FontAwesomeModule());

        if (savedInstanceState != null) {
            // Retrieve authtoken from savedinstancestate if it exists
            SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
            authtoken = sharedPreferences.getString(AUTHTOKEN_KEY, null);
            Log.println(Log.INFO, LOG_TAG, "Loaded authtoken from saved instance");
        } else {
            authtoken = DataCache.getInstance().getAuthtoken();
            Log.println(Log.INFO, LOG_TAG, "Loaded authtoken from saved datacache: " + authtoken);
        }

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentFrameLayout);

        if (fragment == null) {
            fragment = createFirstFragment();
            fragmentManager.beginTransaction().add(R.id.fragmentFrameLayout, fragment).commit();
        } else {
            if (fragment instanceof LoginFragment) {
                ((LoginFragment) fragment).registerListener(this);
            }
            if (fragment instanceof MapsFragment) {
                ((MapsFragment) fragment).registerListener(this);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private Fragment createFirstFragment() {
        Fragment fragment;
        if (authtoken == null) {
            //pass this.main as listener for notifyDone() method
            fragment = new LoginFragment(this);
            Bundle bundle = new Bundle();
            bundle.putBoolean(HAS_MENU_KEY, false);
            fragment.setArguments(bundle);
        } else {
            fragment = new MapsFragment(this);
            Bundle bundle = new Bundle();
            bundle.putBoolean(HAS_MENU_KEY, true);
            fragment.setArguments(bundle);
        }
        return fragment;
    }
    @Override
    public void notifyDone(String authtoken) {
        //switch views here
        this.authtoken = authtoken;

        //TODO COMMENTED OUT FOR CACHING DATA AND DEBUGGING

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = createFirstFragment();

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