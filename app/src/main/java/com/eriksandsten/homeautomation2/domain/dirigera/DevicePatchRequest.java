package com.eriksandsten.homeautomation2.domain.dirigera;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.reactive.function.client.WebClient;

@Builder
@Getter
@Setter
public class DevicePatchRequest extends DirigeraRequest<Void> {
    private String deviceId;
    private Device[] patchBody;

    public DevicePatchRequest(String deviceId, Device[] patchBody) {
        this.deviceId = deviceId;
        this.patchBody = patchBody;
    }

    public Void execute(WebClient webClient) {
        webClient.patch()
                .uri("v1/devices/{id}", deviceId)
                .bodyValue(patchBody)
                .retrieve()
                //.onStatus(HttpStatus::isError, DevicePatchRequest::onError)
                //.onStatus(HttpStatus::is2xxSuccessful, DevicePatchRequest::onSuccess)
                .toBodilessEntity().block();

        return null;
    }
}
