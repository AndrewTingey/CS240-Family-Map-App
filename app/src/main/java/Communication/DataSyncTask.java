package Communication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.familymapapp.cache.DataCache;

import java.io.IOException;

import Results.EventResult;
import Results.PersonIDResult;
import Results.PersonResult;

//use this class to cache the data of a user
public class DataSyncTask implements Runnable {
    private final Handler dataSyncHandler;
    private String authtoken;
    private String URL;
    private String userID;
    private static final String SUCCESS_KEY = "SuccessKey";
    private static final String FIRST_NAME_KEY = "FirstNameKey";
    private static final String LAST_NAME_KEY = "LastNameKey";


    public DataSyncTask(Handler dataSyncHandler, String serverHost, String serverPort, String userID,  String authtoken) {
        this.dataSyncHandler = dataSyncHandler;
        this.authtoken = authtoken;
        this.userID = userID;
        this.URL = "http://" + serverHost + ":" + serverPort; //use /event, /user and /user/ownID
    }

    @Override
    public void run() {
        try {
            ServerProxy serverProxy = new ServerProxy(URL + "/event", "GET", new EventResult((String) null, false));
            serverProxy.setAuthtoken(authtoken);
            EventResult eventResult = (EventResult) serverProxy.contactServer(null);

            serverProxy.setUrlString(URL + "/person");
            serverProxy.setResult(new PersonResult((String) null, false));
            PersonResult personResult = (PersonResult) serverProxy.contactServer(null);

            serverProxy.setUrlString(URL + "/person/" + userID);
            serverProxy.setResult(new PersonIDResult(null, false));
            PersonIDResult personIDResult = (PersonIDResult) serverProxy.contactServer(null);

            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();

            messageBundle.putBoolean(SUCCESS_KEY, (personResult.isSuccess() & eventResult.isSuccess()));
            messageBundle.putString(FIRST_NAME_KEY, personIDResult.getFirstName());
            messageBundle.putString(LAST_NAME_KEY, personIDResult.getLastName());

            DataCache dataCache = DataCache.getInstance();
            dataCache.loadData(eventResult.getData(), personResult.getData(), userID, authtoken);

            message.setData(messageBundle);
            dataSyncHandler.sendMessage(message);
        } catch (IOException e) {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            messageBundle.putBoolean(SUCCESS_KEY, false);
            messageBundle.putString(FIRST_NAME_KEY, e.getMessage());
            message.setData(messageBundle);
            dataSyncHandler.sendMessage(message);
        }
    }
}
