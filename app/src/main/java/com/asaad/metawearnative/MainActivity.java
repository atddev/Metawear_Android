package com.asaad.metawearnative;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.usage.UsageEvents;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.DataSignal;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Bmi160Accelerometer;
import com.mbientlab.metawear.module.Bmi160Gyro;
import com.mbientlab.metawear.module.DataProcessor;
import com.mbientlab.metawear.module.Led;
import com.mbientlab.metawear.module.Logging;
import com.mbientlab.metawear.module.Macro;
import com.mbientlab.metawear.processor.Average;
import com.mbientlab.metawear.processor.Comparison;
import com.mbientlab.metawear.module.Bmi160Accelerometer.*;
import com.mbientlab.metawear.processor.Rss;
import com.mbientlab.metawear.processor.Threshold;

import static com.mbientlab.metawear.MetaWearBoard.ConnectionStateHandler;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;
import java.util.Map;



public class MainActivity extends Activity implements ServiceConnection {

  //  private MetaWearBoard.ConnectionStateHandler stateHandler;
   // private MetaWearBleService.LocalBinder serviceBinder;


    private final String MW_MAC_ADDRESS = "F5:FD:FD:34:57:CF";
    private MetaWearBoard mwBoard;
    Bmi160Gyro bmi160GyroModule;
    Bmi160Gyro gyroModule;
    Logging loggingModule;
    Bmi160Accelerometer bmi160AccModule = null;
    boolean opening, closing;

    /* server ip in local network */
    public static final String SERVERIP = "192.168.1.124";
    /* udp port 3444 */
    public static final int SERVERPORT = 3444;
    public String message;

    private ProgressDialog progress;
    ToggleButton toggleCon;
     Button btng;




    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MetaWearBleService.LocalBinder binder = (MetaWearBleService.LocalBinder) service;

        final BluetoothManager btManager= (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice= btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        binder.executeOnUiThread();
        //binder.clearCachedState(remoteDevice);
        mwBoard= binder.getMetaWearBoard(remoteDevice);
        mwBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {
            @Override
            public void connected() {
                Toast.makeText(MainActivity.this, "Connection Successful", Toast.LENGTH_LONG).show();


                toggleCon.setBackgroundColor(Color.parseColor("#009688"));
                //  Dismiss the progress dialog
                progress.dismiss();

                btng.setEnabled(true);


            }

            @Override
            public void disconnected() {
                //  Dismiss the progress dialog
                progress.dismiss();
                Toast.makeText(MainActivity.this, "Successfully Disconnected", Toast.LENGTH_LONG).show();

            }

            @Override
            public void failure(int status, final Throwable error) {
                // Print error message
                Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    public void connectBoard() {
        // check if Bluetooth is enabled
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            // If Bluetooth is disabled
            if (!mBluetoothAdapter.isEnabled()) {

                //show alert dialog
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Bluetooth is disabled, you must enable Bluetooth to connect");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // open bluetooth settings
                                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                            }
                        });
                alertDialog.show();

                // keep toggle button unchecked
                toggleCon.setChecked(false);
                // If Bluetooth is enabled
            }else{
                // Change button text to connecting
                toggleCon.setText("Connecting..");
                toggleCon.setBackgroundColor(Color.parseColor("#4CAF50"));

                // Show progress dialog
                progress = new ProgressDialog(this);
                progress.setTitle("Connecting");
                progress.setMessage("Connecting to MetaWear...");
                progress.show();

                // connect
                mwBoard.connect();
            }
        }




    }

    public void disconnectBoard() {
        progress = new ProgressDialog(this);
        progress.setTitle("Disconnecting");
        progress.setMessage("Disconnecting from MetaWear...");
        progress.show();

        mwBoard.disconnect();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);




        //connect/disconnect button
        toggleCon = (ToggleButton) findViewById(R.id.conn);
        toggleCon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    retrieveBoard();
                    connectBoard();

                } else {
                    disconnectBoard();

                }
            }
        });

        // set opening and closing to false
        opening = false;
        closing = false;

       btng = (Button) findViewById(R.id.button);
        btng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("MainActivity", "start sampling sensor");

                btng.setText("Logging...");
                btng.setEnabled(false);
                // initialize the gyro module
                try {
                    gyroModule = mwBoard.getModule(Bmi160Gyro.class);
                } catch (UnsupportedModuleException e) {
                    e.printStackTrace();
                }


                gyroModule.routeData().fromAxes().stream("gyroAxisSub")
                        .commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                    @Override
                    public void success(RouteManager result) {
                        result.subscribe("gyroAxisSub", new RouteManager.MessageHandler() {
                            @Override
                            public void process(Message msg) {
                                final CartesianFloat spinData = msg.getData(CartesianFloat.class);
                                //     Log.i("test", spinData.toString());

                                // if x axes is less than -2, door is being open
                                if (spinData.x() < -2) {
                                    if (!opening) {
                                        Log.i("test", spinData.toString());
                                        Log.i("test", "Door Opening ");



                                        /* Set the message */
                                        message = "Door Opening   " + android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n";


                                        mwBoard.readBatteryLevel().onComplete(new AsyncOperation.CompletionHandler<Byte>() {
                                            @Override
                                            public void success(final Byte result) {
                                                //  ((TextView) findViewById(R.id.textView2)).setText(String.format(Locale.US, "%d", result));
                                                Log.i("test" + "ng battery level %d", String.format(result.toString(), Locale.US, "%d"));

                                                /* Append battery level to the message */
                                                message += (String.format(result.toString(), Locale.US, "%d"));
                                                new Thread(new Client()).start();
                                            }

                                            @Override
                                            public void failure(Throwable error) {
                                                Log.e("test", "Error reading battery level", error);
                                            }
                                        });


                                        // update boolean values
                                        opening = true;
                                        closing = false;
                                    }
                                }
                                if (spinData.x() > 2) {
                                    if (!closing) {
                                        Log.i("test", spinData.toString());
                                        Log.i("test", "Door closing ");
                                        //set messsage
                                        message = "Door Closeing   " + android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n";
                                        // and start a thread to send the message to server
                                        new Thread(new Client()).start();
                                        opening = false;
                                        closing = true;
                                    }
                                }
                            }
                        });

                        // set the date output rate to the minimum (25hz) for efficient power consumption
                        gyroModule.configure().setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_25_HZ)
                                .setFullScaleRange(Bmi160Gyro.FullScaleRange.FSR_250)
                                .commit();
                        // Start the gyroscope
                        gyroModule.start();
                    }
                });


            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // disconnect board
        mwBoard.disconnect();
        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }



    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }


    /* Udp client to send logs to local server (raspberry pi)*/
    /* Note: Udp server soruce code is included in the (/res) directory*/
    public class Client implements Runnable {

        @Override
        public void run() {
            try {

                // send message to Pi
                InetAddress serverAddr = InetAddress.getByName(SERVERIP);
                DatagramSocket clientSocket = new DatagramSocket();
                byte[] sendData = new byte[1024];
                String sentence = message;
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddr, SERVERPORT);
                clientSocket.send(sendPacket);

                // get reply back from Pi
                byte[] receiveData1 = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData1, receiveData1.length);
                clientSocket.receive(receivePacket);

                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}