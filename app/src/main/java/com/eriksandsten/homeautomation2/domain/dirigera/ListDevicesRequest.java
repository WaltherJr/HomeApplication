package com.eriksandsten.homeautomation2.domain.dirigera;

import org.springframework.web.reactive.function.client.WebClient;
import java.util.Arrays;
import java.util.List;

public class ListDevicesRequest extends DirigeraRequest<List<Device>> {
    @Override
    public List<Device> execute(WebClient webClient) {
        return Arrays.asList(webClient.get()
            .uri("/v1/devices")
            .retrieve()
            //.onStatus(HttpStatus::isError, DirigeraRequest::onError)
            //.onStatus(HttpStatus::is2xxSuccessful, DirigeraRequest::onSuccess)
            .toEntity(Device[].class).block().getBody());
    }
}
