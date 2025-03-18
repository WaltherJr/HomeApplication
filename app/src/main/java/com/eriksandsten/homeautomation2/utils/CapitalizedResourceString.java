package com.eriksandsten.homeautomation2.utils;

import android.content.res.Resources;

public class CapitalizedResourceString extends ResourceString {
    public CapitalizedResourceString(int resourceId) {
        super(resourceId);
    }

    public static ResourceString from(int resourceId) {
        return ResourceString.from(resourceId);
    }

    @Override
    public String getString(Resources resources) {
        String s = resources.getString(resourceId);
        return s.charAt(0) + s.substring(1);
    }
}
