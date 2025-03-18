package com.eriksandsten.homeautomation2.utils.injection;

import lombok.Data;

@Data
public class JavaScriptByURL extends JavaScript {
    private String scriptUrl;
    private String scriptIntegrity;

    public JavaScriptByURL() {
        super();
    }

    public JavaScriptByURL(String scriptName, DOMTarget domTarget, String scriptUrl) {
        super(scriptName, domTarget);
        this.scriptUrl = scriptUrl;
        this.scriptIntegrity = null;
    }

    public JavaScriptByURL(String scriptName, DOMTarget domTarget, String scriptUrl, String scriptIntegrity) {
        super(scriptName, domTarget);
        this.scriptUrl = scriptUrl;
        this.scriptIntegrity = scriptIntegrity;
    }

    public String loadJSInHead() {
        return LOAD_JS.apply("head", "script.src = '%s';".formatted(scriptUrl) + (scriptIntegrity != null ? "\nscript.integrity = '%s';\nscript.crossOrigin = 'anonymous';".formatted(scriptIntegrity) : ""), "appendChild");
    }

    public String loadJSInBody() {
        return LOAD_JS.apply("body", "script.src = '%s';".formatted(scriptUrl) + (scriptIntegrity != null ? "\nscript.integrity = '%s';\nscript.crossOrigin = 'anonymous';".formatted(scriptIntegrity) : ""), "appendChild");
    }

    @Override
    public String getInjectionCode() {
        String scriptName = getScriptName();
        String s = getDomTarget() == DOMTarget.BODY ? loadJSInBody() : loadJSInHead();
        return s.formatted(scriptName, scriptName, scriptName);
    }
}
