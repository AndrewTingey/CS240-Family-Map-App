package com.example.familymapapp.UserInterface;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.familymapapp.R;

import java.util.ArrayList;
import java.util.List;

import Model.Event;
import Model.Person;

public class SearchActivity extends AppCompatActivity {
    private final int PERSON_KEY = 0;
    private final int EVENT_KEY = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView recyclerView = findViewById(R.id.recycler_view); //todo need to add tools:context to .xml
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        List<Event> eventList = searchEvents("THE STRING SEARCHED");
        List<Person> personList = searchPeople("THE STRING SEARCHED");

        SearchActivityAdapter adapter = new SearchActivityAdapter(personList, eventList);
        recyclerView.setAdapter(adapter);

    }

    private List<Person> searchPeople(String the_string_searched) { //TODO SEARCH DATACACHE AND FILTER
        Person person = new Person("at646", "andrewtin", "ANDREW", "TINGEY", "M", null, null, null);
        List<Person> personList = new ArrayList<>();
        personList.add(person);
        return personList;
    }

    private List<Event> searchEvents(String the_string_searched) { //TODO SEARCH DATACACHE AND FILTER
        Event event = new Event("EventID", "Username", "PersonID", (float) 1.1, (float) 2.2, "USA", "Las Vegas", "Party", 1999);
        List<Event> eventList = new ArrayList<>();
        eventList.add(event);
        return eventList;
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
            View view = getLayoutInflater().inflate(R.layout.person_event_item, parent, false); // todo write personEventItem.xml
            return new SearchActivityViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchActivityViewHolder holder, int position) {
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
        private final int viewType;
        private Event event;
        private Person person;


        public SearchActivityViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            if (viewType == EVENT_KEY) { //todo write person/event_item.xml formatted with these @+id
                title = itemView.findViewById(R.id.eventTitle);
                description = itemView.findViewById(R.id.eventDescription);
            } else {
                title = itemView.findViewById(R.id.eventTitle); //this would be the person's name
                description = null;
            }
        }

        private void bind(Event event) { //TODO format the strings here and there v
            this.event = event;
            title.setText(event.getEventID());
            description.setText(event.getCity());
        }

        private void bind(Person person) { // this is very wrong
            this.person = person;
            title.setText(person.getFirstName());
        }

        @Override
        public void onClick(View view) { //todo switch to person or event fragment here
            if (viewType == EVENT_KEY) {
                Toast.makeText(SearchActivity.this, String.format("Event clicked: %s", event.getEventID()), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SearchActivity.this, String.format("Person clicked: %s", person.getFirstName()), Toast.LENGTH_LONG).show();
            }
        }
    }
}