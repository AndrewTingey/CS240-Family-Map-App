package Communication;

import android.os.Bundle;
import android.os.Message;
import android.os.Handler;

import java.io.IOException;
//import java.util.logging.Handler;

import Requests.LoginRequest;
import Results.EventResult;
import Results.LoginResult;
import Results.Result;

public class LoginTask implements Runnable{
    private final Handler loginButtonHandler;
    private String username;
    private String password;
    private String URL;
    private static final String SUCCESS_KEY = "SuccessKey";
    private static final String AUTHTOKEN_KEY = "AuthtokenKey";
    private static final String PERSONID_KEY = "PersonIDKey";
    private static final String MESSAGE_KEY = "MessageKey";


    public LoginTask(Handler loginButtonHandler, String serverHost, String serverPort, String username, String password) {
        this.loginButtonHandler = loginButtonHandler;
        this.username = username;
        this.password = password;

        this.URL = "http://" + serverHost + ":" + serverPort + "/user/login";
    }

    public void packAndSendMessage (LoginResult r) {
        Message message = Message.obtain();
        Bundle messageBundle = new Bundle();
        messageBundle.putBoolean(SUCCESS_KEY, r.isSuccess());
        messageBundle.putString(AUTHTOKEN_KEY, r.getAuthtoken());
        messageBundle.putString(PERSONID_KEY, r.getPersonID());
        messageBundle.putString(MESSAGE_KEY, r.getMessage());
        message.setData(messageBundle);
        loginButtonHandler.sendMessage(message);
    }

    @Override
    public void run() {
        try {
            LoginRequest request = new LoginRequest(username, password);
            LoginResult result = (LoginResult) new ServerProxy(URL, "POST", new LoginResult(null, false)).contactServer(request);

            packAndSendMessage(result);
        } catch (IOException e) {
            Result result = new Result("Error contacting server: " + e.getMessage(), false);
            packAndSendMessage((LoginResult) result);
        }
    }
}
