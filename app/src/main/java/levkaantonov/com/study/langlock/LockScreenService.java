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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class LockScreenService extends Service {
    BroadcastReceiver mReceiver;
    private Boolean mReceiverIsRegistered = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new LockScreenReceiver();
        mReceiverIsRegistered = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!mReceiverIsRegistered){
            unregisterReceiver(mReceiver);
            mReceiverIsRegistered = false;
        }
        KeyguardManager.KeyguardLock keyguardLock;
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock("IN");
        keyguardLock.disableKeyguard();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if(!pm.isInteractive()){
            Intent startActivityIntent = new Intent(this, LockScreenActivity.class);
            startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(startActivityIntent);
            return START_STICKY;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);

        registerReceiver(mReceiver, intentFilter);
        mReceiverIsRegistered = true;

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

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        mReceiverIsRegistered = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
