package com.example.familymapapp;
import com.example.familymapapp.cache.DataCache;

import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import Communication.ServerProxy;
import Model.Event;
import Model.Person;
import Requests.LoginRequest;
import Results.EventResult;
import Results.LoginResult;
import Results.PersonResult;

import com.example.familymapapp.UserInterface.LoginFragment;

public class DataCacheTest {

    /*
    need tests for:
    ● Calculates family relationships (i.e., spouses, parents, children)
    ● Filters events according to the current filter settings
    ● Chronologically sorts a person’s individual events (birth first, death last, etc.)
    ● Correctly searches for people and events (for your Search Activity)
     */
    String url = "http://localhost:8080";
    String authtoken = "64eedb86-27bb-446f-8296-3a98f7086c29"; //patrick spencers
    String username = "patrick";
    String password = "spencer";
    DataCache data;
    private final Set<Person> expectedFatherSideMales = new HashSet<>();
    private final Set<Person> expectedFatherSideFemales = new HashSet<>();
    private final Set<Person> expectedMotherSideMales = new HashSet<>();
    private final Set<Person> expectedMotherSideFemales = new HashSet<>();

    @Before
    public void setExpectedData() {
        Person patrickSpencer = new Person("Patrick_Spencer",	"patrick",	"Patrick",	"Spencer",	"m",	"Happy_Birthday",	"Golden_Boy", null);
        Person patrickWilson = new Person("Happy_Birthday",	"patrick",	"Patrick",	"Wilson",	"m"	, null, null,		"Golden_Boy");
        Person spencerSeeger = new Person("Golden_Boy",	"patrick",	"Spencer",	"Seeger"	,"f"	, null, null,		"Happy_Birthday");

        expectedMotherSideFemales.add(spencerSeeger);
        expectedFatherSideMales.add(patrickWilson);

        System.out.println("Expected data set");
    }
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
    public void familyRelations () {
        DataCache dataCache = DataCache.getInstance();
        assertNotNull(dataCache);

        Set<Person> fatherSideFemales = dataCache.getFatherSideFemales();
        assertNotNull(fatherSideFemales);
        assertEquals(expectedFatherSideFemales, fatherSideFemales);

        Set<Person> motherSideFemales = dataCache.getMotherSideFemales();
        assertNotNull(motherSideFemales);
        assertEquals(expectedMotherSideFemales, motherSideFemales);

        Set<Person> fatherSideMales = dataCache.getFatherSideMales();
        assertNotNull(fatherSideMales);
        assertEquals(expectedFatherSideMales, fatherSideMales);

        Set<Person> motherSideMales = dataCache.getMotherSideMales();
        assertNotNull(motherSideMales);
        assertEquals(expectedMotherSideMales, motherSideMales);
    }
}