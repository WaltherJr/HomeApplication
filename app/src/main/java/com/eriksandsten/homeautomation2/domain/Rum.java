package com.eriksandsten.homeautomation2.domain;

import com.eriksandsten.homeautomation2.domain.dirigera.Device;
import java.util.List;
import lombok.Data;

@Data
public class Rum {
    private String roomName;
    private RoomType roomType;
    private List<Device> devices;

    public Rum(String roomName, RoomType roomType, List<Device> devices) {
        this.roomName = roomName;
        this.roomType = roomType;
        this.devices = devices;
    }

    public static RoomType mapRoomNameToRoomType(String roomName) {
        return switch (roomName.toLowerCase().trim()) {
            case "tvättrum" -> RoomType.LAUNDRY_ROOM;
            case "vardagsrum" -> RoomType.LIVING_ROOM;
            case "sovrum" -> RoomType.BEDROOM;
            case "hall" -> RoomType.HALLWAY;
            default -> RoomType.UNKNOWN;
        };
    }

    public String getRoomTypeClassName() {
        return "room-type-" + roomType.toString().replace("_", "-").toLowerCase();
    }
}
