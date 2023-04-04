package com.example.familymapapp.UserInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.w3c.dom.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import Model.Event;
import Model.Person;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
    private GoogleMap map;
    private View view;
    private final String LOG_TAG = "MapsFragment";

    public MapsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        this.view = inflater.inflate(R.layout.fragment_maps, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLoadedCallback(this);
        setMarkers();
    }

    @Override
    public void onMapLoaded() {

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.println(Log.INFO, LOG_TAG, "Marker clicked: " + marker.toString());
                if (marker != null) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), (float) 1.80);
                    map.animateCamera(cameraUpdate);
                    String eventID = (String) marker.getTag();
                    Event event = DataCache.getInstance().getEventByID(eventID);
                    Person person = DataCache.getInstance().getPeopleByID(event.getPersonID());
                    Log.println(Log.INFO, LOG_TAG, "Event clicked: " + person.getGender());

                    TextView nameText = view.findViewById(R.id.mapNameTextBox);
                    nameText.setText(String.format("%s %s", person.getFirstName(), person.getLastName()));

                    TextView infoText = view.findViewById(R.id.mapInfoTextBox);
                    infoText.setText(String.format("%s: %s (%d)", event.getEventType(), event.getCity(), event.getYear()));

                    ImageView imageView = view.findViewById(R.id.mapEventImage);
                    if (person.getGender().equalsIgnoreCase("M")) {
                        //set to male
                        imageView.setImageResource(R.drawable.male_icon);
                    } else if (person.getGender().equalsIgnoreCase("F")) {
                        //set to female
                        imageView.setImageResource(R.drawable.female_icon);
                    } else {
                        //display ! with error
                        imageView.setImageResource(R.drawable.exclamation_icon);
                    }

                    map.clear();
                    setMarkers();
                    drawAllLines(event);
                    return true;
                }
                return false;
            }
        });
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
            } else {
                Log.println(Log.INFO, LOG_TAG, "Person has no spouse");
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
            } else {
                Log.println(Log.INFO, LOG_TAG, "Person has no father");
            }
            if (motherBirthEvent != null) {
                drawSingleLine(event, motherBirthEvent, Color.RED, 17);
            }else {
                Log.println(Log.INFO, LOG_TAG, "Person has no mother");
            }
        }

        if (lifeStoryLines) {
            SortedSet<Event> lifeEvents = DataCache.getInstance().getEventsByPersonID(event.getPersonID());
            Event e1 = null;
            Event e2 = null;
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
        Event fatherBirthEvent = drawFamilyLines(fatherID, width - 4);

        String motherID = DataCache.getInstance().getPeopleByID(personID).getMotherID();
        Event motherBirthEvent = drawFamilyLines(motherID, width - 4);

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

    private void setMarkers() {
        //CHECK SETTINGS FOR VALUES
        DataCache data = DataCache.getInstance();
        boolean fatherSide = true;
        boolean motherSide = true;
        boolean maleEvents = true;
        boolean femaleEvents = true;

        Set<Person> peopleToAdd = new HashSet<>();
        if (fatherSide) {
            if (maleEvents) {
                peopleToAdd.addAll(data.getFatherSideMales());
            }
            if (femaleEvents) {
                peopleToAdd.addAll(data.getFatherSideFemales());
            }
        }
        if (motherSide) {
            if (maleEvents) {
                peopleToAdd.addAll(data.getMotherSideMales());
            }
            if (femaleEvents) {
                peopleToAdd.addAll(data.getMotherSideFemales());
            }
        }

        for (Person personToAdd : peopleToAdd) {
            if (personToAdd == null) { continue; }
            //add this persons events to map
            String personID = personToAdd.getPersonID();
            Log.println(Log.INFO, LOG_TAG, "Adding person: " + personID);
            SortedSet<Event> eventsToDisplay = data.getEventsByPersonID(personToAdd.getPersonID());
            for (Event event : eventsToDisplay) {
                float eventColor = getEventColor(event.getEventType());
                Marker marker = map.addMarker(new MarkerOptions().position(new
                        LatLng(event.getLatitude(), event.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(eventColor)));
                marker.setTag(event.getEventID());
                Log.println(Log.INFO, LOG_TAG, "EVENT ADDED: " + event.toString());
            }
        }
    }
    private float getEventColor(String eventType) {
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

}