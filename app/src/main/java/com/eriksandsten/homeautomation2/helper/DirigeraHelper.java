package com.eriksandsten.homeautomation2.helper;

import com.eriksandsten.homeautomation2.domain.dirigera.Device;
import com.eriksandsten.homeautomation2.domain.dirigera.DeviceAttributes;
import com.eriksandsten.homeautomation2.domain.dirigera.DevicePatchRequest;
import com.eriksandsten.homeautomation2.domain.dirigera.ListDevicesRequest;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.function.Consumer;

public class DirigeraHelper {
    public static Device[] patchFieldRequest(Consumer<DeviceAttributes.DeviceAttributesBuilder> newFieldValue) {
        DeviceAttributes.DeviceAttributesBuilder attributesBuilder = DeviceAttributes.builder();
        newFieldValue.accept(attributesBuilder);

        return DevicePatchRequest.builder().patchBody(new Device[] {
                Device.builder().attributes(attributesBuilder.build()).build()
        }).build().getPatchBody();
    }

    public static void turnOffApartmentLightsAndPowerOutlets(WebClient webClient, Long turnOffApartmentLightsInitialDelayInMs) {
        try {
            List<Device> devices = new ListDevicesRequest().execute(webClient);
            final Device[] turnOffDeviceRequest = patchFieldRequest(attributes -> attributes.isOn(Boolean.FALSE));

            // Make a first pass with only power outlets, since some lights might depend on these
            devices.stream().filter(device -> device.isReachable.equals(Boolean.TRUE) && device.deviceType.equals("outlet"))
                    .forEach(outlet -> new DevicePatchRequest(outlet.getId(), turnOffDeviceRequest).execute(webClient));

            // Pause for some time
            Thread.sleep(turnOffApartmentLightsInitialDelayInMs);

            // Make the second pass with lights this time
            devices = new ListDevicesRequest().execute(webClient);
            devices.stream().filter(device -> device.isReachable.equals(Boolean.TRUE) && device.deviceType.equals("light"))
                    .forEach(outlet -> new DevicePatchRequest(outlet.getId(), turnOffDeviceRequest).execute(webClient));

        } catch (final InterruptedException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
