package com.eriksandsten.homeautomation2.helper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Getter;

public abstract class BaseClient {
    private static final ExecutorService asusMediaServerRequestExecutor = Executors.newSingleThreadExecutor();
    @Getter
    private final String serverUrl;
    private final int requestTimeoutInMs;

    public BaseClient(String serverUrl, int requestTimeoutInMs) {
        this.serverUrl = serverUrl;
        this.requestTimeoutInMs = requestTimeoutInMs;
    }

    public Object pingServer() {
        return performRequest(() -> HttpHelper.performGetRequest(getServerUrl(), "/", requestTimeoutInMs));
    }

    protected Object performRequest(Callable<?> runnable) {
        try {
            return asusMediaServerRequestExecutor.submit(runnable).get();

        } catch (final ExecutionException | InterruptedException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
