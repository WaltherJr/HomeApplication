package com.eriksandsten.homeautomation2.domain.dirigera;

//import de.dvdgeisler.iot.dirigera.client.api.http.RequestException;
//import de.dvdgeisler.iot.dirigera.client.api.model.Error;

abstract class DirigeraRequest {
    /*
    protected static Mono<? extends Throwable> onSuccess(final ClientResponse clientResponse) {
        var responseCode = clientResponse.statusCode();

        return clientResponse
                .bodyToMono(String.class)
                .map(Jsoup::parse)
                .map(Document::body)
                .map(Element::text)
                .map(RequestException::new);
    }

    protected static Mono<? extends Throwable> onError(final ClientResponse clientResponse) {
        if (clientResponse.rawStatusCode() == 404)
            return clientResponse
                    .bodyToMono(String.class)
                    .map(Jsoup::parse)
                    .map(Document::body)
                    .map(Element::text)
                    .map(RequestException::new);

        return clientResponse
                .bodyToMono(Error.class)
                .map(RequestException::new);
    }*/
}
