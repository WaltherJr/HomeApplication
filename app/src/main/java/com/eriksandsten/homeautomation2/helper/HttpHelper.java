package com.eriksandsten.homeautomation2.helper;

import com.eriksandsten.homeautomation2.utils.RESTCallJSONError;
import com.eriksandsten.homeautomation2.utils.RESTCallJSONWrapper;
import com.eriksandsten.homeautomation2.utils.RESTCallSuccess;
import com.eriksandsten.homeautomation2.utils.RestCallResponseType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.*;

public final class HttpHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int DEFAULT_REQUEST_TIMEOUT_IN_MS = 2000;
    public static String latestRESTCallResponse; // Strings in Java are immutable by default

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public static String performGetRequest(String serverUrl, String serverEndpoint, int timeoutInMs, Map<String, String> requestHeaders) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.GET.name(), null, String.class, timeoutInMs, requestHeaders);
    }

    public static String performGetRequest(String serverUrl, String serverEndpoint, int timeoutInMs) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.GET.name(), null, String.class, timeoutInMs, null);
    }

    public static String performGetRequest(String serverUrl, String serverEndpoint) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.GET.name(), null, String.class, DEFAULT_REQUEST_TIMEOUT_IN_MS, null);
    }

    public static String performPutRequest(String serverUrl, String serverEndpoint, String jsonBody) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.PUT.name(), jsonBody, String.class, DEFAULT_REQUEST_TIMEOUT_IN_MS, null);
    }

    public static String performPutRequest(String serverUrl, String serverEndpoint, String jsonBody, int timeoutInMs) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.PUT.name(), jsonBody, String.class, timeoutInMs, null);
    }

    public static String performPostRequest(String serverUrl, String serverEndpoint, String jsonBody) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.POST.name(), jsonBody, String.class, DEFAULT_REQUEST_TIMEOUT_IN_MS, null);
    }

    private static String performRequest(String baseUrl, String serverEndpoint, String method, String jsonBody, Class<?> responseType, int timeoutInMs, Map<String, String> requestHeaders) {
        try {
            return executor.submit(() -> {
                Duration timeout = Duration.ofMillis(Integer.valueOf(timeoutInMs).longValue());
                OkHttpClient client = new OkHttpClient.Builder()
                        .callTimeout(timeout).connectTimeout(timeout).callTimeout(timeout)
                        .build();

                // var requestBuilder = new Request.Builder().url(baseUrl + serverEndpoint).method(method, formBody);
                var requestBuilder = new Request.Builder().url(baseUrl + serverEndpoint).method(method, RequestBody.create(jsonBody, MediaType.get("application/json")));
                if (requestHeaders != null) {
                    requestBuilder = requestBuilder.headers(Headers.of(requestHeaders));
                }

                try (Response response = client.newCall(requestBuilder.build()).execute()) {
                    final String responseBodyString = response.body().string();
                    latestRESTCallResponse = responseBodyString;
                    Object responseObj = responseType != String.class ? mapper.readValue(responseBodyString, responseType) : responseBodyString;
                    return mapper.writeValueAsString(new RESTCallJSONWrapper(RestCallResponseType.SUCCESS, new RESTCallSuccess(responseObj)));

                } catch (final RuntimeException | IOException e) {
                    try {
                        return mapper.writeValueAsString(new RESTCallJSONWrapper(RestCallResponseType.ERROR, new RESTCallJSONError(e)));

                    } catch (final JsonProcessingException __) {
                        return new RESTCallJSONWrapper(RestCallResponseType.ERROR, new RESTCallJSONError(e)).toString();
                    }
                }
            }).get();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return new RESTCallJSONWrapper(RestCallResponseType.ERROR, new RESTCallJSONError(e)).toString();
        }
    }
}
