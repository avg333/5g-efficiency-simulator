package entities;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class Ue extends Entity {

	private static int idCounter = 0;

	private double eL = 0.0;
	private double eA = 0.0;
	private long taskCounter = 0;

	public Ue(double x, double y, DatagramSocket sc, InetAddress ad, int port) {
		super(x, y, sc, ad, port);
		this.setId(idCounter++);
	}

	public void addTask(double x, double y, double l, double a) {
		setX(x);
		setY(y);
		eL += l;
		eA += a;
		taskCounter++;
	}

	public double geteL() {
		return taskCounter != 0 ? (eL / taskCounter) : 0;
	}

	public double geteA() {
		return taskCounter != 0 ? (eA / taskCounter) : 0;
	}

}
