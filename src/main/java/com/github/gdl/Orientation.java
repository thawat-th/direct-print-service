package com.github.gdl;

import java.util.HashMap;
import java.util.Map;

public enum Orientation {

    LANDSCAPE(0),
    PORTRAIT(1),
    REVERSE_LANDSCAPE(2);

    private int value;
    private static Map map = new HashMap<>();

    private Orientation(int value) {
        this.value = value;
    }

    static {
        for (Orientation orientation : Orientation.values()) {
            map.put(orientation.value, orientation);
        }
    }

    public static Orientation valueOf(int pageType) {
        return (Orientation) map.get(pageType);
    }

    public int getValue() {
        return value;
    }

}
