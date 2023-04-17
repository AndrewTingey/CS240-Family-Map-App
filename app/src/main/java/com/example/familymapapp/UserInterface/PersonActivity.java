package com.example.familymapapp.UserInterface;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.familymapapp.R;
import com.example.familymapapp.cache.DataCache;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import Model.Event;
import Model.Person;

public class PersonActivity extends AppCompatActivity {
    public static final String PERSON_ID_KEY = "PersonIDKey";
    public static final String SELECTED_EVENT_KEY = "EventIDKey";
    private List<Event> eventList = new ArrayList<>();
    private List<Person> personList = new ArrayList<>();
    private String firstName;
    private String lastName;
    private String gender;
    private Person selectedPerson;
    private void setLists(String personID) {
        DataCache data = DataCache.getInstance();
        selectedPerson = data.getPeopleByID(personID);
        this.firstName = selectedPerson.getFirstName();
        this.lastName = selectedPerson.getLastName();
        if (selectedPerson.getGender().equalsIgnoreCase("M")) {
            this.gender = "Male";
        } else {
            this.gender = "Female";
        }

        SortedSet<Event> lifeEvents = data.getEventsByPersonID(personID);
        for (Event event : lifeEvents) {
            eventList.add(event);
        }

        //Probably function call on null errors here
        List<Person> family = data.getChildrenByParentID(personID);
        if (family == null) {
            family = new ArrayList<>();
        }

        Person familyMember = data.getPeopleByID(selectedPerson.getSpouseID());
        if (familyMember != null) {
            family.add(familyMember);
        }
        familyMember = data.getPeopleByID(selectedPerson.getFatherID());
        if (familyMember != null) {
            family.add(familyMember);
        }
        familyMember = data.getPeopleByID(selectedPerson.getMotherID());
        if (familyMember != null) {
            family.add(familyMember);
        }
        personList = family;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Iconify.with(new FontAwesomeModule());

        Intent intent = getIntent();
        String personID = intent.getStringExtra(PERSON_ID_KEY);
        setLists(personID);

        TextView firstNameView = findViewById(R.id.firstNameText);
        TextView lastNameView = findViewById(R.id.lastNameText);
        TextView genderView = findViewById(R.id.genderText);

        firstNameView.setText(firstName);
        lastNameView.setText(lastName);
        genderView.setText(gender);

        ExpandableListView expandableListView = findViewById(R.id.expandableListView);
        expandableListView.setAdapter(new ExpandableListAdapter(eventList, personList));
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private static final int PERSON_GROUP_POSITION = 0;
        private static final int EVENT_GROUP_POSITION = 1;
        List<Person> personList;
        List<Event> eventList;

        public ExpandableListAdapter(List<Event> eventList, List<Person> personList) {
            this.personList = personList;
            this.eventList = eventList;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int i) {
            switch (i) {
                case PERSON_GROUP_POSITION:
                    return personList.size();
                case EVENT_GROUP_POSITION:
                    return eventList.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + i);
            }
        }

        @Override
        public Object getGroup(int i) {
            return null;
        }

        @Override
        public Object getChild(int i, int i1) {
            return null;
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case PERSON_GROUP_POSITION:
                    titleView.setText(R.string.family);
                    break;
                case EVENT_GROUP_POSITION:
                    titleView.setText(R.string.life_events);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View itemView;
            switch(groupPosition) {
                case PERSON_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.person_event_item, parent, false);
                    initializePersonView(itemView, childPosition);
                    break;
                case EVENT_GROUP_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.person_event_item, parent, false);
                    initializeEventView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
            return itemView;
        }

        private void initializeEventView(View eventItemView, int childPosition) {
            Event event = eventList.get(childPosition);

            TextView eventTitle = eventItemView.findViewById(R.id.eventTitle);
            eventTitle.setText(String.format("%s: %s, %s (%d)", event.getEventType(), event.getCity(), event.getCountry(), event.getYear()));
            Person associatedPerson = DataCache.getInstance().getPeopleByID(event.getPersonID());

            TextView description = eventItemView.findViewById(R.id.eventDescription);
            description.setText(String.format("%s %s", associatedPerson.getFirstName(), associatedPerson.getLastName()));

            ImageView icon = eventItemView.findViewById(R.id.mapEventImage);

            int color = MapsFragment.getEventColorInt(event.getEventType());
            Drawable genderIcon = new IconDrawable(getBaseContext(), FontAwesomeIcons.fa_map_marker).colorRes(color).sizeDp(40);
            icon.setImageDrawable(genderIcon);

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(PersonActivity.this, "Event Selected: " + eventList.get(childPosition).getEventID(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PersonActivity.this, EventActivity.class);
                    intent.putExtra(SELECTED_EVENT_KEY, eventList.get(childPosition).getEventID());
                    startActivity(intent);
                }
            });
        }

        private void initializePersonView(View personItemView, final int childPosition) {
            Person person = personList.get(childPosition);

            TextView PersonName = personItemView.findViewById(R.id.eventTitle);
            PersonName.setText(String.format("%s %s", person.getFirstName(), person.getLastName()));

            TextView relationship = personItemView.findViewById(R.id.eventDescription);
            String relationToPerson = getRelation(person.getPersonID());
            relationship.setText(relationToPerson);

            ImageView icon = personItemView.findViewById(R.id.mapEventImage);

            if (person.getGender().equalsIgnoreCase("M")) {
                Drawable genderIcon = new IconDrawable(getBaseContext(), FontAwesomeIcons.fa_male).
                        colorRes(R.color.blue).sizeDp(40);
                icon.setImageDrawable(genderIcon);
            } else {
                Drawable genderIcon = new IconDrawable(getBaseContext(), FontAwesomeIcons.fa_female).
                        colorRes(R.color.pink).sizeDp(40);
                icon.setImageDrawable(genderIcon);
            }

            personItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(PersonActivity.this, "Person Selected: " + personList.get(childPosition).getFirstName(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PersonActivity.this, PersonActivity.class);
                    intent.putExtra(PERSON_ID_KEY, personList.get(childPosition).getPersonID());
                    startActivity(intent);
                }
            });
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }
    }

    private String getRelation(String person2ID) {
        if (Objects.equals(selectedPerson.getFatherID(), person2ID)) {
            return "Father";
        } else if (Objects.equals(selectedPerson.getMotherID(), person2ID)) {
            return "Mother";
        } else if (Objects.equals(selectedPerson.getSpouseID(), person2ID)) {
            return "Spouse";
        } else {
            String gender = DataCache.getInstance().getPeopleByID(person2ID).getGender();
            if (gender.equalsIgnoreCase("M")) {
                return "Son";
            } else {
                return "Daughter";
            }
        }
    }
}