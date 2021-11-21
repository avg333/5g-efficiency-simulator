package broker;

import entities.Entity;
import types.EventType;

public class Event {

	private static long counter = 0;

	private final long id;
	private final EventType type;
	private final double t;
	private final Entity entity;

	public Event(final EventType type, final double t, final Entity entity) {
		this.id = counter++;
		this.type = type;
		this.t = t;
		this.entity = entity;
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
