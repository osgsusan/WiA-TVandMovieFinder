package osg.susan.moviefinder.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by susanosgood on 5/2/15.
 */
public class SearchBroadcastReceiver extends BroadcastReceiver {

    private static final String INTERNET_NOT_CONNECTED = "Internet connection is not available";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) Toast.makeText(context, INTERNET_NOT_CONNECTED, Toast.LENGTH_LONG).show();
    }
}
