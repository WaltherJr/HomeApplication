package com.eriksandsten.homeautomation2.utils;

import android.Manifest;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import com.eriksandsten.homeautomation2.activity.main.BaseActivity;
import com.eriksandsten.homeautomation2.activity.main.MainActivity;

import java.lang.reflect.Method;
import lombok.Getter;

public class BluetoothSpeaker {
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothDevice speakers;
    @Getter
    private final String name;
    private final MainActivity mainActivity;

    public BluetoothSpeaker(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.bluetoothAdapter = ((BluetoothManager) mainActivity.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        this.speakers = bluetoothAdapter.getRemoteDevice(mainActivity.getProperty("speakers_mac_address"));

        if (ActivityCompat.checkSelfPermission(mainActivity.getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[] { Manifest.permission.BLUETOOTH_CONNECT }, 1);
        }

        this.name = speakers.getName();
        bluetoothAdapter.getProfileProxy(mainActivity.getApplicationContext(), profileListener, BluetoothProfile.A2DP);
    }

    public void volumeUp() {
        AudioManager audioManager = (AudioManager) mainActivity.getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    public void volumeDown() {
        AudioManager audioManager = (AudioManager) mainActivity.getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }

    BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                BluetoothA2dp bluetoothA2dp = (BluetoothA2dp) proxy;

                try {
                    Method connectMethod = bluetoothA2dp.getClass().getMethod("connect", BluetoothDevice.class);
                    connectMethod.invoke(bluetoothA2dp, speakers);
                    mainActivity.getMainActivityGUI().setBluetoothSpeakersText(name);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            mainActivity.getMainActivityGUI().setBluetoothSpeakersText(null);
        }
    };
}
