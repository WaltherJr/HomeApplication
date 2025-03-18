package com.eriksandsten.homeautomation2.helper;

import lombok.Getter;

@Getter
public class SpinnerData<T> {
    private String text;
    private T data;

    public SpinnerData(String text, T data) {
        this.text = text;
        this.data = data;
    }
}
