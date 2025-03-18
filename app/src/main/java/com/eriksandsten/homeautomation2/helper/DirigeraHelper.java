package com.eriksandsten.homeautomation2.helper;

import com.eriksandsten.homeautomation2.domain.dirigera.Device;
import com.eriksandsten.homeautomation2.domain.dirigera.DeviceAttributes;
import com.eriksandsten.homeautomation2.domain.dirigera.DevicePatchRequest;
import com.eriksandsten.homeautomation2.domain.dirigera.FetchDevicesRequest;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

public class DirigeraHelper {

    public static Device[] patchIsOnFieldRequest(Boolean isOn) {
        return DevicePatchRequest.builder().devices(new Device[] {
                Device.builder().attributes(DeviceAttributes.builder().isOn(isOn).build()).build()
        }).build().getDevices();
    }

    public static Device[] patchLightLevelRequest(Short lightLevel) {
        return DevicePatchRequest.builder().devices(new Device[] {
                Device.builder().attributes(DeviceAttributes.builder().lightLevel(lightLevel).build()).build()
        }).build().getDevices();
    }

    public static void turnOffApartmentLightsAndPowerOutlets(WebClient webClient, Long turnOffApartmentLightsInitialDelayInMs) {
        try {
            List<Device> devices = FetchDevicesRequest.execute(webClient);
            final Device[] turnOffDeviceRequest = patchIsOnFieldRequest(Boolean.FALSE);

            // Make a first pass with only power outlets, since some lights might depend on these
            devices.stream().filter(device -> device.isReachable.equals(Boolean.TRUE) && device.deviceType.equals("outlet"))
                    .forEach(outlet -> DevicePatchRequest.execute(webClient, outlet.getId(), turnOffDeviceRequest));

            // Pause for some time
            Thread.sleep(turnOffApartmentLightsInitialDelayInMs);

            // Make the second pass with lights this time
            devices = FetchDevicesRequest.execute(webClient);
            devices.stream().filter(device -> device.isReachable.equals(Boolean.TRUE) && device.deviceType.equals("light"))
                    .forEach(outlet -> DevicePatchRequest.execute(webClient, outlet.getId(), turnOffDeviceRequest));

        } catch (final InterruptedException e) {
        }
    }
}
