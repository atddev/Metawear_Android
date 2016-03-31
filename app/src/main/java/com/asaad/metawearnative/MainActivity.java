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

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.DataSignal;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.Bmi160Gyro;
import com.mbientlab.metawear.module.DataProcessor;
import com.mbientlab.metawear.module.Led;
import com.mbientlab.metawear.processor.Comparison;

import java.util.Map;


public class MainActivity extends Activity implements ServiceConnection {
    private MetaWearBleService.LocalBinder serviceBinder;


    private final String MW_MAC_ADDRESS= "F5:FD:FD:34:57:CF";
    private MetaWearBoard mwBoard;
    Bmi160Gyro bmi160GyroModule;

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

        }

        @Override
        public void disconnected() {
       //     Toast toast = Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_SHORT);
        //    toast.show();
            Log.i("MainActivity", "Connection Lost");
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
    public void disconnectBoard() {
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
        ToggleButton toggleCon = (ToggleButton) findViewById(R.id.conn);
        toggleCon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    retrieveBoard();
                    connectBoard();

                } else {
                    connectBoard();

                }
            }
        });



        Button btng = (Button) findViewById(R.id.button);
        btng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("MainActivity", "start sampling sensor");


                try {
                    bmi160GyroModule = mwBoard.getModule(Bmi160Gyro.class);
                } catch (UnsupportedModuleException e) {
                    e.printStackTrace();
                }
// Set the measurement range to +/-2000 degrees/s
// Set output data rate to 100Hz
                bmi160GyroModule.configure()
                        .setFullScaleRange(Bmi160Gyro.FullScaleRange.FSR_125)
                        .setOutputDataRate(Bmi160Gyro.OutputDataRate.ODR_50_HZ)
                        .commit();

                bmi160GyroModule.routeData().fromZAxis().process("comp_filter", new Comparison(Comparison.Operation.GT, 30)).
                        monitor(new DataSignal.ActivityHandler() {
                            @Override
                            public void onSignalActive(Map<String, DataProcessor> processors,
                                                       DataSignal.DataToken token) {

                                // If a value greater than the reference value is received,
                                // update the comparison to use the new, higher value
                                processors.get("comp_filter").modifyConfiguration(new Comparison(
                                        Comparison.Operation.GTE, token));
                                Log.i("MainActivity", "Token is: " + token.toString());


                            }
                        }).commit();


            //Stream rotation data around the Z axe from the gyro sensor
                bmi160GyroModule.routeData().fromAxes().stream("gyro_stream").commit()
                        .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                            @Override
                            public void success(RouteManager result) {
                                result.subscribe("gyro_stream", new RouteManager.MessageHandler() {
                                    @Override
                                    public void process(Message msg) {
                                        final CartesianFloat spinData = msg.getData(CartesianFloat.class);

                                        Log.i("MainActivity", spinData.toString());
                                    }
                                });

                                bmi160GyroModule.start();
                            }
                        });
            }
        });

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
