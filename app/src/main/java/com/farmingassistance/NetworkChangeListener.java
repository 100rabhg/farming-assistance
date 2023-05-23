package com.farmingassistance;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

public class NetworkChangeListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!isConnectedToInternet(context)){
            AlertDialog.Builder  builder =  new AlertDialog.Builder(context);
            View layout_dialog = LayoutInflater.from(context).inflate(R.layout.nointernet_dialog,null);
            builder.setView(layout_dialog);

            AppCompatButton button = layout_dialog.findViewById(R.id.btnRetry);
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCancelable(false);
            dialog.getWindow().setGravity(Gravity.CENTER);
            button.setOnClickListener(view -> {
                dialog.dismiss();
                onReceive(context,intent);
            });


        }
    }
    static  boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager!=null){
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if(info!=null){
                for (NetworkInfo networkInfo : info) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
