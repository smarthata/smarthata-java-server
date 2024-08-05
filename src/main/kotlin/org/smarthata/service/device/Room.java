package org.smarthata.service.device;

import org.jetbrains.annotations.NotNull;

public enum Room {
    ALL("Везде"),

    CANOPY("Навес"),
    STREET("Улица"),

    FLOOR("Первый этаж"),

    STAIRS("Лестница"),

    BEDROOM("Спальня"),
    BATHROOM("Ванная 2"),
    ROOM_EGOR("Детская Егора"),
    ROOM_LIZA("Детская Лизы"),

    GARAGE("Гараж"),
    WORKSHOP("Мастерская");


    public final String rusName;

    Room(String rusName) {
        this.rusName = rusName;
    }

    public String getRoomCode() {
        return name().toLowerCase().replace('_', '-');
    }

    public static Room getFromRoomCode(@NotNull String roomCode) {
        return valueOf(roomCode.toUpperCase().replace('-', '_'));
    }
}
