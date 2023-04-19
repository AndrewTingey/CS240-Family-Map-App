package com.example.familymapapp.UserInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.familymapapp.R;
import com.example.familymapapp.cache.DataCache;
import com.example.familymapapp.cache.SettingsCache;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import Model.Event;
import Model.Person;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private GoogleMap map;
    private View view;
    private Event selectedEvent = null;
    private boolean hasMenu = false;
    private Listener listener;
    private final String LOG_TAG = "MapsFragment";


    public interface Listener {
        void notifyDone(String authtoken);
    }
    public void registerListener(Listener listener) {
        this.listener = listener;
    }

    public MapsFragment(Listener listener) {
        this.listener = listener;
    }

    public MapsFragment(Listener listener, Event selectedEvent) {
        this.listener = listener;
        setSelectedEvent(selectedEvent);
    }
    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(hasMenu);

        //check if logged out
        if (DataCache.getInstance().getAuthtoken() == null) {
            listener.notifyDone(null);
        }
        Log.println(Log.INFO, LOG_TAG, "MAP: " + map);
        if (map != null && selectedEvent != null) {
            Log.println(Log.INFO, LOG_TAG, "SETTING MARKERS");
            animateCamera();
        }
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem settingsMenuItem = menu.findItem(R.id.settingsMenuItem);
        settingsMenuItem.setIcon(new IconDrawable(getContext(),
                FontAwesomeIcons.fa_gear).colorRes(R.color.white)
                .actionBarSize());

        MenuItem searchMenuItem = menu.findItem(R.id.searchMenuItem);
        searchMenuItem.setIcon(new IconDrawable(getContext(),
                FontAwesomeIcons.fa_search).colorRes(R.color.white)
                .actionBarSize());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.println(Log.INFO, LOG_TAG, "MENU ITEM SELECTEd FROM maps");
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (item.getItemId() == R.id.settingsMenuItem) {
            Log.println(Log.INFO, LOG_TAG, "Settings Clicked");
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.searchMenuItem) {
            Log.println(Log.INFO, LOG_TAG, "Search Clicked");
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            hasMenu = getArguments().getBoolean(MainActivity.HAS_MENU_KEY);
        }
        setHasOptionsMenu(hasMenu);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        this.view = inflater.inflate(R.layout.fragment_maps, container, false);

        Iconify.with(new FontAwesomeModule());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //set default image
        ImageView imageView = view.findViewById(R.id.mapEventImage);
        Drawable safariIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_safari).
                colorRes(R.color.black).sizeDp(40);
        imageView.setImageDrawable(safariIcon);

        //make info box clickable
        LinearLayout infoBox = view.findViewById(R.id.infoBox);
        infoBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedEvent != null) {
                    Log.println(Log.INFO, LOG_TAG, "INFO BOX PRESSED, with event: " + selectedEvent.getEventID());
                    //switch to person activity
                    Intent intent = new Intent(getActivity(), PersonActivity.class);
                    intent.putExtra(PersonActivity.PERSON_ID_KEY, selectedEvent.getPersonID());
                    startActivity(intent);
                }
            }
        });
        return view;
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        setMarkers(); //moevd to on map loaded
        if (this.selectedEvent != null) {
            animateCamera();
        }
        Log.println(Log.INFO, LOG_TAG, "onMapReadyCalleed");
    }
    @Override
    public void onMapLoaded() {
        Log.println(Log.INFO, LOG_TAG, "onMapLoaded called");
        //setMarkers();
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.println(Log.INFO, LOG_TAG, "Marker clicked: " + marker.toString());
                if (marker != null) {
                    String eventID = (String) marker.getTag();
                    setSelectedEvent(DataCache.getInstance().getEventByID(eventID));
                    animateCamera();
                    return true;
                }
                return false;
            }
        });
    }

    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public void animateCamera() {
        LatLng pos = new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pos, (float) 1.80);
        map.animateCamera(cameraUpdate);

        Person person = DataCache.getInstance().getPeopleByID(selectedEvent.getPersonID());
        Log.println(Log.INFO, LOG_TAG, "Event clicked: " + person.getGender());

        TextView nameText = view.findViewById(R.id.mapNameTextBox);
        nameText.setText(String.format("%s %s", person.getFirstName(), person.getLastName()));

        TextView infoText = view.findViewById(R.id.mapInfoTextBox);
        infoText.setText(String.format("%s: %s (%d)", selectedEvent.getEventType(), selectedEvent.getCity(), selectedEvent.getYear()));

        ImageView imageView = view.findViewById(R.id.mapEventImage);
        if (person.getGender().equalsIgnoreCase("M")) {
            //set to male
            Drawable genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_male).
                    colorRes(R.color.blue).sizeDp(40);
            imageView.setImageDrawable(genderIcon);
        } else if (person.getGender().equalsIgnoreCase("F")) {
            //set to female
            Drawable genderIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_female).
                    colorRes(R.color.pink).sizeDp(40);
            imageView.setImageDrawable(genderIcon);
            //imageView.setImageResource(R.drawable.female_icon);
        } else {
            //display ! with error
            Drawable safariIcon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_safari).
                    colorRes(R.color.black).sizeDp(40);
            imageView.setImageDrawable(safariIcon);
        }
        map.clear();
        setMarkers();
        drawAllLines(selectedEvent);
    }
    private void setMarkers() {
        //CHECK SETTINGS FOR VALUES
        DataCache data = DataCache.getInstance();
        SettingsCache settings = SettingsCache.getInstance();
        boolean fatherSide = settings.isFatherSide();
        boolean motherSide = settings.isMotherSide();
        boolean maleEvents = settings.isMaleEvents();
        boolean femaleEvents = settings.isFemaleEvents();

        Set<Person> peopleToAdd = new HashSet<>();
        //add user and spouse always unconditionally
        Person user = data.getUser();
        peopleToAdd.add(user);
        peopleToAdd.add(data.getPeopleByID(user.getSpouseID()));


        //conditionally add ancestors
        if (maleEvents) {
            if (fatherSide) {
                peopleToAdd.addAll(data.getFatherSideMales());
            }
            if (motherSide) {
                peopleToAdd.addAll(data.getMotherSideMales());
            }
        } if (femaleEvents) {
            if (fatherSide) {
                peopleToAdd.addAll(data.getFatherSideFemales());
            }
            if (motherSide) {
                peopleToAdd.addAll(data.getMotherSideFemales());
            }
        }

        for (Person personToAdd : peopleToAdd) {
            if (personToAdd == null) { continue; }
            //add this person's events to map
            String personID = personToAdd.getPersonID();
            SortedSet<Event> eventsToDisplay = data.getEventsByPersonID(personID);
            for (Event event : eventsToDisplay) {
                if (data.isInFilters(event)) {
                    float eventColor = getEventColor(event.getEventType());
                    Marker marker = map.addMarker(new MarkerOptions().position(new
                            LatLng(event.getLatitude(), event.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(eventColor)));
                    marker.setTag(event.getEventID());
                }
            }
        }
    }

    private void drawAllLines(Event event) {
        SettingsCache settingsCache = SettingsCache.getInstance();
        boolean spouseLines = settingsCache.isSpouseLines();
        boolean familyTreeLines = settingsCache.isFamilyTreeLines();
        boolean lifeStoryLines = settingsCache.isLifeStoryLines();

        if (spouseLines) {
            Person spouse = DataCache.getInstance().getSpouseByID(event.getPersonID());
            if (spouse != null) {
                SortedSet<Event> spouseLifeEvents = DataCache.getInstance().getEventsByPersonID(spouse.getPersonID());
                Event spouseBirthEvent = spouseLifeEvents.first();

                drawSingleLine(event, spouseBirthEvent, Color.CYAN, 10);
            }
        }

        if (familyTreeLines) {
            //must do first iteration, because event clicked is not necessarily a birth event
            String fatherID = DataCache.getInstance().getPeopleByID(event.getPersonID()).getFatherID();
            Event fatherBirthEvent = drawFamilyLines(fatherID, 13);

            String motherID = DataCache.getInstance().getPeopleByID(event.getPersonID()).getMotherID();
            Event motherBirthEvent = drawFamilyLines(motherID, 13);

            if (fatherBirthEvent != null) {
                drawSingleLine(event, fatherBirthEvent, Color.BLUE, 17);
            }
            if (motherBirthEvent != null) {
                drawSingleLine(event, motherBirthEvent, Color.RED, 17);
            }
        }

        if (lifeStoryLines) {
            SortedSet<Event> lifeEvents = DataCache.getInstance().getEventsByPersonID(event.getPersonID());
            Event e1 = null;
            Event e2;
            for (Event lifeEvent : lifeEvents) {
                e2 = lifeEvent;
                if (e1 != null) {
                    drawSingleLine(e1, e2, Color.YELLOW, 5);
                }
                e1 = e2;
            }
        }
    }

    private Event drawFamilyLines(String personID, int width) {
        if (personID == null) return null;
        SortedSet<Event> lifeEvents = DataCache.getInstance().getEventsByPersonID(personID);
        if (lifeEvents == null) return null;
        Event firstEvent = lifeEvents.first();

        String fatherID = DataCache.getInstance().getPeopleByID(personID).getFatherID();
        Event fatherBirthEvent = drawFamilyLines(fatherID, width / 2);

        String motherID = DataCache.getInstance().getPeopleByID(personID).getMotherID();
        Event motherBirthEvent = drawFamilyLines(motherID, width / 2);

        if (fatherBirthEvent != null) {
            drawSingleLine(firstEvent, fatherBirthEvent, Color.BLUE, width);
        }
        if (motherBirthEvent != null) {
            drawSingleLine(firstEvent, motherBirthEvent, Color.RED, width);
        }

        return firstEvent;
    }

    void drawSingleLine(Event startEvent, Event endEvent, int googleColor, int width) {
        // Create start and end points for the line
        LatLng startPoint = new LatLng(startEvent.getLatitude(), startEvent.getLongitude());
        LatLng endPoint = new LatLng(endEvent.getLatitude(), endEvent.getLongitude());

        // Add line to map by specifying its endpoints, color, and width
        PolylineOptions options = new PolylineOptions()
                .add(startPoint)
                .add(endPoint)
                .color(googleColor)
                .width(width);
        Polyline line = map.addPolyline(options);
    }

    public static float getEventColor(String eventType) {
        Set<String> types = DataCache.getInstance().getEventTypes();
        int index = 0;
        for (String entry:types) {
            if (entry.equalsIgnoreCase(eventType)) break;
            index++;
        }
        switch (index) {
            case 0: return BitmapDescriptorFactory.HUE_RED;
            case 1: return BitmapDescriptorFactory.HUE_BLUE;
            case 2: return BitmapDescriptorFactory.HUE_GREEN;
            case 3: return BitmapDescriptorFactory.HUE_ORANGE;
            case 4: return BitmapDescriptorFactory.HUE_YELLOW;
            case 5: return BitmapDescriptorFactory.HUE_VIOLET;
            case 6: return BitmapDescriptorFactory.HUE_MAGENTA;
            case 7: return BitmapDescriptorFactory.HUE_AZURE;
            default: return BitmapDescriptorFactory.HUE_CYAN;
        }
    }

    public static int getEventColorInt(String eventType) {
        Set<String> types = DataCache.getInstance().getEventTypes();
        int index = 0;
        for (String entry:types) {
            if (entry.equalsIgnoreCase(eventType)) break;
            index++;
        }
        switch (index) {
            case 0: return R.color.Red;
            case 1: return R.color.Blue;
            case 2: return R.color.Green;
            case 3: return R.color.Orange;
            case 4: return R.color.Yellow;
            case 5: return R.color.Violet;
            case 6: return R.color.Magenta;
            case 7: return R.color.Azure; // what is azure
            default: return R.color.Cyan; //fix this color
        }
    }
}