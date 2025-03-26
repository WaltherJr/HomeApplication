package com.eriksandsten.homeautomation2.utils.injection;


import com.eriksandsten.homeautomation2.testutils.TestUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class InlineStylesheetTest {

    @Test
    public void getInjectionCodeTest() throws IOException, InterruptedException {
        String s = TestUtils.normalizeJS(new InlineStylesheet("hello-world-stylesheet", "html, body {margin: 0; padding: 0;}").getInjectionCode());

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
if (!document.getElementById('hello-world-stylesheet')) {
    var parentElement = document.getElementsByTagName('head').item(0);
    var stylesheet = document.createElement('style');
    stylesheet.id = 'hello-world-stylesheet';
    stylesheet.innerHTML = atobUTF8('aHRtbCwgYm9keSB7bWFyZ2luOiAwOyBwYWRkaW5nOiAwO30=');
    parentElement['appendChild'](stylesheet);
} else {
    console.error('Page stylesheet [hello-world-stylesheet] already loaded');
}
""", s);
    }
}
