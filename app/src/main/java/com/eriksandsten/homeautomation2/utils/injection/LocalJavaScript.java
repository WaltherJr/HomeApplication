package com.eriksandsten.homeautomation2.utils.injection;

import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import java.util.Base64;
import lombok.Data;

@Data
public class LocalJavaScript extends JavaScript {
    private String scriptContent;

    public LocalJavaScript() {
        super();
    }

    public LocalJavaScript(String scriptName, DOMTarget domTarget, String scriptContent) {
        super(scriptName, domTarget);
        this.scriptContent = scriptContent;
    }

    public String loadJSInHead() {
        return LOAD_JS.apply("head", "script.innerHTML = %s;", "appendChild");
    }

    public String loadJSInBody() {
        return LOAD_JS.apply("body", "script.innerHTML = %s;", "appendChild");
    }

    @Override
    public String getInjectionCode() {
        final String encodedScript = Base64.getEncoder().encodeToString(getScriptContent().getBytes());
        final String scriptName = getScriptName();
        return HomeAutomationUtils.ATOB_UTF8_FUNCTION +
                (getDomTarget() == DOMTarget.BODY ? loadJSInBody() : loadJSInHead())
                        .formatted(scriptName, scriptName, "atobUTF8('%s')".formatted(encodedScript), scriptName);
    }
}
