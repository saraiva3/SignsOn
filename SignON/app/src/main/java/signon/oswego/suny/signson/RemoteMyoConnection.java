package signon.oswego.suny.signson;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.thalmic.myo.Hub;

public class RemoteMyoConnection extends Service {

    private MyoServices mMyoRemote;
    private StopReceiver mStopReceiver = new StopReceiver();

    public RemoteMyoConnection() {
    }

    public MyoServices getMyoRemote() {
        return mMyoRemote;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new MBinder();

    public class MBinder extends Binder {
        public RemoteMyoConnection getService() {
            return RemoteMyoConnection.this;
        }
    }

    private class StopReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            stopSelf();
            return;
        }

        mMyoRemote = new MyoServices(this);
        hub.addListener(mMyoRemote);

        Preferences prefs = new Preferences(this);
        String address = prefs.getMyoAddress();
        if (!TextUtils.isEmpty(address)) {
            // Will do nothing if already paired to Myo at given address.
            Hub.getInstance().attachByMacAddress(address);
        }
        registerReceiver(mStopReceiver, new IntentFilter("ACTION_STOP_MYO_GLASS"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Hub.getInstance().removeListener(mMyoRemote);
        mMyoRemote.shutdown();
        unregisterReceiver(mStopReceiver);
        Hub.getInstance().shutdown();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
