package com.eriksandsten.homeautomation2.utils;

import lombok.Data;

@Data
public class RESTCallSuccess {
    private Object responseBody;

    public RESTCallSuccess(Object responseBody) {
        this.responseBody = responseBody;
    }
}
