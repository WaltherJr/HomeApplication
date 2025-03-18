package com.eriksandsten.homeautomation2.domain.dirigera;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceAttributes implements Serializable {
    public String customName;
    public String model;
    public String manufacturer;
    public String firmwareVersion;
    public String hardwareVersion;
    public String serialNumber;
    public String productCode;
    public Boolean isOn;

    public String startupOnOff;
    public Short lightLevel;
    public Short startUpConcurrentLevel;
    public String identifyStarted;
    public Short identifyPeriod;
    public Boolean permittingJoin;
    public String otaStatus;
    public String otaState;
    public Short otaProgress;
    public String otaPolicy;
    public String otaScheduleStart;
    public String otaScheduleEnd;

    public Integer blindsCurrentLevel;
    public Integer blindsTargetLevel;
    public String blindsState;
}
