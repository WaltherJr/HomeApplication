package com.eriksandsten.homeautomation2.domain.dirigera;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.io.Serializable;
import java.util.List;

public class DeviceList implements Serializable {
    @JsonUnwrapped
    public List<Device> devices;
}
