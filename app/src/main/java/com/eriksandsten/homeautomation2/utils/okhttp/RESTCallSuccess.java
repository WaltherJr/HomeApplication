package com.eriksandsten.homeautomation2.utils.okhttp;

import lombok.Data;

@Data
public class RESTCallSuccess {
    private Object responseBody;

    public RESTCallSuccess(Object responseBody) {
        this.responseBody = responseBody;
    }
}
