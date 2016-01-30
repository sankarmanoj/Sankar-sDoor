package com.sankarmanoj.sankarsdoor;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sankarmanoj.sankarsdoor.MainActivity;
import com.sankarmanoj.sankarsdoor.QuickPreferences;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by sankarmanoj on 27/01/16.
 */
public class TCPConnection extends Thread {
    InetAddress ServerAddress;
    Socket socket;
    int ServerPort;
    PrintWriter output;
    BufferedReader input;
    String fromDevice="";
    Boolean running = true;
    Boolean connected=false;
    String TAG = "TCPConnection";
    public TCPConnection(String address, int port)
    {
        try {
             ServerAddress = InetAddress.getByName(address);
            ServerPort=port;
            this.start();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            //TODO:Broadcast Address Error

        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket(ServerAddress, ServerPort);
            connected=true;
            output=new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            LocalBroadcastManager.getInstance(MainActivity.mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.TCPConnectionEstablished));
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
                                    Log.d(TAG,fromDevice+"::hello");
                                    if(fromDevice.contains("open"))

                                    {
                                        Log.d(TAG,"Got Open");
                                        LocalBroadcastManager.getInstance(MainActivity.mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.StateOpen));
                                    }
                                    if(fromDevice.contains("close")||fromDevice.equals("close"))
                                    {
                                        Log.d(TAG, "Got Close");
                                        LocalBroadcastManager.getInstance(MainActivity.mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.StateClose));
                                    }
                                }
                                else {
                                    fromDevice = fromDevice + Character.toString((char) toAdd);
                                    Log.d(TAG+":toAdd", String.valueOf(toAdd));
                                }
                            }
                            else
                            {
                                Log.e("Got Junk","Killing Connection");
                                LocalBroadcastManager.getInstance(MainActivity.mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.TCPSocketFailed));
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

                            LocalBroadcastManager.getInstance(MainActivity.mActivity.getApplicationContext()).sendBroadcast(new Intent(QuickPreferences.TCPSocketFailed));


                            break;


                        }
                    }
                }
            }).start();
        }catch (IOException e)
        {
            e.printStackTrace();
            //TODO:Broadcast Connection Error
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
           // throw  new RuntimeException("Not Connected");
        }
        Log.d(TAG,"Sending"+toSend);

    }
    public void killMeNow()
    {
        this.SendMessage("killmenow");

    }
}
