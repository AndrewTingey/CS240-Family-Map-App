package com.example.familymapapp.cache;

import java.util.*;

import Model.*;

public class DataCache {
    private static String BIRTH_STRING = "birth";
    private static String DEATH_STRING = "death";
    private static DataCache instance;
    //if authtoken is null, user is not logged in
    private String authtoken = null;
    private final Map<String, Person> peopleByID = new HashMap<>();
    private final Map<String, List<Person>> childrenByParentID = new HashMap<>();
    private final Map<String, SortedSet<Event>> eventsByPersonID = new HashMap<>();
    private final Comparator<Event> eventComparator = new Comparator<Event>() {
        @Override
        //a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
        public int compare(Event event, Event t1) {
            //todo
            //birth events first, death events last
            if (event.getEventType().equalsIgnoreCase(BIRTH_STRING)) {
                if (!t1.getEventType().equalsIgnoreCase(BIRTH_STRING)) {
                    return -1; //birth is less than non birth
                }
            } else if (t1.getEventType().equalsIgnoreCase(BIRTH_STRING)) {
                return 1; // non-birth is greater than birth
            }

            if (event.getEventType().equalsIgnoreCase(DEATH_STRING)) {
                if (!t1.getEventType().equalsIgnoreCase(DEATH_STRING)) {
                    return 1; // death is greater than non death
                }
            } else if (t1.getEventType().equalsIgnoreCase(DEATH_STRING)) {
                return -1; //non death is less than death
            }

            //compare years
            int diffInYears = t1.getYear() - event.getYear();
            if (diffInYears != 0) return diffInYears;
            else {
                //compare strings of event type
                return event.getEventType().compareTo(t1.getEventType());
            }
        } };
    private final Set<String> eventTypes = new HashSet<>();
    private final Map<String, Event> eventByID = new HashMap<>();
    private Person user;

    //immediate family
    Set<Person> immediateFamilyMales = new HashSet<>();
    Set<Person> immediateFamilyFemales = new HashSet<>();

    //ancestors
    private final Set<Person> fatherSideMales = new HashSet<>();
    private final Set<Person> fatherSideFemales = new HashSet<>();
    private final Set<Person> motherSideMales = new HashSet<>();
    private final Set<Person> motherSideFemales = new HashSet<>();

    //to load data
    public void loadData(List<Event> events, List<Person> people, String userID, String authtoken) {
        instance = getInstance();
        this.authtoken = authtoken;
        for (Person person : people) {
            peopleByID.put(person.getPersonID(), person);
        }
        this.user = peopleByID.get(userID);
        for (Event event : events) {
            eventByID.put(event.getEventID(), event);
            SortedSet<Event> eventList = eventsByPersonID.get(event.getPersonID());
            if (eventList == null) {
                eventList = new TreeSet<>(eventComparator);
            }
            eventList.add(event);
            eventsByPersonID.put(event.getPersonID(), eventList);
            eventTypes.add(event.getEventType());
        }
        String currentPersonID = user.getFatherID();
        fatherSideMales.add(peopleByID.get(currentPersonID));
        setFatherSide(currentPersonID);

        currentPersonID = user.getMotherID();
        motherSideFemales.add(peopleByID.get(currentPersonID));
        setMotherSide(currentPersonID);

        immediateFamilyFemales.add(peopleByID.get(user.getMotherID()));
        immediateFamilyMales.add(peopleByID.get(user.getFatherID()));
        List<Person> kidsList = childrenByParentID.get(user.getPersonID());
        if (kidsList != null) { //For the hypothetical user that has children... Im not even sure how
            for (Person kid : kidsList) {
                if (kid.getGender().equalsIgnoreCase("M")) { immediateFamilyMales.add(kid); }
                if (kid.getGender().equalsIgnoreCase("F")) { immediateFamilyFemales.add(kid); }
            }
        }
    }

    private void setFatherSide (String currentPersonID) {
        if (currentPersonID == null) { return; }
        Person currentPerson = peopleByID.get(currentPersonID);

        List<Person> kidsList = childrenByParentID.get(currentPerson.getFatherID());
        if (kidsList == null) { kidsList = new ArrayList<>(); }
        kidsList.add(currentPerson);
        childrenByParentID.put(currentPerson.getFatherID(), kidsList);

        fatherSideMales.add(peopleByID.get(currentPerson.getFatherID()));
        setFatherSide(currentPerson.getFatherID());

        kidsList = childrenByParentID.get(currentPerson.getMotherID());
        if (kidsList == null) { kidsList = new ArrayList<>(); }
        kidsList.add(currentPerson);
        childrenByParentID.put(currentPerson.getMotherID(), kidsList);

        fatherSideFemales.add(peopleByID.get(currentPerson.getMotherID()));
        setFatherSide(currentPerson.getMotherID());
    }

    private void setMotherSide (String currentPersonID) {
        if (currentPersonID == null) { return; }
        Person currentPerson = peopleByID.get(currentPersonID);

        List<Person> kidsList = childrenByParentID.get(currentPerson.getFatherID());
        if (kidsList == null) { kidsList = new ArrayList<>(); }
        kidsList.add(currentPerson);
        childrenByParentID.put(currentPerson.getFatherID(), kidsList);

        motherSideMales.add(peopleByID.get(currentPerson.getFatherID()));
        setMotherSide(currentPerson.getFatherID());

        kidsList = childrenByParentID.get(currentPerson.getMotherID());
        if (kidsList == null) { kidsList = new ArrayList<>(); }
        kidsList.add(currentPerson);
        childrenByParentID.put(currentPerson.getMotherID(), kidsList);

        motherSideFemales.add(peopleByID.get(currentPerson.getMotherID()));
        setMotherSide(currentPerson.getMotherID());
    }

    private DataCache() {}

    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }


    public static void setInstance(DataCache instance) {
        DataCache.instance = instance;
    }

    public String getAuthtoken() {
        return authtoken;
    }

    public void setAuthtoken(String authtoken) {
        this.authtoken = authtoken;
    }

    public Person getPeopleByID(String personID) {
        return peopleByID.get(personID);
    }

    public Map<String, List<Person>> getChildrenByParentID() {
        return childrenByParentID;
    }

    public SortedSet<Event> getEventsByPersonID(String personID) {
        SortedSet<Event> toReturn = eventsByPersonID.get(personID);
        return eventsByPersonID.get(personID);
    }

    public Comparator<Event> getEventComparator() {
        return eventComparator;
    }

    public Set<String> getEventTypes() {
        return eventTypes;
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public Set<Person> getImmediateFamilyMales() {
        return immediateFamilyMales;
    }

    public void setImmediateFamilyMales(Set<Person> immediateFamilyMales) {
        this.immediateFamilyMales = immediateFamilyMales;
    }

    public Set<Person> getImmediateFamilyFemales() {
        return immediateFamilyFemales;
    }

    public void setImmediateFamilyFemales(Set<Person> immediateFamilyFemales) {
        this.immediateFamilyFemales = immediateFamilyFemales;
    }

    public Set<Person> getFatherSideMales() {
        return fatherSideMales;
    }

    public Set<Person> getFatherSideFemales() {
        return fatherSideFemales;
    }

    public Set<Person> getMotherSideMales() {
        return motherSideMales;
    }

    public Set<Person> getMotherSideFemales() {
        return motherSideFemales;
    }

    public Event getEventByID(String eventID) {
        return eventByID.get(eventID);
    }

    public Person getSpouseByID(String personID) {
        Person person = getPeopleByID(personID);
        return getPeopleByID(person.getSpouseID());
    }
}
