package com.eriksandsten.homeautomation2.domain.dirigera;

//import de.dvdgeisler.iot.dirigera.client.api.http.RequestException;
//import de.dvdgeisler.iot.dirigera.client.api.model.Error;

import org.springframework.web.reactive.function.client.WebClient;

abstract class DirigeraRequest<T> {

    public abstract T execute(WebClient webClient);
}
