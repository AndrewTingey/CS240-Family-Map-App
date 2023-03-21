package com.example.familymapapp.cache;

public class DataCache {
    private static DataCache instance;

    //if authtoken is null, user is not logged in
    //private Authtoken authtoken;
    //private final Map<String, Person> peopleByID = new HashMap<>();
    //private final Map<String, List<Person>> childrenByParentID
    //private final Map<String, SortedSet<Event>> eventsByPersonID
    //private final Comprator<Event> eventComparator = new Comparator<Event>() {...}
    //private final Set<String> eventTypes = new HashSet<>()

    //user
    //private Person user;
    //
    //immediate family
    //Set<Person> immediateFamilyMales
    //Set<Person> immediateFamilyFemales

    //ancestors
    //private final Set<Person> fatherSideMales
    //private final Set<Person> fatherSideFemales
    //private final Set<Person> motherSideMales
    //private final Set<Person> motherSideFemales

    //to load data
    //loadData(List<Event>, List<Person>)

    private DataCache() {}

    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }
}
