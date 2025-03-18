package com.eriksandsten.homeautomation2.utils.okhttp;

import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import java.util.Map;
import lombok.Data;

@Data
public class RESTCallJSONError {
    private String title;
    private String message;

    public RESTCallJSONError(Exception error) {
        this.title = error.getClass().getName();
        this.message = error.getMessage();
    }

    @Override
    public String toString() {
        return HomeAutomationUtils.toJSON(Map.of("title", title, "message", message));
    }
}
