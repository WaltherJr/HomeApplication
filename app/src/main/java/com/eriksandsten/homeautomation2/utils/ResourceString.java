package com.eriksandsten.homeautomation2.utils;

import android.content.res.Resources;

public class ResourceString {
    protected int resourceId;

    protected ResourceString(int resourceId) {
        this.resourceId = resourceId;
    }

    public static ResourceString from(int resourceId) {
        return new ResourceString(resourceId);
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getString(Resources resources) {
        return resources.getString(resourceId);
    }
}
