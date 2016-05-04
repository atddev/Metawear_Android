package com.asaad.metawearnative;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.Bmi160Gyro;
import com.mbientlab.metawear.module.Bmm150Magnetometer;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;


public class MainActivity extends Activity implements ServiceConnection {


    private final String MW_MAC_ADDRESS = "F5:FD:FD:34:57:CF";
    private MetaWearBoard mwBoard;
    Bmi160Gyro gyroModule;
    Bmm150Magnetometer magModule;
    boolean opening, closing, alert;


    int openv, closev;
    /* server ip in local network */
    public static final String SERVERIP = "192.168.1.124";
    /* udp port 3444 */
    public static final int SERVERPORT = 3444;
    public String message;

    private ProgressDialog progress;
    Button ConBut;
    Button btng;
    // confiugre magn button
    Button megcon;
    CheckBox checkalert;
    RadioGroup btnr;
    // battery level text
    TextView batT;
    // battery level image
    ImageView batI;

    SharedPreferences prefs;

    // method to show the magnetoemeter config. dialog
    public void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.set_mag, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        try {
            magModule = mwBoard.getModule(Bmm150Magnetometer.class);
        } catch (UnsupportedModuleException e) {
            e.printStackTrace();
        }

        final EditText opval = (EditText) promptView.findViewById(R.id.EditText_opval);
        final EditText clval = (EditText) promptView.findViewById(R.id.EditText_clval);
        final TextView curval = (TextView) promptView.findViewById(R.id.TextView_curval);
        // setup a dialog window

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        openv = Integer.parseInt(opval.getText().toString());
                        closev = Integer.parseInt(clval.getText().toString());


                        magModule.stop();

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                magModule.stop();
                                dialog.cancel();

                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();


        //Set to low power mode
        magModule.setPowerPrsest(Bmm150Magnetometer.PowerPreset.LOW_POWER);
        magModule.enableBFieldSampling();

        //Stream rotation data around the XYZ axes from the gyro sensor
        magModule.routeData().fromBField().stream("mag_stream").commit()
                .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                    @Override
                    public void success(RouteManager result) {
                        result.subscribe("mag_stream", new RouteManager.MessageHandler() {
                            @Override
                            public void process(Message msg) {
                                final CartesianFloat bField = msg.getData(CartesianFloat.class);


                                curval.setText(bField.z().toString());

                                magModule.start();
                            }
                        });

                    }
                });
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MetaWearBleService.LocalBinder binder = (MetaWearBleService.LocalBinder) service;

        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        binder.executeOnUiThread();
        //binder.clearCachedState(remoteDevice);
        mwBoard = binder.getMetaWearBoard(remoteDevice);
        mwBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {
            @Override
            public void connected() {
                Toast.makeText(MainActivity.this, "Connection Successful", Toast.LENGTH_LONG).show();


                ConBut.setBackgroundColor(Color.parseColor("#009688"));
                ConBut.setText("Disconnect");
                //  Dismiss the progress dialog
                progress.dismiss();
                //enable buttons
                megcon.setEnabled(true);
                btng.setEnabled(true);
                // show updated battery status
                updateBattery();


            }

            @Override
            public void disconnected() {
                //  Dismiss the progress dialog
                progress.dismiss();
                //show toast
                Toast.makeText(MainActivity.this, "Successfully Disconnected", Toast.LENGTH_LONG).show();
                // change disconnect button
                ConBut.setBackgroundColor(Color.parseColor("#3F51B5"));
                ConBut.setText("Connect");

                //change logging button do default
                btng.setText("START LOGGING");
                btng.setEnabled(false);


            }

            @Override
            public void failure(int status, final Throwable error) {
                //  Dismiss the progress dialog
                progress.dismiss();
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

                // If Bluetooth is enabled
            } else {

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


        // Unbind the service when board disconnected
        getApplicationContext().unbindService(this);

        mwBoard.disconnect();

    }


    // this method is to update the battery level in the UI
    public void updateBattery() {

        if (mwBoard.isConnected()) {
            mwBoard.readBatteryLevel().onComplete(new AsyncOperation.CompletionHandler<Byte>() {
                @Override
                public void success(final Byte result) {
                    ((TextView) findViewById(R.id.BattextView)).setText("Battery Level: " + String.format(Locale.US, "%d", result) + "%");
                    int batlvl = result.intValue();

                    // set icon
                    if (batlvl > 80 && batlvl <= 100) {
                        batI.setImageResource(R.drawable.bat100);
                    } else if (batlvl > 60 && batlvl < 80) {
                        batI.setImageResource(R.drawable.bat60);
                    } else if (batlvl > 40 && batlvl < 60) {
                        batI.setImageResource(R.drawable.bat40);
                    } else if (batlvl > 20 && batlvl < 40) {
                        batI.setImageResource(R.drawable.bat20);
                    } else if (batlvl > 10 && batlvl < 20) {
                        batI.setImageResource(R.drawable.bat19);
                    } else if (batlvl <= 10) {
                        batI.setImageResource(R.drawable.bat5);
                    }


                }

                @Override
                public void failure(Throwable error) {
                    // do nothing
                }
            });

        } // end if connect

    }

    public void OnBatteryUpdate(View v) {
        updateBattery();
        Toast.makeText(MainActivity.this, "Updating Battery", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);


        //check box
        //   final CheckBox checkalert = (CheckBox) findViewById(R.id.checkbox);
        checkalert = (CheckBox) MainActivity.this.findViewById(R.id.checkBox);
        checkalert.setChecked(false);
        // battery icon
        batI = (ImageView) findViewById(R.id.imageView);
        // battery level text
        batT = (TextView) findViewById(R.id.BattextView);

        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        // set meg value dilog

        final View layout = inflater.inflate(R.layout.set_mag, (ViewGroup) findViewById(R.id.root));
        // opening value
        final EditText opValue = (EditText) layout.findViewById(R.id.EditText_opval);
        // closeing value
        final EditText clValue = (EditText) layout.findViewById(R.id.EditText_clval);
        // current value
        final TextView curValue = (TextView) layout.findViewById(R.id.TextView_curval);
        // set opening and closing to false
        opening = false;
        closing = false;


        prefs = this.getSharedPreferences("SenseApp", Context.MODE_PRIVATE);


        // restore UI status
        if (mwBoard != null && mwBoard.isConnected()) {
            ConBut.setBackgroundColor(Color.parseColor("#009688"));
            ConBut.setText("Disconnect");

            megcon.setEnabled(true);
            btng.setEnabled(true);
            // show updated battery status
            updateBattery();


            /// if logging, check which sensor and settext
            prefs = this.getSharedPreferences("SenseApp", Context.MODE_PRIVATE);

            int prefValue = prefs.getInt("sensor", 0);
            // if gyro is logging
            if (prefValue == 1) {
                btng.setText("Logging (Gyro)...");
                btng.setEnabled(false);
                // set gyro radio button to checked
                RadioButton gyb = (RadioButton) findViewById(R.id.radio_gyro);
                gyb.setChecked(true);
            }
            // if magnetometer is logging
            if (prefValue == 2) {
                btng.setText("Logging (Magneto)...");
                btng.setEnabled(false);
                // set gyro radio button to checked
                RadioButton gyb = (RadioButton) findViewById(R.id.radio_gyro);
                gyb.setChecked(true);
            }
        }


        //connect/disconnect button
        ConBut = (Button) findViewById(R.id.conn);
        ConBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mwBoard.isConnected()) {
                    disconnectBoard();
                } else {
                    connectBoard();
                }

            }

        });


        // Choose sensor radio buttons group
        btnr = (RadioGroup) findViewById(R.id.radio);

        // Configure Magnetometer button clicked
        megcon = (Button) findViewById(R.id.conmag);
        megcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }

        });


        // start sensor button
        btng = (Button) findViewById(R.id.button);
        btng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alert = checkalert.isChecked();

                Log.i("MainActivity", "start sampling sensor");
                // if gyro is selected
                if (btnr.getCheckedRadioButtonId() == R.id.radio_gyro) {
                    btng.setText("Logging (Gyro)...");
                    btng.setEnabled(false);

                    // save sensor 1 (gyro) in use
                    prefs.edit().putInt("sensor", 1).apply();
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
                                            Log.i("test", "Door Opened ");



                                        /* Set the message */
                                            message = "Door Opened   " + android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n";


                                            mwBoard.readBatteryLevel().onComplete(new AsyncOperation.CompletionHandler<Byte>() {
                                                @Override
                                                public void success(final Byte result) {
                                                    //  ((TextView) findViewById(R.id.textView2)).setText(String.format(Locale.US, "%d", result));
                                                    Log.i("test" + "ng battery level %d", String.format(result.toString(), Locale.US, "%d"));

                                                /* Append battery level to the message */
                                                    message += (String.format(result.toString(), Locale.US, "%d"));

                                                    String curTime = android.text.format.DateFormat.format("hh:mm:ss aa", new java.util.Date()).toString();
                                                    String curDate = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();

                                                    //send log to webserver
                                                    SendToServer sendTask = new SendToServer();
                                                    sendTask.execute("Door Opened", curTime, curDate);

                                                    // if alret box is checked, send to Raspi
                                                    if (alert) {
                                                        new Thread(new Client()).start();
                                                    }


                                                    // SendToServer.SendTask(message);
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
                                            Log.i("test", "Door Closed ");
                                            //set messsage


                                            String curTime = android.text.format.DateFormat.format("hh:mm:ss aa", new java.util.Date()).toString();
                                            String curDate = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();

                                            //send log to webserver
                                            SendToServer sendTask = new SendToServer();
                                            sendTask.execute("Door Closed", curTime, curDate);


                                            message = "Door Closed  " + android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n";
                                            // and start a thread to send the message to server
                                            if (alert) {
                                                new Thread(new Client()).start();
                                            }
                                            opening = false;
                                            closing = true;
                                        }
                                    }
                                }
                            });

                            // set the data output rate to the minimum (25hz) for efficient power consumption
                            gyroModule.configure().setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_25_HZ)
                                    .setFullScaleRange(Bmi160Gyro.FullScaleRange.FSR_250)
                                    .commit();
                            // Start the gyroscope
                            gyroModule.start();
                        }
                    });


                }//end if gyro

                // if magnetometer
                if (btnr.getCheckedRadioButtonId() == R.id.radio_mag) {
                    // log magn
                    Log.i("test", "Magnetometer Data Logging ");
                    btng.setText("Logging (Magneto)...");
                    btng.setEnabled(false);

                    // save sensor 2 (Magne) in use
                    prefs.edit().putInt("sensor", 2).apply();

                    try {
                        magModule = mwBoard.getModule(Bmm150Magnetometer.class);
                    } catch (UnsupportedModuleException e) {
                        e.printStackTrace();
                    }


                    //Set to low power mode
                    magModule.setPowerPrsest(Bmm150Magnetometer.PowerPreset.LOW_POWER);
                    magModule.enableBFieldSampling();

                    //Stream rotation data around the XYZ axes from the gyro sensor
                    magModule.routeData().fromBField().stream("mag_stream").commit()
                            .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                                @Override
                                public void success(RouteManager result) {
                                    result.subscribe("mag_stream", new RouteManager.MessageHandler() {
                                        @Override
                                        public void process(Message msg) {
                                            final CartesianFloat bField = msg.getData(CartesianFloat.class);

                                            //                 Log.i("MainActivity" +"Z", bField.z().toString());


                                            if (bField.z() > openv) {
                                                if (!opening) {
                                                    Log.i("test", bField.toString());
                                                    Log.i("test", "Door Opened ");



                                                       /* Set the message */
                                                    message = "Door Opened   " + android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n";


                                                    mwBoard.readBatteryLevel().onComplete(new AsyncOperation.CompletionHandler<Byte>() {
                                                        @Override
                                                        public void success(final Byte result) {
                                                            //  ((TextView) findViewById(R.id.textView2)).setText(String.format(Locale.US, "%d", result));
                                                            Log.i("test" + "ng battery level %d", String.format(result.toString(), Locale.US, "%d"));

                                                /* Append battery level to the message */
                                                            message += (String.format(result.toString(), Locale.US, "%d"));


                                                            String curTime = android.text.format.DateFormat.format("hh:mm:ss aa", new java.util.Date()).toString();
                                                            String curDate = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();

                                                            //send log to webserver
                                                            SendToServer sendTask = new SendToServer();
                                                            sendTask.execute("Door Opened", curTime, curDate);


                                                            if (alert) {
                                                                new Thread(new Client()).start();
                                                            }
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

                                            } else if (bField.z() < closev) {
                                                if (!closing) {
                                                    Log.i("test", bField.z().toString());
                                                    Log.i("test", "Door Closed ");
                                                    //set message
                                                    message = "Door Closed   " + android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()) + "\n";


                                                    String curTime = android.text.format.DateFormat.format("hh:mm:ss aa", new java.util.Date()).toString();
                                                    String curDate = android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString();

                                                    //send log to webserver
                                                    SendToServer sendTask = new SendToServer();
                                                    sendTask.execute("Door Closed", curTime, curDate);

                                                    // and start a thread to send the message to server
                                                    if (alert) {
                                                        new Thread(new Client()).start();
                                                    }
                                                    opening = false;
                                                    closing = true;
                                                }
                                            }


                                        }
                                    });

                                    magModule.start();
                                }
                            });


                }


            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind the service when board disconnected
        if (mwBoard != null && !mwBoard.isConnected()) {
            getApplicationContext().unbindService(this);
        }
    }


    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }


    @Override
    public void onResume() {
        super.onResume();
        // restore UI status
        if (mwBoard != null && mwBoard.isConnected()) {
            ConBut.setBackgroundColor(Color.parseColor("#009688"));
            ConBut.setText("Disconnect");


            btng.setEnabled(true);
            megcon.setEnabled(true);
            // show updated battery status
            updateBattery();


            /// if logging, check which sensor and settext
            prefs = this.getSharedPreferences("SenseApp", Context.MODE_PRIVATE);

            int prefValue = prefs.getInt("sensor", 0);
            // if gyro is logging
            if (prefValue == 1) {
                btng.setText("Logging (Gyro)...");
                btng.setEnabled(false);
                // set gyro radio button to checked
                RadioButton gyb = (RadioButton) findViewById(R.id.radio_gyro);
                gyb.setChecked(true);


            }
            // if magnetometer is logging
            if (prefValue == 2) {
                btng.setText("Logging (Magneto)...");
                btng.setEnabled(false);
                // set gyro radio button to checked
                RadioButton gyb = (RadioButton) findViewById(R.id.radio_gyro);
                gyb.setChecked(true);


            }


        }

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


                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}