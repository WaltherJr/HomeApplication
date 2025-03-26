package com.eriksandsten.homeautomation2.utils.injection;

import lombok.Data;

@Data
public class JSInjection {
    private Javascript[] scripts;

    public JSInjection(Javascript... scripts) {
        this.scripts = scripts;
    }
}
