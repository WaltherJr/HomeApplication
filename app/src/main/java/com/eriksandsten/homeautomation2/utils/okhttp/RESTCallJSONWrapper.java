package com.eriksandsten.homeautomation2.utils.okhttp;

import lombok.Data;

@Data
public class RESTCallJSONWrapper {
    private RestCallResponseType responseType;
    private Object wrappedValue;

    public RESTCallJSONWrapper() {
    }

    public RESTCallJSONWrapper(RestCallResponseType responseType, Object wrappedValue) {
        this.responseType = responseType;
        this.wrappedValue = wrappedValue;
    }

    @Override
    public String toString() {
        return "{}";
    }
}
