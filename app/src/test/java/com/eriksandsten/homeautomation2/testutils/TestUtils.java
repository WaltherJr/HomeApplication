package com.eriksandsten.homeautomation2.testutils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public final class TestUtils {
    public static String normalizeJS(String unformattedJS) throws IOException {
        Process process = Runtime.getRuntime().exec("C:/Users/eriks/AppData/Roaming/npm/js-beautify.cmd --end-with-newline");

        OutputStream os = process.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(unformattedJS);
        writer.flush();
        writer.close();

        java.util.Scanner s = new java.util.Scanner(process.getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
