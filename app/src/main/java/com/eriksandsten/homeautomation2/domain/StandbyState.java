package com.eriksandsten.homeautomation2.domain;

import lombok.Getter;

public enum StandbyState {
    ON("on"), OFF("off");

    @Getter
    private String value;

    StandbyState(String value) {
        this.value = value;
    }
}
