package com.example.familymapapp.UserInterface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.familymapapp.R;
import com.example.familymapapp.cache.DataCache;

import Model.Event;

public class EventActivity extends AppCompatActivity implements MapsFragment.Listener {

    public static final String SELECTED_EVENT_KEY = "EventIDKey";
    private static final String LOG_TAG = "EventActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Intent intent = getIntent();
        String selectedEventID = intent.getStringExtra(SELECTED_EVENT_KEY);
        Log.println(Log.INFO, LOG_TAG, "Selected EventID recieved: " + selectedEventID);
        Event event = DataCache.getInstance().getEventByID(selectedEventID);
        Log.println(Log.INFO, LOG_TAG, "Event recieved: " + event.getEventID());

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentFrameLayoutEvents);
        if (fragment == null) {
            fragment = new MapsFragment(this, event);
            fragmentManager.beginTransaction().add(R.id.fragmentFrameLayoutEvents, fragment).commit();
        } else {
            if (fragment instanceof MapsFragment) {
                ((MapsFragment) fragment).registerListener(this);
                ((MapsFragment) fragment).setSelectedEvent(event);
            }
        }

    }
    @Override
    public void notifyDone(String authtoken) {
        //do i need this?
    }
}