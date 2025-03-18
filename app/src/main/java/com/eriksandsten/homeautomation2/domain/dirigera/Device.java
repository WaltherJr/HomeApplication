package com.eriksandsten.homeautomation2.domain.dirigera;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Device implements Serializable {
    public String id;
    public String type;
    public String deviceType;
    public String createdAt;
    public Boolean isReachable;
    public String lastSeen;
    public DeviceAttributes attributes;
    public Room room;
}
