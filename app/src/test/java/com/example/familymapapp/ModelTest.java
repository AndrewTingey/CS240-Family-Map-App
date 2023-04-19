package com.example.familymapapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.familymapapp.cache.DataCache;

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
    public void eventsInOrder() {
        //the correct order
        Event e1 = new Event("Sheila_Birth",	"sheila"	,"Sheila_Parker", (float) -36.1833000183105, (float) 144.966705322266,	"Australia"	,"Melbourne",	"birth"	,1970);
        Event e2 = new Event("Sheila_Marriage"	,"sheila"	,"Sheila_Parker",	34.0499992370605F	,-117.75F,	"United States",	"Los Angeles",	"marriage"	,2012);
        Event e3 = new Event("Sheila_Asteroids",	"sheila",	"Sheila_Parker",	77.4666976928711F,	-68.7667007446289F,	"Denmark",	"Qaanaaq",	"completed asteroids",	2014);
        Event e4 = new Event("Other_Asteroids",	"sheila"	,"Sheila_Parker",	74.4666976928711F,	-60.7667007446289F,	"Denmark",	"Qaanaaq",	"COMPLETED ASTEROIDS",2014);
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
        //assertEquals(e3, i.next());
        //assertEquals(e4, i.next());
    }
}
