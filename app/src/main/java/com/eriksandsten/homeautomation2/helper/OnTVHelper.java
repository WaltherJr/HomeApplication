package com.eriksandsten.homeautomation2.helper;

import com.eriksandsten.homeautomation2.utils.okhttp.RESTCallJSONError;
import com.eriksandsten.homeautomation2.utils.okhttp.RESTCallJSONWrapper;
import com.eriksandsten.homeautomation2.utils.okhttp.RESTCallSuccess;
import com.eriksandsten.homeautomation2.utils.okhttp.RestCallResponseType;
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

public final class OnTVHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final long DEFAULT_TIMEOUT_IN_MS = 1000L;
    public static String latestRESTCallResponse;

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public static String performGetRequest(String serverUrl, String serverEndpoint, long timeoutInMs, Map<String, String> requestHeaders) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.GET.name(), null, String.class, timeoutInMs, requestHeaders);
    }

    public static String performGetRequest(String serverUrl, String serverEndpoint, long timeoutInMs) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.GET.name(), null, String.class, timeoutInMs, null);
    }

    public static String performGetRequest(String serverUrl, String serverEndpoint) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.GET.name(), null, String.class, DEFAULT_TIMEOUT_IN_MS, null);
    }

    public static String performPutRequest(String serverUrl, String serverEndpoint, FormBody formBody) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.PUT.name(), formBody, String.class, DEFAULT_TIMEOUT_IN_MS, null);
    }

    public static String performPutRequest(String serverUrl, String serverEndpoint, FormBody formBody, long timeoutInMs) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.PUT.name(), formBody, String.class, timeoutInMs, null);
    }

    public static String performPostRequest(String serverUrl, String serverEndpoint, FormBody formBody) {
        return performRequest(serverUrl, serverEndpoint, HttpMethod.POST.name(), formBody, String.class, DEFAULT_TIMEOUT_IN_MS, null);
    }

    private static String performRequest(String baseUrl, String serverEndpoint, String method, FormBody formBody, Class<?> responseType, long timeoutInMs, Map<String, String> requestHeaders) {
        try {
            return executor.submit(() -> {
                Duration timeout = Duration.ofMillis(timeoutInMs);
                OkHttpClient client = new OkHttpClient.Builder()
                        .callTimeout(timeout).connectTimeout(timeout).callTimeout(timeout)
                        .build();

                var requestBuilder = new Request.Builder().url(baseUrl + serverEndpoint).method(method, formBody);
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
