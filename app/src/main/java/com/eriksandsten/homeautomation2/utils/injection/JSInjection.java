package com.eriksandsten.homeautomation2.utils.injection;

import lombok.Data;

@Data
public class JSInjection {
    private JavaScript[] scripts;

    public JSInjection(JavaScript... scripts) {
        this.scripts = scripts;
    }
}
