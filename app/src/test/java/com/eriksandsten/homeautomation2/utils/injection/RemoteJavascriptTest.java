package com.eriksandsten.homeautomation2.utils.injection;

import com.eriksandsten.homeautomation2.testutils.TestUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class RemoteJavascriptTest {

    @Test
    public void getInjectionCodeTest() throws IOException, InterruptedException {
        String s = TestUtils.normalizeJS(new RemoteJavascript("jquery-script", DOMTarget.BODY,
                "https://code.jquery.com/jquery-3.7.1.min.js").getInjectionCode());

        Assert.assertEquals("""
if (!document.getElementById('jquery-script')) {
    var parentElement = document.getElementsByTagName('body').item(0);
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.id = 'jquery-script';
    script.src = 'https://code.jquery.com/jquery-3.7.1.min.js';
    parentElement['appendChild'](script);
} else {
    console.error('Page javascript [jquery-script] already loaded');
}
""", s);
    }

    @Test
    public void getInjectionCodeTest_withScriptIntegrity() throws IOException, InterruptedException {
        String s = TestUtils.normalizeJS(new RemoteJavascript("jquery-script", DOMTarget.BODY,
                "https://code.jquery.com/jquery-3.7.1.min.js", "sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=").getInjectionCode());

        Assert.assertEquals("""
if (!document.getElementById('jquery-script')) {
    var parentElement = document.getElementsByTagName('body').item(0);
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.id = 'jquery-script';
    script.src = 'https://code.jquery.com/jquery-3.7.1.min.js';
    script.integrity = 'sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=';
    script.crossOrigin = 'anonymous';
    parentElement['appendChild'](script);
} else {
    console.error('Page javascript [jquery-script] already loaded');
}
""", s);
    }
}
