package com.eriksandsten.homeautomation2.utils.injection;

import lombok.Data;

@Data
public class CSSInjection {
    private LocalStylesheet[] stylesheets;

    public CSSInjection(LocalStylesheet... stylesheets) {
        this.stylesheets = stylesheets;
    }
}
