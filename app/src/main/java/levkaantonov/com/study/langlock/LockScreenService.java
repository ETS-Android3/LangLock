package levkaantonov.com.study.langlock;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.annotation.Nullable;
import android.util.Log;

public class LockScreenService extends Service {
    BroadcastReceiver mReceiver;

    @Override
    public void onCreate() {
        Log.e(Misc.DEBUG_TAG, "Сервис onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(Misc.DEBUG_TAG, "Сервис onStartCommand");
        KeyguardManager.KeyguardLock keyguardLock;
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock("IN");
        keyguardLock.disableKeyguard();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if(!pm.isInteractive()){
            Intent startActivityIntent = new Intent(this, LockScreenActivity.class);
            startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(startActivityIntent);
        }

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new LockScreenReceiver();
        registerReceiver(mReceiver, intentFilter);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                notificationIntent,
                0);

        Notification notification = new NotificationCompat.Builder(this, Misc.CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_service_text))
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        //do heavy work on a background thread
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(Misc.DEBUG_TAG, "Сервис onDestroy");
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
