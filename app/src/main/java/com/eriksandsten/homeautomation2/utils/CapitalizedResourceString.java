package com.eriksandsten.homeautomation2.utils;

import android.content.res.Resources;

public class CapitalizedResourceString extends ResourceString {
    public CapitalizedResourceString(int resourceId) {
        super(resourceId);
    }

    public static ResourceString from(int resourceId) {
        return new CapitalizedResourceString(resourceId);
    }

    @Override
    public String getString(Resources resources) {
        String str = resources.getString(resourceId);
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
