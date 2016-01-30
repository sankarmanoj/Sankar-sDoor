package com.sankarmanoj.sankarsdoor;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity {

    public static Activity mActivity;
    BluetoothDevice SelectedDevice;
    BluetoothConnection bluetoothConnection;
    SharedPreferences sharedPreferences;
    TextView ConnectedDeviceTextView , StateTextView;
    TextView WifiStatusTextView;
    Button OpenButton, CloseButton;
    SharedPreferences.Editor sharedPrefEditor;
    TCPConnection tcpConnection;
    String RPIMac = "00:1B:10:00:2A:EC";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity=this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefEditor = sharedPreferences.edit();
        ConnectedDeviceTextView = (TextView)findViewById(R.id.connectedTextView);
        WifiStatusTextView = (TextView)findViewById(R.id.wifistatusTextView);
        OpenButton=(Button)findViewById(R.id.openButton);
        CloseButton=(Button)findViewById(R.id.closeButton);
        StateTextView = (TextView)findViewById(R.id.stateTextView);

        //Initialize Wireless Radios
        final BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        myBluetoothAdapter.enable();
        WifiManager myWifiManger = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        myWifiManger.setWifiEnabled(true);
        tcpConnection = new TCPConnection("192.168.0.200",8439);
        bluetoothConnection = new BluetoothConnection(myBluetoothAdapter.getRemoteDevice(RPIMac));


        //Device Change Option

        //On Click Listeners
        OpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tcpConnection.SendMessage("open");
                bluetoothConnection.SendMessage("open");
            }
        });
        CloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tcpConnection.SendMessage("close");
                bluetoothConnection.SendMessage("close");
            }
        });

        //Setup BroadCast Receivers
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiStatusTextView.setText("Connected");
                WifiStatusTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tcpConnection.killMeNow();
                        WifiStatusTextView.setOnClickListener(null);
                    }
                });
            }
        },new IntentFilter(QuickPreferences.TCPConnectionEstablished));
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(), "Connection to Device Failed", Toast.LENGTH_SHORT).show();
                MainActivity.mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConnectedDeviceTextView.setText("Connection Closed");
                        ConnectedDeviceTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                bluetoothConnection=new BluetoothConnection(myBluetoothAdapter.getRemoteDevice(RPIMac));
                                ConnectedDeviceTextView.setOnClickListener(null);
                            }
                        });
                    }
                });

            }
        },new IntentFilter(QuickPreferences.BluetoothSocketFailed));


        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MainActivity.mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StateTextView.setText("Status : Open");
                        OpenButton.setEnabled(false);
                        CloseButton.setEnabled(true);
                    }
                });
            }
        },new IntentFilter(QuickPreferences.StateOpen));



        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MainActivity.mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StateTextView.setText("Status : Closed");
                        OpenButton.setEnabled(true);
                        CloseButton.setEnabled(false);
                    }
                });
            }
        },new IntentFilter(QuickPreferences.StateClose));



        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectedDeviceTextView.setText(bluetoothConnection.getDevice().getName());
                bluetoothConnection.SendMessage("status");
                ConnectedDeviceTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bluetoothConnection.killMeNow();
                        ConnectedDeviceTextView.setOnClickListener(null);

                    }
                });
            }
        }, new IntentFilter(QuickPreferences.BluetoothConnectionEstablished));
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiStatusTextView.setText("Connection Failed");
                WifiStatusTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tcpConnection = new TCPConnection("192.168.0.200",8439);
                        WifiStatusTextView.setOnClickListener(null);
                    }
                });
            }
        },new IntentFilter(QuickPreferences.TCPSocketFailed));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
//Create Bluetooth Device Selector dialog
//        ArrayList<BluetoothDevice> devices = new ArrayList<>(myBluetoothAdapter.getBondedDevices());
//        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        final BluetoothDeviceArrayAdapter adapter = new BluetoothDeviceArrayAdapter(getApplicationContext(),0,devices);
//        builder.setTitle("Select A Device");
//        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                SelectedDevice = adapter.getItem(i);
//                Log.d("Opening new Connection", SelectedDevice.getName());
//                bluetoothConnection=new BluetoothConnection(SelectedDevice);
//                ConnectedDeviceTextView.setText("Establishing Connection");
//                Log.d("Selected Device", "Name=" + SelectedDevice.getName());
//                dialogInterface.dismiss();
//            }
//
//        });
//
//        String PreviousMac = sharedPreferences.getString(QuickPreferences.PreviousSuccessfulDevice, "null");
//        Log.d("RPI Mac",PreviousMac);
//        if(PreviousMac.equals("null")) {
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        }
//        else
//        {
//            SelectedDevice=null;
//            for (BluetoothDevice device : devices)
//            {
//                if(device.getAddress().equals(PreviousMac))
//                {
//                    SelectedDevice=device;
//                    bluetoothConnection=new BluetoothConnection(SelectedDevice);
//
//                    break;
//                }
//
//            }
//            if(SelectedDevice==null)
//            {
//                dialog = builder.create();
//                dialog.show();
//            }
//        }
