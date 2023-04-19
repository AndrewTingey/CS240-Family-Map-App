package com.example.familymapapp;

import static org.junit.Assert.*;

import org.junit.*;

import java.io.IOException;

import Communication.ServerProxy;
import Requests.LoginRequest;
import Requests.RegisterRequest;
import Results.EventResult;
import Results.LoginResult;
import Results.PersonResult;
import Results.RegisterResult;

public class ServerProxyTests {
    String url = "http://localhost:8080";
    String authtoken = "ef31aafe-0a8f-4e0c-a429-eb752e145b0e"; //sheila parker's
    String username = "sheila";
    String password = "parker";
    @Test
    public void loginPass() throws IOException {
        LoginRequest request = new LoginRequest(username, password);
        LoginResult loginResult = (LoginResult) new ServerProxy(url + "/user/login", "POST",  new LoginResult(null, false)).contactServer(request);
        System.out.println(loginResult.getMessage());
        assertTrue(loginResult.isSuccess());
        assertEquals(authtoken, loginResult.getAuthtoken());
    }

    @Test
    public void loginFail() throws IOException {
        String loginURL = url + "/user/login";
        LoginRequest request = new LoginRequest(username, "wrong password");
        LoginResult loginResult = (LoginResult) new ServerProxy(loginURL, "POST",  new LoginResult(null, false)).contactServer(request);
        assertFalse(loginResult.isSuccess());

        request = new LoginRequest("not a username", password);
        loginResult = (LoginResult) new ServerProxy(loginURL, "POST",  new LoginResult(null, false)).contactServer(request);
        assertFalse(loginResult.isSuccess());
    }

    @Test
    public void registerPass() throws IOException {
        String registerURL = url + "/user/register";
        RegisterRequest request = new RegisterRequest("testing_other", "pw123", "a@gmail", "TestingAndrew", "TestingTingey", "M");
        RegisterResult result = (RegisterResult) new ServerProxy(registerURL, "POST", new RegisterResult(null, false)).contactServer(request);
        //assertTrue(result.isSuccess()); //todo
    }

    @Test
    public void registerFail() throws IOException {
        String registerURL = url + "/user/register";
        //registering with username that already exists
        RegisterRequest request = new RegisterRequest("sheila", "parker", "a@gmail", "TestingAndrew", "TestingTingey", "M");
        RegisterResult result = (RegisterResult) new ServerProxy(registerURL, "POST", new RegisterResult(null, false)).contactServer(request);
        assertFalse(result.isSuccess());
    }

    @Test
    public void getPeoplePass() throws IOException {
        String personURL = url + "/person";
        PersonResult result = new PersonResult((String) null, false);
        ServerProxy serverProxy = new ServerProxy(personURL, "GET", result);
        serverProxy.setAuthtoken(authtoken);
        result = (PersonResult) serverProxy.contactServer(null);

        assertTrue(result.isSuccess());
        assertEquals(8, result.getData().size());
    }

    @Test
    public void getPeopleFail() throws IOException {
        String personURL = url + "/person";
        PersonResult result = new PersonResult((String) null, false);
        ServerProxy serverProxy = new ServerProxy(personURL, "GET", result);
        //serverProxy.setAuthtoken(""); //no authtoken should not allow user to retrieve people
        result = (PersonResult) serverProxy.contactServer(null);

        assertFalse(result.isSuccess());
    }

    @Test
    public void getEventsPass() throws IOException {
        String eventURL = url + "/event";
        EventResult result = new EventResult((String) null, false);
        ServerProxy serverProxy = new ServerProxy(eventURL, "GET", result);
        serverProxy.setAuthtoken(authtoken);
        result = (EventResult) serverProxy.contactServer(null);

        assertTrue(result.isSuccess());
        assertEquals(16, result.getData().size()); //16 events related to sheila parker
    }

    @Test
    public void getEventsFail() throws IOException {
        String eventURL = url + "/event";
        EventResult result = new EventResult((String) null, false);
        ServerProxy serverProxy = new ServerProxy(eventURL, "GET", result);
        //serverProxy.setAuthtoken(authtoken); //no authtoken should not allow user to retrieve people
        result = (EventResult) serverProxy.contactServer(null);

        assertFalse(result.isSuccess());
    }
}
