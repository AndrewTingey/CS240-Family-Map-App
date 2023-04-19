package com.example.familymapapp.cache;

import android.util.Log;

import java.util.*;

import Model.*;

public class DataCache {
    private static String BIRTH_STRING = "birth";
    private static String DEATH_STRING = "death";
    private static String LOG_KEY = "DataCache";
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
            int diffInYears = event.getYear() - t1.getYear();

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
        //fathers side
        String currentPersonID = user.getFatherID();
        if (currentPersonID != null) {
            fatherSideMales.add(peopleByID.get(currentPersonID));
            List<Person> family = getChildrenByParentID(currentPersonID);
            if (family == null) {
                family = new ArrayList<>();
            }
            family.add(peopleByID.get(userID));
            childrenByParentID.put(currentPersonID, family);
            setFatherSide(currentPersonID);
        }

        //mothers side
        currentPersonID = user.getMotherID();
        if (currentPersonID != null) {
            motherSideFemales.add(peopleByID.get(currentPersonID));
            List<Person> family = getChildrenByParentID(currentPersonID);
            if (family == null) {
                family = new ArrayList<>();
            }
            family.add(peopleByID.get(userID));
            childrenByParentID.put(currentPersonID, family);
            setMotherSide(currentPersonID);
        }

        immediateFamilyFemales.add(peopleByID.get(user.getMotherID()));
        immediateFamilyMales.add(peopleByID.get(user.getFatherID()));
        List<Person> kidsList = childrenByParentID.get(user.getPersonID());
        if (kidsList != null) { //For the hypothetical user that has children... Im not even sure how
            for (Person kid : kidsList) {
                if (kid.getGender().equalsIgnoreCase("M")) { immediateFamilyMales.add(kid); }
                if (kid.getGender().equalsIgnoreCase("F")) { immediateFamilyFemales.add(kid); }
            }
        }

        assert (peopleByID.get(userID) != null);
    }

    private void setFatherSide (String currentPersonID) {
        if (currentPersonID == null) { return; }
        Person currentPerson = peopleByID.get(currentPersonID);

        List<Person> kidsList = childrenByParentID.get(currentPerson.getFatherID());
        if (kidsList == null) { kidsList = new ArrayList<>(); }
        kidsList.add(currentPerson);
        childrenByParentID.put(currentPerson.getFatherID(), kidsList);

        String fatherID = currentPerson.getFatherID();
        if (fatherID != null) {
            fatherSideMales.add(peopleByID.get(currentPerson.getFatherID()));
            setFatherSide(currentPerson.getFatherID());
        }

        kidsList = childrenByParentID.get(currentPerson.getMotherID());
        if (kidsList == null) { kidsList = new ArrayList<>(); }
        kidsList.add(currentPerson);
        childrenByParentID.put(currentPerson.getMotherID(), kidsList);

        String motherID = currentPerson.getMotherID();
        if (motherID != null) {
            fatherSideFemales.add(peopleByID.get(currentPerson.getMotherID()));
            setFatherSide(currentPerson.getMotherID());
        }
    }

    private void setMotherSide (String currentPersonID) {
        if (currentPersonID == null) { return; }
        Person currentPerson = peopleByID.get(currentPersonID);

        String fatherID = currentPerson.getFatherID();

        if (fatherID != null) {
            List<Person> kidsList = childrenByParentID.get(currentPerson.getFatherID());
            if (kidsList == null) { kidsList = new ArrayList<>(); }
            kidsList.add(currentPerson);
            childrenByParentID.put(currentPerson.getFatherID(), kidsList);

            motherSideMales.add(peopleByID.get(fatherID));
            setMotherSide(currentPerson.getFatherID());
        }

        String motherID = currentPerson.getMotherID();
        if (motherID != null) {
            List<Person> kidsList = childrenByParentID.get(currentPerson.getMotherID());
            if (kidsList == null) { kidsList = new ArrayList<>(); }
            kidsList.add(currentPerson);
            childrenByParentID.put(currentPerson.getMotherID(), kidsList);

            motherSideFemales.add(peopleByID.get(motherID));
            setMotherSide(currentPerson.getMotherID());
        }
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

    public List<Person> getChildrenByParentID(String parentID) {
        return childrenByParentID.get(parentID);
    }

    public SortedSet<Event> getEventsByPersonID(String personID) {
        return eventsByPersonID.get(personID);
    }

    private boolean isEventRelated(Set<Person> people, Event event) {
        String associatedPerson = event.getPersonID();
        for (Person person : people) {
            if (Objects.equals(person.getPersonID(), associatedPerson)) {
                return true;
            }
        }
        return false;
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

    public List<Person> searchPerson(String searchString) {
        searchString = searchString.toLowerCase();
        List<Person> toReturn = new ArrayList<>();
        Iterator i = peopleByID.values().iterator();
        while(i.hasNext()) {
            Person person = (Person) i.next();
            //Log.println(Log.INFO, LOG_KEY, "Person iterated: " + person.getFirstName());
            if (person.getFirstName().toLowerCase().contains(searchString)) {
                toReturn.add(person);
            } else if (person.getLastName().toLowerCase().contains(searchString)) {
                toReturn.add(person);
            }
        }
        return toReturn;
    }
    public List<Event> searchEvent(String searchString) {
        searchString = searchString.toLowerCase();
        List<Event> toReturn = new ArrayList<>();
        Iterator i = eventByID.values().iterator();
        while(i.hasNext()) {
            Event event = (Event) i.next();
            if (isInFilters(event)) {
                if (event.getCity().toLowerCase().contains(searchString)) {
                    toReturn.add(event);
                } else if (event.getCountry().toLowerCase().contains(searchString)) {
                    toReturn.add(event);
                } else if (event.getEventType().toLowerCase().contains(searchString)) {
                    toReturn.add(event);
                }else {
                    Integer year = new Integer(event.getYear());
                    if (year.toString().contains(searchString)) {
                        toReturn.add(event);
                    }
                }
            }
        }
        return toReturn;
    }

    public boolean isInFilters(Event event) {
        SettingsCache settings = SettingsCache.getInstance();

        if (settings.isMaleEvents) {
            if (settings.isFatherSide) {
                if (isEventRelated(fatherSideMales, event)) {
                    return true;
                }
            }
            if (settings.isMotherSide) {
                if (isEventRelated(motherSideMales, event)) {
                    return true;
                }
            }
        }
        if (settings.isFemaleEvents) {
            if (settings.isFatherSide) {
                if (isEventRelated(fatherSideFemales, event)) {
                    return true;
                }
            }
            if (settings.isMotherSide) {
                if (isEventRelated(motherSideFemales, event)) {
                    return true;
                }
            }
        }
        //todo might return false on user and spouse
        if (settings.isFemaleEvents) {
            if (DataCache.getInstance().getPeopleByID(event.getPersonID()).getGender().equalsIgnoreCase("F")) {
                return true;
            }
        }
        if (settings.isMaleEvents) {
            if (DataCache.getInstance().getPeopleByID(event.getPersonID()).getGender().equalsIgnoreCase("M")) {
                return true;
            }
        }

        return false;
    }

    public void logout() {
        instance = null;
        authtoken = null;
    }

}
