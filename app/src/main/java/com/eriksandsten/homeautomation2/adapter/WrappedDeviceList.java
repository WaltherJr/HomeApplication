package com.eriksandsten.homeautomation2.adapter;

import com.eriksandsten.homeautomation2.domain.dirigera.Device;
import java.util.List;

public class WrappedDeviceList {
    private List<Device> devices;

    public synchronized List<Device> getDevices() {
        return devices;
    }

    public synchronized void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public synchronized Device getDeviceById(String id) {
        return devices.stream().filter(d -> d.getId().equals(id)).findFirst().get();
    }
}
