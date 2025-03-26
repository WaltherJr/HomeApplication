package com.eriksandsten.homeautomation2.activity.main;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.helper.SpinnerData;

import java.util.List;
import java.util.function.Consumer;

public class SpinnerWithData<T> extends androidx.appcompat.widget.AppCompatSpinner {
    private final List<SpinnerData<T>> itemsList;
    private final CustomSpinnerAdapter<T> adapter;

    public SpinnerWithData(Context context, List<SpinnerData<T>> itemsList, Consumer<OnItemSelectedParams> onItemSelectedListener) {
        super(context);
        this.itemsList = itemsList;
        this.adapter = new CustomSpinnerAdapter<T>(context, itemsList);
        setAdapter(adapter);

        setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onItemSelectedListener.accept(new OnItemSelectedParams(parent, view, position, id));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


    }
}
