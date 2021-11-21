package loggers;

import entities.Bs;
import entities.Ue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Depuracion {

	private static final DecimalFormat DF_CSV = new DecimalFormat();
	private static final String SEPARADOR = ";";

	private static final double AVANCE = 0.1;
	private static double aux = AVANCE;

	protected static final List<MyVector> lista = new ArrayList<>();
	protected static final List<String> progresUE = new ArrayList<>();
	protected static final List<String> progresBS = new ArrayList<>();

	private Depuracion() {

	}

	public static void imprimirLista() {
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter("log_TIME" + ".csv"))) {
			writer.write("X" + SEPARADOR + "Y\n");
			for (MyVector myVector : lista) writer.write(myVector.getT() + SEPARADOR + myVector.getTime() + "\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void addVector(long t, long time) {
		if (t / 1000.0 >= aux) {
			MyVector vector = new MyVector(t, time);
			lista.add(vector);
			aux += AVANCE;
		}
	}

	public static void addProgressUE(long t, double total, Ue ue) {
		if (t / total * 100 <= aux) {
			return;
		}
		aux += AVANCE;
		String entrada = DF_CSV.format(t) + SEPARADOR + DF_CSV.format(ue.geteL()) + SEPARADOR
				+ DF_CSV.format(ue.geteA()) + ";1;1,25" + SEPARADOR
				+ DF_CSV.format(Math.abs((ue.geteL() - 1) * 100)) + SEPARADOR
				+ DF_CSV.format(Math.abs((ue.geteA() - 1.25) / 1.25 * 100));
		progresUE.add(entrada);
	}

	public static void addProgressBS(long t, double total, Bs bs) {
		if (t / total * 100 <= aux) {
			return;
		}
		aux += AVANCE;
		String entrada = DF_CSV.format(t) + SEPARADOR + DF_CSV.format(bs.getEq()) + SEPARADOR
				+ DF_CSV.format(bs.getEw()) + ";3,2;4" + SEPARADOR
				+ DF_CSV.format(Math.abs((bs.getEq() - 3.2) / 3.2 * 100)) + SEPARADOR
				+ DF_CSV.format(Math.abs((bs.getEw() - 4.0) / 4.0 * 100));
		progresBS.add(entrada);
	}

	public static void imprimirProgresionUE() {
		String columnas = "T;E[L];E[A];E[L] Te�rico;E[A] Te�rico;Error E[L];Error E[A]";
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter("PROGRESSUE.csv"))) {
			writer.write(columnas + "\n");
			for (String s : progresUE) writer.write(s + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void imprimirProgresionBS() {
		String columnas = "T;E[Q];E[W];E[Q] Te�rico;E[W] Te�rico;Error E[Q];Error E[W]";
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter("PROGRESSBS.csv"))) {
			writer.write(columnas + "\n");
			for (String progresB : progresBS) writer.write(progresB + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printGrid(Ue ue, Bs bs, int filas, int columnas, Map<Integer, Bs> listaBS, Map<Integer, Ue> listaUE) {

		char[][] mapa = new char[filas][columnas * 2];

		for (int i = 0; i < mapa.length; i++) {
			for (int j = 0; j < mapa[i].length; j++) {
				if (j % 2 == 0)
					mapa[i][j] = ' ';
				else
					mapa[i][j] = SEPARADOR.charAt(0);
			}
		}

		for (Map.Entry<Integer, Bs> entry : listaBS.entrySet()) {
			int x = (int) entry.getValue().getX();
			int y = (int) entry.getValue().getY();
			mapa[x][y * 2] = 'B';
		}

		for (Map.Entry<Integer, Ue> entry : listaUE.entrySet()) {
			int x = (int) entry.getValue().getX();
			int y = (int) entry.getValue().getY();
			mapa[x][y * 2] = 'U';
		}

		mapa[(int) ue.getX()][(int) ue.getY() * 2] = Character.forDigit(ue.getId(), 10);
		mapa[(int) bs.getX()][(int) bs.getY() * 2] = Character.forDigit(bs.getId(), 10);

		try (final BufferedWriter writer = new BufferedWriter(new FileWriter("ROUTE_GRID.csv"))) {
			for (char[] chars : mapa) {
				writer.write(new String(chars) + "\n");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	record MyVector(long t, long time) {

		public long getT() {
			return t;
		}

		public long getTime() {
			return time;
		}
	}
}
