package com.myo.EMGHack;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.echo.holographlibrary.LineGraph;

import java.util.HashMap;

import signon.oswego.suny.signson.GestureDetectMethod;
import signon.oswego.suny.signson.GestureDetectModel;
import signon.oswego.suny.signson.GestureDetectModelManager;
import signon.oswego.suny.signson.GestureSaveMethod;
import signon.oswego.suny.signson.GestureSaveModel;
import signon.oswego.suny.signson.MyoGattCallback;
import signon.oswego.suny.signson.NopModel;

public class MainActivity extends ActionBarActivity implements BluetoothAdapter.LeScanCallback {
    public static final int MENU_LIST = 0;
    public static final int MENU_BYE = 1;

    /** Device Scanning Time (ms) */
    private static final long SCAN_PERIOD = 5000;

    /** Intent code for requesting Bluetooth enable */
    private static final int REQUEST_ENABLE_BT = 1;

    private static final String TAG = "BLE_Myo";

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private TextView emgDataText;


    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();

    private String deviceName;

    private GestureSaveModel saveModel;
    private GestureSaveMethod saveMethod;
    private GestureDetectModel detectModel;
    private GestureDetectMethod detectMethod;

    private LineGraph graph;
    private Button graphButton1;
    private Button graphButton2;
    private Button graphButton3;
    private Button graphButton4;
    private Button graphButton5;
    private Button graphButton6;
    private Button graphButton7;
    private Button graphButton8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mHandler = new Handler();
        startNopModel();
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();


        // --------------------Catch when bluettoth is accepted ----------------------------
       // deviceName = intent.getStringExtra(ListActivity.TAG);
                //
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.stopLeScan(MainActivity.this);
                    }
                }, SCAN_PERIOD);
                mBluetoothAdapter.startLeScan(this);


    }


    // on STOP
   // this.closeBLEGatt();


    /** Define of BLE Callback */
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (deviceName.equals(device.getName())) {
            mBluetoothAdapter.stopLeScan(this);
            // Trying to connect GATT
            HashMap<String,View> views = new HashMap<String,View>();
            //put GraphView
            views.put("graph",graph);
            //put Button1?8
            mMyoCallback = new MyoGattCallback(mHandler, emgDataText, views);
            mBluetoothGatt = device.connectGatt(this, false, mMyoCallback);
            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
        }
    }


    //-------------------------ONCLICK SAVE AND DETECT
/*
    public void onClickSave(View v) {
        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Ready ||
                saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
            saveModel   = new GestureSaveModel(saveMethod);
            startSaveModel();
        } else if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Not_Saved) {
            startSaveModel();
        }
        saveMethod.setState(GestureSaveMethod.SaveState.Now_Saving);
        gestureText.setText("Saving ; " + (saveMethod.getGestureCounter() + 1));
    }

    public void onClickDetect(View v) {
        if (saveMethod.getSaveState() == GestureSaveMethod.SaveState.Have_Saved) {
            gestureText.setText("Let's Go !!");
            detectMethod = new GestureDetectMethod(saveMethod.getCompareDataList());
            detectModel = new GestureDetectModel(detectMethod);
            startDetectModel();
        }
    }
    */
    /* ---------------------------GOOD BYE
    /*

    public void closeBLEGatt() {
        if (mBluetoothGatt == null) {
            return;
        }
        mMyoCallback.stopCallback();
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }*/

    // ----------------- SAVE and DETECTIOn
    /*
    public void startSaveModel() {
        IGestureDetectModel model = saveModel;
        model.setAction(new GestureDetectSendResultAction(this));
        GestureDetectModelManager.setCurrentModel(model);
    }

    public void startDetectModel() {
        IGestureDetectModel model = detectModel;
        model.setAction(new GestureDetectSendResultAction(this));
        GestureDetectModelManager.setCurrentModel(model);
    }*/

    public void startNopModel() {
        GestureDetectModelManager.setCurrentModel(new NopModel());
    }

/*
    public void setGestureText(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                gestureText.setText(message);
            }
        });
    }*/

    // --------- OLD BLUETOTH GET Acessivel -----------
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(MainActivity.this);
                }
            }, SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(this);
        }
    }*/
}

