package broker;

import entities.Entity;
import types.EventType;

public record Event(EventType type, long id, double t, Entity entity) {

    private static long counter = 0;

    public static long getNextId() {
        return counter++;
    }

    public long getId() {
        return id;
    }

    public EventType getType() {
        return type;
    }

    public double getT() {
        return t;
    }

    public Entity getEntity() {
        return entity;
    }

}
