package com.example.familymapapp.UserInterface;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.familymapapp.R;
import com.example.familymapapp.cache.DataCache;
import com.example.familymapapp.cache.SettingsCache;

public class SettingsActivity extends AppCompatActivity {

    private final String LOG_TAG = "SettingsActivity";
    private final String LIFE_STORY_LINES = "LifeStoryKey";
    private Switch lifeStory;
    private Switch familyTree;
    private Switch spouseLines;
    private Switch fatherSide;
    private Switch motherSide;
    private Switch maleEvents;
    private Switch femaleEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        lifeStory = findViewById(R.id.LifeStoryLines);
        familyTree = findViewById(R.id.FamilyTreeLines);
        spouseLines = findViewById(R.id.SpouseLines);
        fatherSide = findViewById(R.id.FathersSide);
        motherSide = findViewById(R.id.MothersSide);
        maleEvents = findViewById(R.id.MaleEvents);
        femaleEvents = findViewById(R.id.FemaleEvents);

        if (savedInstanceState != null) {
            boolean isLifeStory = savedInstanceState.getBoolean(LIFE_STORY_LINES, false);
            lifeStory.setChecked(isLifeStory);
            Log.println(Log.INFO, LOG_TAG, "From savedinstancestate");
        } else {
            SettingsCache settings = SettingsCache.getInstance();
            lifeStory.setChecked(settings.isLifeStoryLines());
            familyTree.setChecked(settings.isFamilyTreeLines());
            spouseLines.setChecked(settings.isSpouseLines());
            fatherSide.setChecked(settings.isFatherSide());
            motherSide.setChecked(settings.isMotherSide());
            maleEvents.setChecked(settings.isMaleEvents());
            femaleEvents.setChecked(settings.isFemaleEvents());
            Log.println(Log.INFO, LOG_TAG, "not from savedinstancestate");
        }


        RelativeLayout logoutButton = findViewById(R.id.LogoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.println(Log.INFO, LOG_TAG, "LOGOUT BUTTON PRESSED");
                DataCache.getInstance().logout();
                Intent intent = new Intent(getBaseContext(), MainActivity.class); //why get base context?
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SettingsCache settings = SettingsCache.getInstance();
        settings.setSettings(lifeStory.isChecked(), familyTree.isChecked(), spouseLines.isChecked(), fatherSide.isChecked(), motherSide.isChecked(), maleEvents.isChecked(), femaleEvents.isChecked());
        Log.println(Log.INFO, LOG_TAG, "Life Story Lines was saved from onStop() as: " + lifeStory.isChecked());
    }
}