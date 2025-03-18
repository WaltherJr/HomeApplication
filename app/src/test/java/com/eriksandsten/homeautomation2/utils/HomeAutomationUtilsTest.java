package com.eriksandsten.homeautomation2.utils;

import org.junit.Assert;
import org.junit.Test;
import java.util.Map;

public class HomeAutomationUtilsTest {
    @Test
    public void testToJSON() {
        Assert.assertEquals("{\"message\": \"My message\", \"title\": \"My title\"}", HomeAutomationUtils.toJSON(Map.of("title", "My title", "message", "My message")));
        Assert.assertEquals("{\"message\": \"My \\\"message\\\"\", \"title\": \"My \\\"title\\\"\"}", HomeAutomationUtils.toJSON(Map.of("title", "My \"title\"", "message", "My \"message\"")));
    }
}
