package com.example.langlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

import static com.example.langlock.Misc.RUN_SERVICE;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!RUN_SERVICE){
            return;
        }

        Intent serviceIntent = new Intent(context, LockScreenService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
    }
}
