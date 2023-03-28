package com.example.familymapapp;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;

import Communication.ServerProxy;
import Requests.LoginRequest;
import Results.LoginResult;

public class ServerProxyTests {
    @Test
    public void loginPass() throws IOException {
        LoginRequest request = new LoginRequest("testing_andrew", "pw123");
        LoginResult loginResult = (LoginResult) new ServerProxy("http://localhost:8080/user/login", "POST",  new LoginResult(null, false)).contactServer(request);
        System.out.println(loginResult.getMessage());
        assertTrue(loginResult.isSuccess());
        System.out.println("AUTHTOKEN: " + loginResult.getAuthtoken());
    }
}
