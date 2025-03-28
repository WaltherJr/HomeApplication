package com.eriksandsten.homeautomation2.activity.main;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.widget.AppCompatSpinner;

import com.eriksandsten.homeautomation2.helper.SpinnerData;

import java.util.List;
import java.util.function.Consumer;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class SpinnerWithData<T> extends androidx.appcompat.widget.AppCompatSpinner {
    private final CustomSpinnerAdapter<T> adapter;
    @Getter
    @Setter
    private List<SpinnerData<T>> itemsList;
    @Getter
    @Setter
    private Consumer<OnItemSelectedParams> onItemSelectedCallback;

    public static <T> AppCompatSpinner factory(BaseActivity activity, int spinnerId, List<SpinnerData<T>> itemsList, Consumer<OnItemSelectedParams> onItemSelectedListener) {
        AppCompatSpinner spinner = activity.findViewById(spinnerId);
        spinner.setAdapter(new CustomSpinnerAdapter<T>(spinner.getContext(), itemsList));
        // spinner.setItemsList(itemsList);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onItemSelectedListener.accept(new OnItemSelectedParams(parent, view, position, id));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return spinner;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        setDropDownWidth(getWidth());
    }

    public SpinnerWithData(Context context, List<SpinnerData<T>> itemsList, Consumer<OnItemSelectedParams> onItemSelectedListener) {
        super(context);
        this.itemsList = itemsList;
        this.adapter = new CustomSpinnerAdapter<T>(context, this.itemsList);
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

    @Override
    public CustomSpinnerAdapter<T> getAdapter() {
        return adapter;
    }
}
