package Communication;

import android.os.Bundle;
import android.os.Message;
import android.os.Handler;

import java.io.IOException;

import Requests.LoginRequest;
import Requests.RegisterRequest;
import Results.LoginResult;
import Results.RegisterResult;
import Results.Result;

public class RegisterTask implements Runnable {
    private final Handler registerButtonHandler;
    private RegisterRequest request;
    private String URL;
    private static final String SUCCESS_KEY = "SuccessKey";
    private static final String MESSAGE_KEY = "MessageKey";
    private static final String AUTHTOKEN_KEY = "AuthtokenKey";

    public RegisterTask(Handler registerButtonHandler, String serverHost, String serverPort, RegisterRequest request) {
        this.registerButtonHandler = registerButtonHandler;
        this.request = request;

        this.URL = "http://" + serverHost + ":" + serverPort + "/user/register";
    }

    public void packAndSendMessage (RegisterResult r) {
        Message message = Message.obtain();
        Bundle messageBundle = new Bundle();
        messageBundle.putBoolean(SUCCESS_KEY, r.isSuccess());
        messageBundle.putString(MESSAGE_KEY, r.getMessage());
        messageBundle.putString(AUTHTOKEN_KEY, r.getAuthtoken());

        message.setData(messageBundle);
        registerButtonHandler.sendMessage(message);
    }

    @Override
    public void run() {
        try {
            RegisterResult result = (RegisterResult) new ServerProxy(URL, "POST", new RegisterResult(null, false)).contactServer(request);
            packAndSendMessage(result);
        } catch (IOException e) {
            Result result = new Result("Error contacting server: " + e.getMessage(), false);
            packAndSendMessage((RegisterResult) result);
        }
    }
}
