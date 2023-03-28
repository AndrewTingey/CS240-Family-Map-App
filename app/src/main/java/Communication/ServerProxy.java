package Communication; //also LoginTask, RegisterTask, DataSyncTask go in this package

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;
import java.util.stream.Collectors;

import Results.*;
import Requests.*;


public class ServerProxy {
    private String urlString;
    private String requestMethod;
    private Result result;
    private String authtoken = null;
    private final String LOG_KEY = "ServerProxy";
    //private String urlString = "http://localhost:8080/user/login";//for testing here
    //private String urlString = "http://10.0.2.2:8080/user/login";//for testing on the android emulator

    public ServerProxy(String urlString, String requestMethod, Result result) {
        this.urlString = urlString;
        this.requestMethod = requestMethod;
        this.result = result;
    }
    public Result contactServer(Request request) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(5000);
            connection.setRequestMethod(requestMethod);
            if (authtoken != null) {
                connection.setDoOutput(false);
                connection.addRequestProperty("Authorization", authtoken);
            } else {
                connection.setDoOutput(true);
            }
            connection.connect();
        } catch (IOException e) {
            return new Result("Connection failed" + e.getMessage(), false);
        }

        if (request != null) {
            try (OutputStream requestBody = connection.getOutputStream()) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonString = gson.toJson(request);
                requestBody.write(jsonString.getBytes());
            } catch (IOException e) {
                Log.println(Log.ERROR, LOG_KEY, "ERROR: " + e.getMessage());
            }
        }

        String jsonResponse;
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            //process the response
            connection.getHeaderField("Content-Length");
            InputStream responseBody = connection.getInputStream();
            jsonResponse = new BufferedReader(new InputStreamReader(responseBody)).lines().collect(Collectors.joining(""));
        } else {
            //SERVER RETURNED AN HTTP ERROR
            Log.println(Log.ERROR, LOG_KEY, "Error: " + connection.getResponseMessage());
            InputStream errorStream = connection.getErrorStream();
            jsonResponse = new BufferedReader(new InputStreamReader(errorStream)).lines().collect(Collectors.joining(""));
        }
        Gson gson = new Gson();
        Result result = gson.fromJson(jsonResponse, this.result.getClass());
        return result;
    }

    public String getAuthtoken() {
        return authtoken;
    }

    public void setAuthtoken(String authtoken) {
        this.authtoken = authtoken;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
