package com.eriksandsten.homeautomation2.utils.injection;

import lombok.Data;

@Data
public class CSSInjection {
    private InlineStylesheet[] stylesheets;

    public CSSInjection(InlineStylesheet... stylesheets) {
        this.stylesheets = stylesheets;
    }
}
