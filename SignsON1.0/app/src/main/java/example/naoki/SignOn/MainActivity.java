package example.naoki.SignOn;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import android.widget.TextView;
import android.widget.Toast;

import com.echo.holographlibrary.LineGraph;
import com.google.glass.companion.Proto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;

import example.naoki.ble_myo.R;


public class MainActivity extends ActionBarActivity implements BluetoothAdapter.LeScanCallback, GlassDevice.GlassConnectionListener {
    public static final int MENU_LIST = 0;
    public static final int MENU_BYE = 2;
    public static final int MENU_OPTIONS = 1;
    public static final int MENU_HELP = 3;
    public static final char letters[] = { 'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','X','Z','W','Y'};
    private static final long SCAN_PERIOD = 6000;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LANGUAGE = 1;
    private static final String TAG = "BLE_Myo";

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt    mBluetoothGatt;
    private TextView         emgDataText;
    private TextView         gestureText;

    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();
    private AppPrefs mPrefs;
    private String deviceName;
    private GlassDevice mGlass;
    private GestureSaveModel    saveModel;
    private GestureSaveMethod   saveMethod;
    private GestureDetectModel  detectModel;
    private GestureDetectMethod detectMethod;
    private TextView text;
    private LineGraph graph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graph = (LineGraph) findViewById(R.id.holo_graph_view);
        text = (TextView) findViewById(R.id.textView);
        mPrefs = new AppPrefs(this);
        emgDataText = (TextView)findViewById(R.id.emgDataTextView);
        gestureText = (TextView)findViewById(R.id.gestureTextView);
        mHandler = new Handler();


        startNopModel();


        mGlass = new GlassDevice();
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(ListActivity.TAG);

        if (deviceName != null) {

            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.stopLeScan(MainActivity.this);
                    }
                }, SCAN_PERIOD);
                mBluetoothAdapter.startLeScan(this);
            }
        }
        //bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_LIST, 0, "Find Myo");
        menu.add(0, MENU_OPTIONS, 0, "Language Options");
        menu.add(0, MENU_BYE, 0, "Close Connections");
        menu.add(0, MENU_HELP, 0, "Help");
        return true;
    }

    @Override
    public void onStop(){
        super.onStop();
        this.closeBLEGatt();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case MENU_LIST:
                Intent intent = new Intent(this,ListActivity.class);
                startActivity(intent);
                return true;

            case MENU_BYE:
                closeBLEGatt();
                mGlass.closeGlassDevice();
                Toast.makeText(getApplicationContext(), "Closing all Connections", Toast.LENGTH_SHORT).show();
                startNopModel();
                return true;
            case MENU_OPTIONS:
                Intent intent2 = new Intent(this,LanguageOptions.class);
                startActivityForResult(intent2, REQUEST_LANGUAGE);
                return true;
            case MENU_HELP:
                Intent intent3 = new Intent(this,Help.class);
                intent3.putExtra("GlassAdrres", mPrefs.getGlassAddress());
                intent3.putExtra("MyoAddress", mPrefs.getMyoAddress());
                startActivity(intent3);
                return true;

        }
        return false;
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (deviceName.equals(device.getName())) {
            mBluetoothAdapter.stopLeScan(this);

            HashMap<String,View> views = new HashMap<String,View>();
            views.put("graph",graph);


            mMyoCallback = new MyoGattCallback(mHandler, emgDataText, views);
            mBluetoothGatt = device.connectGatt(this, false, mMyoCallback);
            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
        }
    }

    public void onClickTest(View v) {
       mMyoCallback.setMyoControlCommand(commandList.sendVibration3());

    }

    public void onClickEMG(View v) {
        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendEmgOnly())) {
            Log.d(TAG,"False EMG");
        } else {
            saveMethod  = new GestureSaveMethod();
            if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
                gestureText.setText("Detect Ready");
            } else {
                gestureText.setText("Teach me the letters in Order");
            }
        }
    }



    public void onClickSave(View v) {
      //  mGlass.postMessage("Envie");
       // Log.d("connection","a"+mGlass.getConnectionStatus());
        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Ready ||
                saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
            saveModel   = new GestureSaveModel(saveMethod);
            startSaveModel();
        } else if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Not_Saved) {
            startSaveModel();
        }
        saveMethod.setState(GestureSaveMethod.SaveState.Now_Saving);

        gestureText.setText("Saving "+letters[saveMethod.getGestureCounter()]);
    }

    public void onClickDetect(View v) {
        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
           // gestureText.setText("Let's Go !!");
            detectMethod = new GestureDetectMethod(saveMethod.getCompareDataList());
            detectModel = new GestureDetectModel(detectMethod);
            startDetectModel();
        }
    }

    public void closeBLEGatt() {
        if (mBluetoothGatt == null) {
            return;
        }
        mMyoCallback.stopCallback();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void startSaveModel() {
        IGestureDetectModel model = saveModel;
        model.setAction(new GestureDetectSendResultAction(this));
        GestureDetectModelManager.setCurrentModel(model);
    }

    public void startDetectModel() {
        IGestureDetectModel model = detectModel;
        model.setAction(new GestureDetectSendResultAction(this));
        GestureDetectModelManager.setCurrentModel(model);
    }

    public void startNopModel() {
        GestureDetectModelManager.setCurrentModel(new NopModel());
    }

    public void setGestureText(final String message) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                gestureText.setText(message);
            }
        });
    }
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGlass = mGlass.getGlassDevice();
            mGlass.registerListener(MainActivity.this);
            updateGlassStatus(mGlass.getConnectionStatus());
            String glassAddress = mPrefs.getGlassAddress();
            if (!TextUtils.isEmpty(glassAddress)) {
                mGlass.connectGlassDevice(glassAddress,text);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mGlass = null;
        }
    };

    public void onChooseGlassClicked(View view) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Device")
                    .setMessage("No device")
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
        builder.setTitle("Choose Your Glass");
        builder.setItems(glassNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                /*if (mGlass != null && mGlass.getConnectionStatus() == GlassDevice.ConnectionStatus.CONNECTED) {
                    mGlass.closeGlassDevice();
                }*/
                //text.setText(glassAddresses[which]);
                mGlass.connectGlassDevice(glassAddresses[which], text);

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LANGUAGE && resultCode == RESULT_OK) {
            String result=data.getStringExtra("result");
            Log.d("Tag",result);
            if(result.equals("pt")){

            }

        }

        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(MainActivity.this);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(this);
        }
    }
    private void updateGlassStatus(GlassDevice.ConnectionStatus connectionStatus) {
        text.setText(connectionStatus.name());
    }
    @Override
    public void onConnectionStatusChanged(GlassDevice.ConnectionStatus status) {
        updateGlassStatus(status);

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

                    }
                });
            }
        }
    }
}

