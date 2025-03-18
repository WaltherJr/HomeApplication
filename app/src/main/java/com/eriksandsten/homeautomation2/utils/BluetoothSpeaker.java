package com.eriksandsten.homeautomation2.utils;

import android.Manifest;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import com.eriksandsten.homeautomation2.activity.main.BaseActivity;
import java.lang.reflect.Method;
import lombok.Getter;

public class BluetoothSpeaker {
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothDevice speakers;
    @Getter
    private final String name;

    public BluetoothSpeaker(BaseActivity activity) {
        bluetoothAdapter = ((BluetoothManager) activity.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        speakers = bluetoothAdapter.getRemoteDevice(activity.getProperty("speakers_mac_address"));

        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, 1);
        }

        name = speakers.getName();
        bluetoothAdapter.getProfileProxy(activity.getApplicationContext(), profileListener, BluetoothProfile.A2DP);
    }

    BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                BluetoothA2dp bluetoothA2dp = (BluetoothA2dp) proxy;

                try {
                    Method connectMethod = bluetoothA2dp.getClass().getMethod("connect", BluetoothDevice.class);
                    connectMethod.invoke(bluetoothA2dp, speakers);
                    Log.d("Bluetooth", "Connected to speaker: " + name);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.d("Bluetooth", "A2DP Service Disconnected");
        }
    };
}
