package com.eriksandsten.homeautomation2.utils.injection;

import com.eriksandsten.homeautomation2.utils.TriFunction;
import lombok.Data;

@Data
public abstract class Javascript {
    protected static final TriFunction<String, String, String, String> LOAD_JS = (parentElement, scriptSrcOrInnerHTMLSnippet, functionToCall) ->
            "if (!document.getElementById('%s')) {" +
                "var parentElement = document.getElementsByTagName('" + parentElement + "').item(0);" +
                "var script = document.createElement('script');" +
                "script.type = 'text/javascript';" +
                "script.id = '%s';" +
                scriptSrcOrInnerHTMLSnippet +
                "parentElement['" + functionToCall + "'](script);" +
            "} else {" +
                "console.error('Page javascript [%s] already loaded');" +
            "}";

    private String scriptName;
    private DOMTarget domTarget;

    public Javascript() {
    }

    public Javascript(String scriptName, DOMTarget domTarget) {
        this.scriptName = scriptName;
        this.domTarget = domTarget;
    }

    public abstract String getInjectionCode();
}
