package example.naoki.SignOn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.google.glass.companion.CompanionMessagingUtil;
import com.google.glass.companion.GlassProtocol;
import com.google.glass.companion.Proto;

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


public final class GlassDevice {

    private static final String TAG = "GlassDevice";
    private static final UUID SECURE_UUID = UUID.fromString("F15CC914-E4BC-45CE-9930-CB7695385850");
    private BluetoothSocket mSocket;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
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
    public GlassDevice getGlassDevice() {
        return this;
    }

    public void connectGlassDevice(final String address,final TextView text) {
       new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
//
               connect(address, text);
                return null;
            }
        }.executeOnExecutor(mExecutor);
    }
    

    public void closeGlassDevice() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                close();
                return null;
            }
        }.executeOnExecutor(mExecutor);
    }
    public interface GlassConnectionListener {
        /**
         * Will be called on the main UI thread.
         */
        public void onConnectionStatusChanged(ConnectionStatus status);

        /**
         * Called from a background thread.
         */
        public void onReceivedEnvelope(Proto.Envelope envelope);
    }

    public GlassDevice() {
    }

    public ConnectionStatus getConnectionStatus() {
        return mConnectionStatus;
    }

    private void setConnectionStatus(ConnectionStatus status) {
        if (status != mConnectionStatus) {
            mConnectionStatus = status;
            synchronized (mListeners) {
                for (final GlassConnectionListener listener : mListeners) {
                    // Posting the callback to guarantee it is on the main thread.
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

    /**
     * Shouldn't be called on the main thread.
     *
     * @param address MAC address of a Glass device to connect to. Must be paired already.
     */
    public void connect(String address,TextView text) {

        text.setText("Connecting");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            text.setText("Error");

            return;
        }

        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        try {
          text.setText("Connected");
            mSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SECURE_UUID);
            mSocket.connect();
            setConnectionStatus(ConnectionStatus.CONNECTED);
            GlassMessagingUtil.createTimelineMessage("");

        } catch (IOException e) {

            setConnectionStatus(ConnectionStatus.CONNECTION_ERROR);
            return;
        }

        // Spin up the thread to read messages from Glass
        GlassReaderThread glassReaderThread = new GlassReaderThread();
        glassReaderThread.start();

        // Handshake with Glass Device
        Proto.Envelope envelope = CompanionMessagingUtil.newEnvelope();
        envelope.timezoneC2G = TimeZone.getDefault().getID();
        write(envelope);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Proto.Envelope envelope2 = CompanionMessagingUtil.newEnvelope();
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
        }
    }

    public void close(){
        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e){
            Log.e(TAG, "Disconnect Error: ", e);
        }

       // setConnectionStatus(ConnectionStatus.DISCONNECTED);
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



    public void postMessage(String text){
        write(GlassMessagingUtil.createTimelineMessage(text));
    }



    public void write(Proto.Envelope envelope) {
        synchronized (STREAM_WRITE_LOCK) {
            try {
                if (mSocket.isConnected()) {
                    OutputStream outStream = mSocket.getOutputStream();
                    Log.e(TAG, "EVNIADO");
                    GlassProtocol.writeMessage(envelope, outStream);
                }
            } catch (IOException e) {
                Log.e(TAG, "Write Error:", e);
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
                            Log.i(TAG, "RX'd from Glass: " + envelope.toString());
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
                Log.v(TAG, "Reader Thread Finished");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
