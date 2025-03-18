package com.eriksandsten.homeautomation2.domain.dirigera;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.reactive.function.client.WebClient;

@Builder
@Getter
@Setter
public class DevicePatchRequest extends DirigeraRequest {
    public Device[] devices;

    public static <T> void execute(WebClient webClient, String deviceId, T patchBody) { // TODO: add error handling and throw
        webClient.patch()
                .uri("v1/devices/{id}", deviceId)
                .bodyValue(patchBody)
                .retrieve()
                //.onStatus(HttpStatus::isError, DevicePatchRequest::onError)
                //.onStatus(HttpStatus::is2xxSuccessful, DevicePatchRequest::onSuccess)
                .toBodilessEntity().block();
    }
}
