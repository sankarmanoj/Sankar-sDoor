package com.sankarmanoj.sankarsdoor;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sankarmanoj on 26/01/16.
 */
public class BluetoothDeviceArrayAdapter extends ArrayAdapter<BluetoothDevice> {
    public BluetoothDeviceArrayAdapter(Context context, int resource , ArrayList<BluetoothDevice> devices)
    {
        super(context, 0, devices);
    }
    public static class ViewHolder
    {
        TextView DeviceNameTextView;
        TextView DeviceMacTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = getItem(position);
        ViewHolder holder;
        if(convertView==null)
        {

            holder = new ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView= layoutInflater.inflate(R.layout.bluetoothdeviceitem,null);
            holder.DeviceMacTextView=(TextView)convertView.findViewById(R.id.deviceMACTextView);
            holder.DeviceNameTextView=(TextView)convertView.findViewById(R.id.deviceNameTextView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.DeviceNameTextView.setText(device.getName());
        holder.DeviceMacTextView.setText(device.getAddress());

        return convertView;
    }
}
