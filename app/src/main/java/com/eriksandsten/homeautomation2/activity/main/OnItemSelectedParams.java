package com.eriksandsten.homeautomation2.activity.main;

import android.view.View;
import android.widget.AdapterView;

public record OnItemSelectedParams(AdapterView<?> parent, View view, int position, long id) {
}
