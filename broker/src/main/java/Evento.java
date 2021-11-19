import entities.Entity;
import types.actionType;

public class Evento {

	private static long contador = 0;

	private long id;
	private actionType tipo;
	private double t;
	private Entity entity;

	public Evento(actionType tipo, double t, Entity entity) {
		this.id = contador++;
		this.tipo = tipo;
		this.t = t;
		this.entity = entity;
	}

	public long getId() {
		return id;
	}

	public actionType getTipo() {
		return tipo;
	}

	public double getT() {
		return t;
	}

	public Entity getEntidad() {
		return entity;
	}

}
