package com.eriksandsten.homeautomation2.utils.injection;

import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.eriksandsten.homeautomation2.utils.TriFunction;
import java.util.Base64;
import lombok.Data;

@Data
public class LocalStylesheet {
    protected static final TriFunction<String, String, String, String> LOAD_JS = (parentElement, scriptSrcOrInnerHTMLSnippet, functionToCall) ->
            "if (!document.getElementById('%s')) {" +
                "var parentElement = document.getElementsByTagName('" + parentElement + "').item(0);" +
                "var stylesheet = document.createElement('style');" +
                "stylesheet.id = '%s';" +
                "stylesheet.innerHTML = %s;" +
                "parentElement['" + functionToCall + "'](stylesheet);" +
            "} else {" +
                "console.error('Page stylesheet [%s] already loaded');" +
            "}";
    private String stylesheetName;
    private String stylesheetContent;

    public LocalStylesheet() {
        super();
    }

    public LocalStylesheet(String stylesheetName, String stylesheetContent) {
        this.stylesheetName = stylesheetName;
        this.stylesheetContent = stylesheetContent;
    }

    public String loadCSSInHead() {
        return LOAD_JS.apply("head", "script.innerHTML = %s;", "appendChild");
    }

    public String getInjectionCode() {
        final String encodedStylesheet = Base64.getEncoder().encodeToString(stylesheetContent.getBytes());
        final String stylesheetName = getStylesheetName();
        return HomeAutomationUtils.ATOB_UTF8_FUNCTION +
                loadCSSInHead().formatted(stylesheetName, stylesheetName, "atobUTF8('%s')".formatted(encodedStylesheet), stylesheetName);
    }
}
