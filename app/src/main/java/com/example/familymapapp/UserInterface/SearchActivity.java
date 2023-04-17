package com.example.familymapapp.UserInterface;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.familymapapp.R;
import com.example.familymapapp.cache.DataCache;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.ArrayList;
import java.util.List;

import Model.Event;
import Model.Person;

public class SearchActivity extends AppCompatActivity {
    private final int PERSON_KEY = 0;
    private final int EVENT_KEY = 1;
    private final String LOG_KEY = "SearchActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(mTextWatcher);

        Iconify.with(new FontAwesomeModule());

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
    }

    private class SearchActivityAdapter extends RecyclerView.Adapter<SearchActivityViewHolder> {
        private final List<Person> personList;
        private final List<Event> eventList;

        public SearchActivityAdapter(List<Person> personList, List<Event> eventList) {
            this.personList = personList;
            this.eventList = eventList;
        }

        @Override
        public int getItemViewType(int position) {
            return position < personList.size() ? PERSON_KEY : EVENT_KEY;
        }

        @NonNull
        @Override
        public SearchActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.person_event_item, parent, false);
            return new SearchActivityViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchActivityViewHolder holder, int position) {
            Log.println(Log.INFO, LOG_KEY, "Logging position: " + position);
            if (position < personList.size()) {
                holder.bind(personList.get(position));
            } else {
                holder.bind(eventList.get(position - personList.size()));
            }
        }
        @Override
        public int getItemCount() {
            return eventList.size() + personList.size();
        }
    }

    private class SearchActivityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView title;
        private final TextView description;
        private final ImageView image;
        private final int viewType;
        private Event event;
        private Person person;


        public SearchActivityViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            if (viewType == EVENT_KEY) {
                title = itemView.findViewById(R.id.eventTitle);
                description = itemView.findViewById(R.id.eventDescription);
                image = itemView.findViewById(R.id.mapEventImage);
            } else {
                title = itemView.findViewById(R.id.eventTitle); //this would be the person's name
                description = itemView.findViewById(R.id.eventDescription); // not actually used
                image = itemView.findViewById(R.id.mapEventImage);
            }
        }

        private void bind(Event event) {
            Log.println(Log.INFO, LOG_KEY, "Binding event: " + event);
            this.event = event;
            title.setText(String.format("%s: %s, %s (%d)", event.getEventType(), event.getCity(), event.getCountry(), event.getYear()));
            Person associatedPerson = DataCache.getInstance().getPeopleByID(event.getPersonID());
            description.setText(String.format("%s %s", associatedPerson.getFirstName(), associatedPerson.getLastName()));

            int color = MapsFragment.getEventColorInt(event.getEventType());
            Drawable eventIcon = new IconDrawable(getBaseContext(), FontAwesomeIcons.fa_map_marker).colorRes(color).sizeDp(40); //why getbase context again?
            image.setImageDrawable(eventIcon);
        }

        private void bind(Person person) {
            Log.println(Log.INFO, LOG_KEY, "Binding person: " + person);
            this.person = person;
            title.setText(String.format("%s %s", person.getFirstName(), person.getLastName()));
            description.setText(""); // blank space babay

            if (person.getGender().equalsIgnoreCase("M")) {
                Drawable genderIcon = new IconDrawable(getBaseContext(), FontAwesomeIcons.fa_male).colorRes(R.color.blue).sizeDp(40); //why getbase context again?
                image.setImageDrawable(genderIcon);
            } else {
                Drawable genderIcon = new IconDrawable(getBaseContext(), FontAwesomeIcons.fa_female).colorRes(R.color.pink).sizeDp(40); //why getbase context again?
                image.setImageDrawable(genderIcon);
            }
        }

        @Override
        public void onClick(View view) {
            if (viewType == EVENT_KEY) {
                //Toast.makeText(SearchActivity.this, String.format("Event clicked: %s", event.getEventID()), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SearchActivity.this, EventActivity.class);
                intent.putExtra(EventActivity.SELECTED_EVENT_KEY, event.getEventID());
                startActivity(intent);
            } else {
                //Toast.makeText(SearchActivity.this, String.format("Person clicked: %s", person.getFirstName()), Toast.LENGTH_SHORT).show();
                //switch to person activity
                Intent intent = new Intent(SearchActivity.this, PersonActivity.class);
                intent.putExtra(PersonActivity.PERSON_ID_KEY, person.getPersonID());
                startActivity(intent);
            }
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            String searchString = charSequence.toString();
            Log.println(Log.INFO, LOG_KEY, "OnTextChanged worked: " + searchString);

            RecyclerView recyclerView = findViewById(R.id.recycler_view);

            List<Event> eventList = DataCache.getInstance().searchEvent(searchString);
            List<Person> personList = DataCache.getInstance().searchPerson(searchString);

            SearchActivityAdapter adapter = new SearchActivityAdapter(personList, eventList);
            recyclerView.setAdapter(adapter);
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };
}