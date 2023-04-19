package com.example.familymapapp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.familymapapp.UserInterface.PersonActivity;
import com.example.familymapapp.cache.DataCache;
import com.example.familymapapp.cache.SettingsCache;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import Communication.ServerProxy;
import Model.Event;
import Model.Person;
import Requests.LoginRequest;
import Results.EventResult;
import Results.LoginResult;
import Results.PersonResult;

public class ModelTest {
    String url = "http://localhost:8080";
    String authtoken = "ef31aafe-0a8f-4e0c-a429-eb752e145b0e"; //sheila parkers
    String username = "sheila";
    String password = "parker";
    DataCache data;
    Person sheila = new Person("Sheila_Parker",	"sheila"	,"Sheila",	"Parker",	"f"	,"Blaine_McGary",	"Betty_White"	,"Davis_Hyer");
    Person blain = new Person("Blaine_McGary",	"sheila",	"Blaine",	"McGary",	"m",	"Ken_Rodham",	"Mrs_Rodham",	"Betty_White");
    Event frogs = new Event("Jones_Frog",	"sheila","Frank_Jones",	25.0666999816895F,	-76.6667022705078F,	"Bahamas",	"Nassau",	"Caught a frog",	1993);
    Event asteroids = new Event("Sheila_Asteroids",	"sheila",	"Sheila_Parker",	77.4666976928711F,	-68.7667007446289F,	"Denmark",	"Qaanaaq",	"completed asteroids"	,2014);
    Event otherAsteroids = new Event("Other_Asteroids",	"sheila",	"Sheila_Parker",	74.4666976928711F, -60.7667007446289F,	"Denmark",	"Qaanaaq",	"COMPLETED ASTEROIDS"	,2014);
    Event surfing = new Event("Mrs_Jones_Surf",	"sheila",	"Mrs_Jones",	-27.9832992553711F,	153.399993896484F,	"Australia",	"Gold Coast",	"Learned to Surf",	2000);
    Event blainBirth = new Event("Blaine_Birth",	"sheila",	"Blaine_McGary",	56.11669921875F,	101.599998474121F,	"Russia",	"Bratsk",	"birth",	1948);

    @Before
    public void setUp() throws IOException {
        data = DataCache.getInstance();

        //get people
        String personURL = url + "/person";
        PersonResult result = new PersonResult((String) null, false);
        ServerProxy serverProxy = new ServerProxy(personURL, "GET", result);
        serverProxy.setAuthtoken(authtoken);
        result = (PersonResult) serverProxy.contactServer(null);
        List<Person> people = result.getData();

        //get events
        String eventURL = url + "/event";
        EventResult eventResult = new EventResult((String) null, false);
        serverProxy = new ServerProxy(eventURL, "GET", eventResult);
        serverProxy.setAuthtoken(authtoken);
        eventResult = (EventResult) serverProxy.contactServer(null);
        List<Event> events = eventResult.getData();

        //get user personID via login
        String loginURL = url + "/user/login";
        LoginRequest request = new LoginRequest(username, password);
        LoginResult loginResult = (LoginResult) new ServerProxy(loginURL, "POST",  new LoginResult(null, false)).contactServer(request);
        String userID = loginResult.getPersonID();

        data.loadData(events, people, userID, authtoken);

        System.out.println("Data loaded");
    }
    @Test
    public void eventsInOrderPass() {
        //the correct order
        Event e1 = new Event("Sheila_Birth",	"sheila"	,"Sheila_Parker", (float) -36.1833000183105, (float) 144.966705322266,	"Australia"	,"Melbourne",	"birth"	,1970);
        Event e2 = new Event("Sheila_Marriage"	,"sheila"	,"Sheila_Parker",	34.0499992370605F	,-117.75F,	"United States",	"Los Angeles",	"marriage"	,2012);
        Event e3 = new Event("Other_Asteroids",	"sheila"	,"Sheila_Parker",	74.4666976928711F,	-60.7667007446289F,	"Denmark",	"Qaanaaq",	"COMPLETED ASTEROIDS",2014);
        Event e4 = new Event("Sheila_Asteroids",	"sheila",	"Sheila_Parker",	77.4666976928711F,	-68.7667007446289F,	"Denmark",	"Qaanaaq",	"completed asteroids",	2014);
        Event e5 = new Event("Sheila_Death",	"sheila",	"Sheila_Parker",	40.2444000244141F,	111.660797119141F,	"United States",	"Provo",	"death",	2015);

        SortedSet<Event> result = DataCache.getInstance().getEventsByPersonID("Sheila_Parker");

        Iterator<Event> i = result.iterator();
        while (i.hasNext()) {
            System.out.println(i.next().getEventID());
        }
        i = result.iterator();
        assertTrue(i.hasNext());
        assertEquals(e1, i.next());
        assertEquals(e2, i.next());
        assertEquals(e3, i.next());
        assertEquals(e4, i.next());
        assertEquals(e5, i.next());
    }
    @Test
    public void eventsInOrderFail() {
        SortedSet<Event> result = DataCache.getInstance().getEventsByPersonID("not_a_username");
        assertNull(result);
    }
    @Test
    public void searchPeopleEventsPass() {
        SettingsCache.getInstance().setSettings(true, true, true, true, true, true, true);
        //EVENTS
        List<Event> eventList = data.searchEvent("FROG");
        assertEquals(frogs, eventList.get(0));

        eventList = data.searchEvent("ast");
        Iterator<Event> i = eventList.iterator();
        assertTrue(i.hasNext());
        assertEquals(otherAsteroids, i.next());
        assertEquals(asteroids, i.next());

        //PEOPLE
        List<Person> result = data.searchPerson("sh");
        assertEquals(sheila, result.get(0));
        result = data.searchPerson("mc");
        assertEquals(blain, result.get(0));
    }
    @Test
    public void searchPeopleEventsFail() {
        SettingsCache.getInstance().setSettings(true, true, true, true, true, true, true);

        //EVENTS
        List<Event> eventList = data.searchEvent("FROGS");
        assertEquals(0, eventList.size()); //no event named frogS

        eventList = data.searchEvent("garbage");
        Iterator<Event> i = eventList.iterator();
        assertFalse(i.hasNext());
        assertEquals(0, eventList.size());

        //PEOPLE
        List<Person> result = data.searchPerson("garbage");
        assertEquals(0, result.size());
        result = data.searchPerson("wilkerson");
        assertEquals(0, result.size());
    }
    @Test
    public void filterEventsPass() {
        SettingsCache settings = SettingsCache.getInstance();
        //no female events
        settings.setSettings(false, false, false, true, true, true, false);
        List<Event> eventList = data.searchEvent("as"); //should return caught a frog, not astroids
        assertEquals(frogs, eventList.get(0));

        //no male events
        settings.setSettings(false, false, false, true, true, false, true);

        eventList = data.searchEvent("as"); //should return asteroids and surfing, not frog
        Iterator<Event> i = eventList.iterator();
        assertTrue(i.hasNext());
        assertEquals(otherAsteroids, i.next());
        assertEquals(asteroids, i.next());
        assertEquals(surfing, i.next());

        //no father side
        settings.setSettings(false, false, false, false, true, true, true);
        eventList = data.searchEvent("ss"); //should return frog, not blain birth ruSSia
        assertEquals(frogs, eventList.get(0));

        //no mother side
        settings.setSettings(false, false, false, true, false, true, true);
        eventList = data.searchEvent("ss"); //should return blain birth in russia, not frogs
        assertEquals(blainBirth, eventList.get(0));
    }
    @Test
    public void filterEventsFail() {
        SettingsCache settings = SettingsCache.getInstance();
        //all events turned off
        settings.setSettings(false, false, false, false, false, false, false);

        List<Event> eventList = data.searchEvent("o"); //should return nothing
        assertEquals(0, eventList.size());
    }
    @Test
    public void familyRelationshipPass() {
        //father
        assertEquals("Father", PersonActivity.getRelation("Sheila_Parker","Blaine_McGary"));
        //mother
        assertEquals("Mother", PersonActivity.getRelation("Sheila_Parker","Betty_White"));
        //spouse
        assertEquals("Spouse", PersonActivity.getRelation("Sheila_Parker","Davis_Hyer"));
        //son
        assertEquals("Son", PersonActivity.getRelation("Ken_Rodham","Blaine_McGary"));
        //daughter
        assertEquals("Daughter", PersonActivity.getRelation("Blaine_McGary","Sheila_Parker"));
    }

    @Test
    public void familyRelationFail() {
        //self
        assertEquals("Persons are not immediately related", PersonActivity.getRelation("Sheila_Parker","Sheila_Parker"));
        //grandpa
        assertEquals("Persons are not immediately related", PersonActivity.getRelation("Sheila_Parker","Ken_Rodham"));
        //grandson
        assertEquals("Persons are not immediately related", PersonActivity.getRelation("Ken_Rodham","Sheila_Parker"));
        //unrelated
        assertEquals("Persons are not immediately related", PersonActivity.getRelation("Ken_Rodham","Mrs_Jones"));


    }
}
