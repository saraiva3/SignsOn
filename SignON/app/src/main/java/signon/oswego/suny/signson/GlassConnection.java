package signon.oswego.suny.signson;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.google.glass.companion.CompanionMessagingUtil;
import com.google.glass.companion.GlassProtocol;
import com.google.glass.companion.Proto;

import com.glass.utilFactory.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlassConnection {
    private static final String TAG = "GlassDevice";

    private static final UUID SECURE_UUID = UUID.fromString("F15CC914-E4BC-45CE-9930-CB7695385850");
    private BluetoothSocket mSocket;

    private final ExecutorService mWriteThread = Executors.newSingleThreadExecutor();
    private final Object STREAM_WRITE_LOCK = new Object();
    private final List<GlassConnectionListener> mListeners = new ArrayList<GlassConnectionListener>();

    private Handler mHandler = new Handler();

    private ConnectionStatus mConnectionStatus = ConnectionStatus.DISCONNECTED;

    public enum ConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        CONNECTION_ERROR
    }

    public interface GlassConnectionListener {

        public void onConnectionStatusChanged(ConnectionStatus status);
        public void onReceivedEnvelope(Proto.Envelope envelope);
    }

    public GlassConnection() {
    }

    public ConnectionStatus getConnectionStatus() {
        return mConnectionStatus;
    }

    private void setConnectionStatus(ConnectionStatus status) {
        if (status != mConnectionStatus) {
            mConnectionStatus = status;
            synchronized (mListeners) {
                for (final GlassConnectionListener listener : mListeners) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onConnectionStatusChanged(mConnectionStatus);
                        }
                    });
                }
            }
        }
    }


    public void connect(String address) {
        setConnectionStatus(ConnectionStatus.CONNECTING);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            setConnectionStatus(ConnectionStatus.CONNECTION_ERROR);
            return;
        }

        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        try {
            mSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SECURE_UUID);
            mSocket.connect();
            setConnectionStatus(ConnectionStatus.CONNECTED);
        } catch (IOException e) {
            Log.e(TAG, "Connect Error: ", e);
            setConnectionStatus(ConnectionStatus.CONNECTION_ERROR);
            return;
        }

        // Spin up the thread to read messages from Glass
        GlassReaderThread glassReaderThread = new GlassReaderThread();
        glassReaderThread.start();
        //Sende a Message to the device ---------------------------------------------
        postMessage("Test");
        // Handshake with Glass Device
        Proto.Envelope envelope = CompanionMessagingUtil.newEnvelope();
        envelope.timezoneC2G = TimeZone.getDefault().getID();
        write(envelope);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
// ###################################### Probably not necessary for now ##################################################
     /*   Proto.Envelope envelope2 = CompanionMessagingUtil.newEnvelope();
        Proto.GlassInfoRequest glassInfoRequest = new Proto.GlassInfoRequest();
        glassInfoRequest.requestBatteryLevel = true;
        glassInfoRequest.requestStorageInfo = true;
        glassInfoRequest.requestDeviceName = true;
        glassInfoRequest.requestSoftwareVersion = true;
        envelope2.glassInfoRequestC2G = glassInfoRequest;
        write(envelope2);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public void close(){
        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e){
            Log.e(TAG, "Disconnect Error: ", e);
        }

        setConnectionStatus(ConnectionStatus.DISCONNECTED);
    }

    public void registerListener(GlassConnectionListener glassConnectionListener) {
        if (glassConnectionListener == null) {
            return;
        }
        synchronized (mListeners) {
            final int size = mListeners.size();
            for (int i = 0; i < size; i++) {
                GlassConnectionListener listener = mListeners.get(i);
                if (listener == glassConnectionListener) {
                    return;
                }
            }
            this.mListeners.add(glassConnectionListener);
        }
    }

    public void unregisterListener(GlassConnectionListener glassConnectionListener) {
        if (glassConnectionListener == null) {
            return;
        }
        synchronized (mListeners) {
            final int size = mListeners.size();
            for (int i = 0; i < size; i++) {
                GlassConnectionListener listener = mListeners.get(i);
                if (listener == glassConnectionListener) {
                    mListeners.remove(i);
                    break;
                }
            }
        }
    }

    // Native Interface commands send to Glass

    public void swipeLeft(){
        writeAsync(GlassMessage.getSwipeLeftEvents());
    }

    public void swipeRight(){
        writeAsync(GlassMessage.getSwipeRightEvents());
    }

    public void swipeDown(){
        writeAsync(GlassMessage.getSwipeDownEvents());
    }

    public void tap(){
        writeAsync(GlassMessage.getTapEvents());
    }

    public void postMessage(String text){
        writeAsync(GlassMessage.createTimelineMessage(text));
    }

    public void requestScreenshot(){
        Proto.Envelope envelope = CompanionMessagingUtil.newEnvelope();
        Proto.ScreenShot screenShot = new Proto.ScreenShot();
        screenShot.startScreenshotRequestC2G = true;
        envelope.screenshot = screenShot;
        writeAsync(envelope);
    }

    public void stopScreenshot(){
        Proto.Envelope envelope = CompanionMessagingUtil.newEnvelope();
        Proto.ScreenShot screenShot = new Proto.ScreenShot();
        screenShot.stopScreenshotRequestC2G = true;
        envelope.screenshot = screenShot;
        writeAsync(envelope);
    }

    public void write(Proto.Envelope envelope) {
        synchronized (STREAM_WRITE_LOCK) {
            try {
                if (mSocket != null) {
                    OutputStream outStream = mSocket.getOutputStream();
                    GlassProtocol.writeMessage(envelope, outStream);
                }
            } catch (IOException e) {
                Log.e(TAG,"Write Error:",e);
            }
        }
    }

    public void writeAsync(final Proto.Envelope envelope) {
        mWriteThread.execute(new Runnable() {
            @Override
            public void run() {
                write(envelope);
            }
        });
    }

    public void writeAsync(final List<Proto.Envelope> envelopes) {
        mWriteThread.execute(new Runnable() {
            @Override
            public void run() {
                for (Proto.Envelope envelope : envelopes) {
                    write(envelope);
                }
            }
        });
    }

    private class GlassReaderThread extends Thread {
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        InputStream inStream = mSocket.getInputStream();
                        Proto.Envelope envelope = (Proto.Envelope) GlassProtocol.readMessage(new Proto.Envelope(), inStream);
                        if (envelope.screenshot == null) {
                            Log.i(TAG,"RX'd from Glass: "+envelope.toString());
                        }
                        if (envelope != null) {
                            synchronized (mListeners) {
                                for (GlassConnectionListener listener : mListeners) {
                                    listener.onReceivedEnvelope(envelope);
                                }
                            }
                        }
                    } catch (InterruptedIOException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                Log.v(TAG,"Reader Thread Finished");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
