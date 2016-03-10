package com.asaad.metawearnative;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Led;



public class MainActivity extends Activity implements ServiceConnection {
    private MetaWearBleService.LocalBinder serviceBinder;


    private final String MW_MAC_ADDRESS= "F5:FD:FD:34:57:CF";
    private MetaWearBoard mwBoard;

    public void retrieveBoard() {
        final BluetoothManager btManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        final BluetoothDevice remoteDevice =
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        // Create a MetaWear board object for the Bluetooth Device
        mwBoard = serviceBinder.getMetaWearBoard(remoteDevice);
    }
    private final MetaWearBoard.ConnectionStateHandler stateHandler= new MetaWearBoard.ConnectionStateHandler() {
        @Override
        public void connected() {

            Log.i("MainActivity", "Connected");
     //       Toast toast = Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT);
      //      toast.show();
            Button co = (Button) findViewById(R.id.co);
            co.setClickable(false);
        }

        @Override
        public void disconnected() {
       //     Toast toast = Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_SHORT);
        //    toast.show();
            Log.i("MainActivity", "Connected Lost");
        }

        @Override
        public void failure(int status, Throwable error) {
        //    Toast toast = Toast.makeText(getApplicationContext(), "Error connecting", Toast.LENGTH_SHORT);
        //    toast.show();
            Log.e("MainActivity", "Error connecting", error);
        }
    };

    public void connectBoard() {
        mwBoard.setConnectionStateHandler(stateHandler);
        mwBoard.connect();
    }

    boolean blue, red,green;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);


        //Blue LED button
        ToggleButton toggleB = (ToggleButton) findViewById(R.id.blue);
        toggleB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    try {
                        // Do not need to type cast result to Led class
                        Led ledCtrllr = mwBoard.getModule(Led.class);
                        ledCtrllr.configureColorChannel(Led.ColorChannel.BLUE)
                                .setRiseTime((short) 0).setPulseDuration((short) 1000)
                                .setRepeatCount((byte) -1).setHighTime((short) 500)
                                .setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                                .commit();
                        ledCtrllr.play(false);

                    } catch (UnsupportedModuleException e) {
                        Log.e("MainActivity", "No Led on the board", e);
                    }

                } else {
                    // The toggle is disabled
                    try {
                        // Do not need to type cast result to Led class
                        Led ledCtrllr = mwBoard.getModule(Led.class);
                        ledCtrllr.configureColorChannel(Led.ColorChannel.BLUE)
                                .setRiseTime((short) 0).setPulseDuration((short) 0)
                                .setRepeatCount((byte) -1).setHighTime((short) 0)
                                .setHighIntensity((byte) 0).setLowIntensity((byte) 0)
                                .commit();
                        ledCtrllr.play(false);

                    } catch (UnsupportedModuleException e) {
                        Log.e("MainActivity", "No Led on the board", e);
                    }

                }
            }
        });

        //Red LED button
        ToggleButton toggleR = (ToggleButton) findViewById(R.id.red);
        toggleR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    try {
                        // Do not need to type cast result to Led class
                        Led ledCtrllr = mwBoard.getModule(Led.class);
                        ledCtrllr.configureColorChannel(Led.ColorChannel.RED)
                                .setRiseTime((short) 0).setPulseDuration((short) 1000)
                                .setRepeatCount((byte) -1).setHighTime((short) 500)
                                .setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                                .commit();
                        ledCtrllr.play(false);

                    } catch (UnsupportedModuleException e) {
                        Log.e("MainActivity", "No Led on the board", e);
                    }

                } else {
                    // The toggle is disabled
                    try {
                        // Do not need to type cast result to Led class
                        Led ledCtrllr = mwBoard.getModule(Led.class);
                        ledCtrllr.configureColorChannel(Led.ColorChannel.RED)
                                .setRiseTime((short) 0).setPulseDuration((short) 0)
                                .setRepeatCount((byte) -1).setHighTime((short) 0)
                                .setHighIntensity((byte) 0).setLowIntensity((byte) 0)
                                .commit();
                        ledCtrllr.play(false);

                    } catch (UnsupportedModuleException e) {
                        Log.e("MainActivity", "No Led on the board", e);
                    }

                }
            }
        });

        //Blue LED button
        ToggleButton toggleG = (ToggleButton) findViewById(R.id.green);
        toggleG.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    try {
                        // Do not need to type cast result to Led class
                        Led ledCtrllr = mwBoard.getModule(Led.class);
                        ledCtrllr.configureColorChannel(Led.ColorChannel.GREEN)
                                .setRiseTime((short) 0).setPulseDuration((short) 1000)
                                .setRepeatCount((byte) -1).setHighTime((short) 500)
                                .setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                                .commit();
                        ledCtrllr.play(false);

                    } catch (UnsupportedModuleException e) {
                        Log.e("MainActivity", "No Led on the board", e);
                    }

                } else {
                    // The toggle is disabled
                    try {
                        // Do not need to type cast result to Led class
                        Led ledCtrllr = mwBoard.getModule(Led.class);
                        ledCtrllr.configureColorChannel(Led.ColorChannel.GREEN)
                                .setRiseTime((short) 0).setPulseDuration((short) 0)
                                .setRepeatCount((byte) -1).setHighTime((short) 0)
                                .setHighIntensity((byte) 0).setLowIntensity((byte) 0)
                                .commit();

                        ledCtrllr.play(false);


                    } catch (UnsupportedModuleException e) {
                        Log.e("MainActivity", "No Led on the board", e);
                    }

                }
            }
        });

    }
    public void conn(View view) {
        // Do something in response to button
        Log.e("MainActivity", " retrieveBoard();");
        retrieveBoard();
        Log.e("MainActivity", " connectBoard();");
        connectBoard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (MetaWearBleService.LocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }
}
