package signon.oswego.suny.signson;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.google.glass.companion.Proto;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;





public class Main extends Activity implements GlassConnection.GlassConnectionListener {
    private static final int REQUEST_ENABLE_BT = 1;
    private RemoteMyoConnection mService;
    private StopReceiver mStopReceiver = new StopReceiver();
    private Preferences mPrefs;
    private DeviceListener mListener;
    private GlassConnection mGlass;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RemoteMyoConnection.MBinder mbinder = ((RemoteMyoConnection.MBinder)service);
            mService = mbinder.getService();

            if (mListener == null) {
                mListener = new MyoListener();
                Hub.getInstance().addListener(mListener);
            }

            mGlass = mService.getMyoRemote().getGlassDevice();
            mGlass.registerListener(Main.this);
            updateGlassStatus(mGlass.getConnectionStatus());

            String glassAddress = mPrefs.getGlassAddress();
            if (!TextUtils.isEmpty(glassAddress)) {
                mService.getMyoRemote().connectGlassDevice(glassAddress);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // setContentView(R.layout.activity_config);
       /* mMyoStatusView = (TextView) findViewById(R.id.myo_status);
        mGlassStatusView = (TextView) findViewById(R.id.glass_status);
        mScreencastView = (ImageView) findViewById(R.id.screenshot);
        mScreencastButton = (Button) findViewById(R.id.btnStartScreencast);
        mPoseView = (TextView) findViewById(R.id.pose);
        mArmView = (TextView) findViewById(R.id.arm);*/

      ///  updateScreencastState();

        mPrefs = new Preferences(this);

        Intent intent = new Intent(this, RemoteMyoConnection.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

      //  registerReceiver(mStopReceiver, new IntentFilter(RemoteMyoConnection.ACTION_STOP_MYO_GLASS));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Hub.getInstance().removeListener(mListener);
        unregisterReceiver(mStopReceiver);
        unbindService(mServiceConnection);
        mGlass.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
     //   getMenuInflater().inflate(R.menu.config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
         //   case R.id.kill_myglass:
             //   killMyGlass();
              //  return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void killMyGlass() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:com.google.glass.companion"));
        startActivity(intent);
    }




    private void updateGlassStatus(GlassConnection.ConnectionStatus connectionStatus) {
       // mGlassStatusView.setText(connectionStatus.name());
    }

    public void onChooseMyoClicked(View view) {
        if (mService == null) {
            Log.w("", "No MyoRemoveService. Can't choose Myo.");
            return;
        }

        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    public void onChooseGlassClicked(View view) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.no_devices_title)
                    .setMessage(R.string.no_devices_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }

        String[] glassNames = new String[pairedDevices.size()];
        final String[] glassAddresses = new String[pairedDevices.size()];
        int i = 0;
        for (BluetoothDevice device : pairedDevices) {
            glassNames[i] = device.getName();
            glassAddresses[i] = device.getAddress();
            i++;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_glass);
        builder.setItems(glassNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mGlass != null && mGlass.getConnectionStatus() == GlassConnection.ConnectionStatus.CONNECTED) {
                    mService.getMyoRemote().closeGlassDevice();
                }
                mService.getMyoRemote().connectGlassDevice(glassAddresses[which]);

                // Remember MAC address for next time.
                mPrefs.setGlassAddress(glassAddresses[which]);
            }
        });
        builder.show();
    }

    private class StopReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    }

    private class MyoListener extends AbstractDeviceListener {
        @Override
        public void onConnect(Myo myo, long timestamp) {
            mPrefs.setMyoAddress(myo.getMacAddress());
          //  mMyoStatusView.setText(R.string.connected);
          //  mPoseView.setText("LOCKED");
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
         //   mMyoStatusView.setText(R.string.disconnected);
          //  mArmView.setText("");
         //   mPoseView.setText("");
        }

        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            //
            if(arm == Arm.LEFT){
              //  mArmView.setText("Use in the Right Arm");
            }
          //  mArmView.setText( R.string.myo_arm_right);
        }
/*
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mArmView.setText(R.string.myo_arm_unknown);
        }*/

        @Override
        public void onPose(Myo myo, long timestamp, final Pose pose) {
           // mPoseView.setText(pose.name());
        }

        @Override
        public void onLock(Myo myo, long timestamp) {
          //  mPoseView.setText("LOCKED");
        }

        @Override
        public void onUnlock(Myo myo, long timestamp) {
         //   mPoseView.setText("UNLOCKED");
        }
    }

    @Override
    public void onConnectionStatusChanged(GlassConnection.ConnectionStatus status) {
        updateGlassStatus(status);
        if (status != GlassConnection.ConnectionStatus.DISCONNECTED) {
        //    setScreencastEnabled(false);
        }
    }

    // Called when a message from Glass is received
    public void onReceivedEnvelope(Proto.Envelope envelope){
        if (envelope.screenshot != null) {
            if (envelope.screenshot.screenshotBytesG2C != null) {
                InputStream in = new ByteArrayInputStream(envelope.screenshot.screenshotBytesG2C);
                final Bitmap bp = BitmapFactory.decodeStream(in);

                // Update the UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      //  mScreencastView.setImageBitmap(bp);
                    }
                });
            }
        }
    }
}
