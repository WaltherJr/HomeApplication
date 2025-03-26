package com.eriksandsten.homeautomation2.domain;

import com.eriksandsten.homeautomation2.domain.dirigera.Device;
import java.util.List;
import lombok.Data;

@Data
public class Room {
    private String name;
    private RoomType type;
    private List<Device> associatedDevices;

    public Room(String name, RoomType type, List<Device> associatedDevices) {
        this.name = name;
        this.type = type;
        this.associatedDevices = associatedDevices;
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
        return "room-type-" + type.toString().replace("_", "-").toLowerCase();
    }
}
