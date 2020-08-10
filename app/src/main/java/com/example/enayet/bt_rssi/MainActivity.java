package com.example.enayet.bt_rssi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity {

    TextView mTVShowRSSI;
    TextView mTVStatus;
    final String tag_bt = "Enayet";
    BluetoothAdapter mBluetoothAdapter;
    Boolean mInside = false;
    //Declare the timer
    Timer timerBT = new Timer();
    Context context;

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
/*
    private BroadcastReceiver mBroadcastRxPairing = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(tag_bt, ": Bond Changed! found");
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d("B.Rx 2", "BroadcastReceiver: BOND_BONDED.");
                    mTVStatus.setText("Bonded!");
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d("B.Rx 2", "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d("B.Rx 2", "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };
*/


    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Log.d("B.Rx 2", "BroadcastReceiver: BOND_BONDED.");
                    mTVStatus.setText("Bonded!");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Log.d("B.Rx 2", "BroadcastReceiver: BOND_BONDED.");
                    mTVStatus.setText("Un-Bonded!");
                }

            }
        }
    };


    private BroadcastReceiver mBrodcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(tag_bt, "mBrodcastReceiver: Action found");
            BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(tag_bt, "Name: " + intent.getStringExtra(BluetoothDevice.EXTRA_NAME) +
                        " rssi: " + intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                if (remoteDevice.getAddress().equals("F0:6B:CA:49:74:8A")){
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                    mTVShowRSSI.setText("Device Name: " + name + "; RSSI: " + rssi + " dBm\n");

                    int absRssi = abs(rssi);
                    Log.d("hello", "" + absRssi);
//                    if (absRssi < 60) {
//                        Log.d(tag_bt, "pairing : for RSSI: " + absRssi);
////                        pairDevice(remoteDevice);
//                        IntentFilter mIntFilterBoandState = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//                        registerReceiver(mBroadcastRxPairing, mIntFilterBoandState);
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                            timerBT.cancel();
//                            mBluetoothAdapter.cancelDiscovery();
//                            remoteDevice.createBond();
//                        }
//                    }
//                    else {
//                        Log.d(tag_bt, "Unpairing : for RSSI: " + absRssi);
//                        if (remoteDevice.getBondState() == BluetoothDevice.BOND_BONDED){
//
//                        }
//                    }

                    if (absRssi < 60){
                        IntentFilter mIntFilterBoandState = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                        registerReceiver(mPairReceiver, mIntFilterBoandState);
                        pairDevice(remoteDevice);
                    }
                    else {
                        unpairDevice(remoteDevice);
                    }
                }
            }
        }
    };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            context = getApplicationContext();
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            mTVShowRSSI = (TextView) findViewById(R.id.tvValue_id);
            mTVStatus = (TextView) findViewById(R.id.tvStatus_ID);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



//          Bluetooth Actions are performed!!! Here !!!

            if (mBluetoothAdapter == null){
                Toast.makeText(this, "Bluetooth not supported!", Toast.LENGTH_SHORT).show();
            }
            else if (!mBluetoothAdapter.isEnabled()){
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableIntent);
            }

            //Set the schedule function and rate
            timerBT.scheduleAtFixedRate(new TimerTask() {
              @Override
              public void run() {
                  //Called each time when 1000 milliseconds (1 second) (the period parameter)
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                        Log.d(tag_bt, "cancelling discovery");

                        mBluetoothAdapter.startDiscovery();
                        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mBrodcastReceiver, discoverDevicesIntent);
                    }
                    else if (!mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.startDiscovery();
                        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mBrodcastReceiver, discoverDevicesIntent);
                  }
              }
          },
                    //Set how long before to start calling the TimerTask (in milliseconds)
                    0,
                    //Set the amount of time between each execution (in milliseconds)
                    5000);
        }

    @Override
    protected void onDestroy() {
        Log.d(tag_bt, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBrodcastReceiver);
//        unregisterReceiver(mBroadcastRxPairing);
        unregisterReceiver(mPairReceiver);
    }

/*
    private void pairDevice(BluetoothDevice device)
    {
        String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
        Intent intent = new Intent(ACTION_PAIRING_REQUEST);
        String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
        intent.putExtra(EXTRA_DEVICE, device);
        String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
        int PAIRING_VARIANT_PIN = 0;
        intent.putExtra(EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
*/

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}