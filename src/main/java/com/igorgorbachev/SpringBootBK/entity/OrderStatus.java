package com.igorgorbachev.SpringBootBK.entity;

public enum OrderStatus {
    VPUTI("В пути"),
    PRIEHAL("Товар приехал"),
    VIDAN("Товар выдан");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }
}
