package com.sankarmanoj.sankarsdoor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sankarmanoj on 26/01/16.
 */
public class BluetoothConnection extends Thread {
    BluetoothDevice device;
    BluetoothSocket socket;
    public boolean connected = false;
    PrintWriter output;
    BufferedReader input;
    private static final boolean D = true;
    public static final String TAG = "BluetoothConnection";
    String fromDevice="";
    boolean running = true;

    public BluetoothDevice getDevice() {
        return device;
    }

    public BluetoothConnection(BluetoothDevice device)
    {
        this.device=device;
        socket=createRfcommSocket(device);
        this.start();
    }
    public String getInputString()
    {
        String temp= fromDevice;
        fromDevice="";
        return temp;
    }
    @Override
    public void run() {
        try
        {
            socket.connect();
            connected=true;
            LocalBroadcastManager.getInstance(MainActivity.mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.BluetoothConnectionEstablished));
            output=new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
            input=new BufferedReader(new InputStreamReader( socket.getInputStream()));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running)
                    {
                        try {
                            int toAdd = input.read();
                            if(Character.isAlphabetic(toAdd)||Character.isDigit(toAdd)||toAdd==13||toAdd==14) {
                               if(toAdd==13)
                               {
                                   fromDevice="";
                               }
                                else if(toAdd==14)
                               {
                                   Log.d("fromDevice",fromDevice);
                                   if(fromDevice.contains("open"))

                                   {
                                       Log.d(TAG,"Got Open");
                                       LocalBroadcastManager.getInstance(MainActivity.mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.StateOpen));
                                   }
                                   else if(fromDevice.contains("close"))
                                   {
                                       Log.d(TAG, "Got Close");
                                       LocalBroadcastManager.getInstance(MainActivity.mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.StateClose));
                                   }
                               }
                                else {
                                   fromDevice = fromDevice + Character.toString((char) toAdd);
                                   Log.d("toAdd", String.valueOf(toAdd));
                               }
                            }
                            else
                            {
                                Log.e("Got Junk","Killing Connection");
                                running=false;
                                socket.close();

                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            try {
                                socket.close();
                            }
                            catch (IOException err)
                            {
                                err.printStackTrace();
                            }

                            LocalBroadcastManager.getInstance(MainActivity.mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.BluetoothSocketFailed));
                            connected=false;

                            break;


                        }
                    }
                    try {
                        input.close();
                        output.close();

                    }catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }
            }).start();

        }
        catch (IOException e)
        {
            e.printStackTrace();

            LocalBroadcastManager.getInstance(MainActivity.mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.BluetoothSocketFailed));
            connected=false;

        }
    }
    public void SendMessage(String message)
    {
        String toSend = Character.toString((char)13)+message+Character.toString((char)14);
        if(connected)
        {
            fromDevice="";
            output.write(toSend);
            output.flush();

        }
        else
        {
//            throw  new RuntimeException("Not Connected");
        }
        Log.d(TAG+"sending",toSend);

    }

    public static BluetoothSocket createRfcommSocket(BluetoothDevice device) {
        BluetoothSocket tmp = null;
        try {

            Class class1 = device.getClass();
            Class aclass[] = new Class[1];
            aclass[0] = Integer.TYPE;
            Method method = class1.getMethod("createRfcommSocket", aclass);
            Object aobj[] = new Object[1];
            aobj[0] = Integer.valueOf(1);

            tmp = (BluetoothSocket) method.invoke(device, aobj);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            if (D) Log.e(TAG, "createRfcommSocket() failed", e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            if (D) Log.e(TAG, "createRfcommSocket() failed", e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            if (D) Log.e(TAG, "createRfcommSocket() failed", e);
        }
        return tmp;
    }
    public void killMeNow()
    {
        this.SendMessage("killmenow");

    }
}
