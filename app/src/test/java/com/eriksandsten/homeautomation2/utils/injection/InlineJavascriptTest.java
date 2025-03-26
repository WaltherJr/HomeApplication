package com.eriksandsten.homeautomation2.utils.injection;

import com.eriksandsten.homeautomation2.testutils.TestUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class InlineJavascriptTest {

    @Test
    public void getInjectionCodeTest() throws IOException {
        String s = TestUtils.normalizeJS(new InlineJavascript("hello-world-script", DOMTarget.BODY, "alert('Hello world!');").getInjectionCode());

        Assert.assertEquals("""
if (typeof atobUTF8 !== 'function') {
    function atobUTF8(data) {
        const decodedData = atob(data);
        const utf8data = new Uint8Array(decodedData.length);
        const decoder = new TextDecoder("utf-8");

        for (let i = 0; i < decodedData.length; i++) {
            utf8data[i] = decodedData.charCodeAt(i);
        }

        return decoder.decode(utf8data);
    }
}
if (!document.getElementById('hello-world-script')) {
    var parentElement = document.getElementsByTagName('body').item(0);
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.id = 'hello-world-script';
    script.innerHTML = atobUTF8('YWxlcnQoJ0hlbGxvIHdvcmxkIScpOw==');
    parentElement['appendChild'](script);
} else {
    console.error('Page javascript [hello-world-script] already loaded');
}
""", s);
    }
}
