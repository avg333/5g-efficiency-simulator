package broker;

import entities.Entity;
import types.EventType;

public record Event(long id, double t, EventType type, Entity entity) {

    private static long counter = 0;

    public static long getNextId() {
        return counter++;
    }

}
