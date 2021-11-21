package loggers;

import entities.Bs;
import entities.Ue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.StateType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class LoggerCustom {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerCustom.class);

	private static final int AVANCE = 1;
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private static final Date date = new Date();
	private static final DecimalFormat DF_CSV = new DecimalFormat();
	private static final DecimalFormat DF_LOG = new DecimalFormat("#.###");
	private static final String SEPARADOR = ";";
	private static final String COLUMNAS = "T" + SEPARADOR + "ENTIDAD" + SEPARADOR + "ID" + SEPARADOR + "EVENTO"
			+ SEPARADOR + "TAREA" + SEPARADOR + "L" + SEPARADOR + "A" + SEPARADOR + "X" + SEPARADOR + "Y" + SEPARADOR
			+ "FROM-UE" + SEPARADOR + "TO-BS" + SEPARADOR + "Q" + SEPARADOR + "W" + SEPARADOR + "STATE";

	private static int aux = AVANCE;
	private static boolean verbosity;
	private static boolean eventos;
	private static final ArrayList<String> listaEventos = new ArrayList<>();

	private LoggerCustom() {

	}

	public static void setSettings(boolean verbosity, boolean eventos) {
		LoggerCustom.verbosity = verbosity;
		LoggerCustom.eventos = eventos;
	}

	public static void printProgress(double current, double total) {
		if ((current / total * 100 <= aux) || verbosity) {
			return;
		}

		aux += AVANCE;

		int percent = (int) (current * 100 / total);
		String string = '\r' +
				String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")) +
				String.format(" %d%% [", percent) + String.join("", Collections.nCopies(percent, "=")) +
				'>' + String.join("", Collections.nCopies(100 - percent, " ")) + ']' +
				String.join("",
						Collections.nCopies(current == 0 ? (int) (Math.log10(total))
								: (int) (Math.log10(total)) - (int) (Math.log10(current)), " ")) +
				String.format(" %d/%d", (int) current, (int) total);

		System.out.print(string);
	}

	public static void imprimirResultados(long elapsedTime, double t, Map<Integer, Bs> listaBS, Map<Integer, Ue> listaUE) {
		LOGGER.info("Fin de la simulacion. Tiempo de ejecucion: {}s", elapsedTime / 1000);
		imprimirResumen(elapsedTime, t, listaBS, listaUE);
		imprimirEventos();
	}

	public static void imprimirResumen(long elapsedTime, double t, Map<Integer, Bs> listaBS, Map<Integer, Ue> listaUE) {
		double eQ = 0;
		double eW = 0;
		double eL = 0;
		double eA = 0;

		for (Map.Entry<Integer, Bs> entry : listaBS.entrySet()) {
			Bs bsAux = entry.getValue();
			eQ += bsAux.getEq();
			eW += bsAux.getEw();
		}

		eQ = eQ / listaBS.size();
		eW = eW / listaBS.size();

		for (Map.Entry<Integer, Ue> entry : listaUE.entrySet()) {
			Ue ueAux = entry.getValue();
			eL += ueAux.geteL();
			eA += ueAux.geteA();
		}

		eL = eL / listaUE.size();
		eA = eA / listaUE.size();

		try (final BufferedWriter writer = new BufferedWriter(new FileWriter("resumen_" + formatter.format(date) + ".txt"))) {
			writer.write("Resumen simulaci�n " + formatter.format(date) + ":\n");
			writer.write("Log: " + verbosity + ". Eventos: " + eventos + ". T final: " + DF_LOG.format(t)
					+ ". Tiempo de simulaci�n: " + DF_LOG.format(elapsedTime / 1000) + "s.\n");
			writer.write("N: " + listaBS.size() + ". E[Q] Global: " + DF_LOG.format(eQ) + ". E[W] Global: "
					+ DF_LOG.format(eW) + ".\n");
			for (Map.Entry<Integer, Bs> entry : listaBS.entrySet())
				writer.write(
						"\tBS ID: " + entry.getValue().getId() + " E[Q]: " + DF_LOG.format(entry.getValue().getEq())
								+ " E[W]: " + DF_LOG.format(entry.getValue().getEw()) + ".\n");
			writer.write("m: " + listaUE.size() + ". E[L] Global: " + DF_LOG.format(eL) + ". E[A] Global: "
					+ DF_LOG.format(eA) + ".\n");
			for (Map.Entry<Integer, Ue> entry : listaUE.entrySet())
				writer.write(
						"\tUE ID: " + entry.getValue().getId() + " E[L]: " + DF_LOG.format(entry.getValue().geteL())
								+ " E[A]: " + DF_LOG.format(entry.getValue().geteA()) + ".\n");
		} catch (IOException e) {
			LOGGER.error("Error al imprimir el archivo resumen.txt.", e);
		}
	}

	public static void imprimirEventos() {
		if (listaEventos.isEmpty())
			return;

		try (final BufferedWriter writer = new BufferedWriter(new FileWriter("eventos_" + formatter.format(date) + ".csv"))) {
			writer.write(COLUMNAS + "\n");
			for (String listaEvento : listaEventos) writer.write(listaEvento + "\n");
		} catch (IOException e) {
			LOGGER.error("Error al imprimir el archivo eventos.csv.", e);
		}
	}

	public static void logTrafficIngress(double t, int idUe, double xUe, double yUe, long idTarea, double size,
										 double delay) {
		if (verbosity) {
			LOGGER.debug("{} UE {} TRAFFIC_INGRESS id={} size={} next={} x={} y={}", t, idUe, idTarea, size, delay, xUe, yUe);
		}
		if (eventos)
			listaEventos.add(DF_CSV.format(t) + SEPARADOR + "UE" + SEPARADOR + idUe + SEPARADOR + "TRAFFIC_INGRESS"
					+ SEPARADOR + idTarea + SEPARADOR + DF_CSV.format(size) + SEPARADOR + DF_CSV.format(delay)
					+ SEPARADOR + DF_CSV.format(xUe) + SEPARADOR + DF_CSV.format(yUe));
	}

	public static void logTrafficRoute(double t, int idUe, int idBs, long idTarea, double size) {
		if (verbosity) {
			LOGGER.debug("{} BK 0 TRAFFIC_ROUTE id={} size={} from-ue={} to-bs={}", t, idTarea, size, idUe, idBs);
		}
		if (eventos)
			listaEventos.add(DF_CSV.format(t) + SEPARADOR + "BK" + SEPARADOR + "0" + SEPARADOR + "TRAFFIC_ROUTE"
					+ SEPARADOR + idTarea + SEPARADOR + DF_CSV.format(size) + SEPARADOR + SEPARADOR + SEPARADOR
					+ SEPARADOR + idUe + SEPARADOR + idBs);
	}

	public static void logTrafficArrival(double t, int idBs, long idDemanda, double cantidad, double cola, double a) {
		if (verbosity) {
			LOGGER.debug("{} BS {} TRAFFIC_ARRIVAL id={} size={} a={} q={}", t, idBs, idDemanda, cantidad, a, cola);
		}
		if (eventos)
			listaEventos.add(DF_CSV.format(t) + SEPARADOR + "BS" + SEPARADOR + idBs + SEPARADOR + "TRAFFIC_ARRIVAL"
					+ SEPARADOR + idDemanda + SEPARADOR + DF_CSV.format(cantidad) + SEPARADOR + DF_CSV.format(a)
					+ SEPARADOR + SEPARADOR + SEPARADOR + SEPARADOR + SEPARADOR + DF_CSV.format(cola) + SEPARADOR
					+ SEPARADOR);
	}

	public static void logTrafficEgress(double t, int idBs, long idDemanda, double cantidad, double cola,
										double wait) {
		if (verbosity) {
			LOGGER.debug("{} BS {} TRAFFIC_EGRESS id={} size={} q={} wait={}", t, idBs, idDemanda, cantidad, cola, wait);
		}
		if (eventos)
			listaEventos.add(DF_CSV.format(t) + SEPARADOR + "BS" + SEPARADOR + idBs + SEPARADOR + "TRAFFIC_EGRESS"
					+ SEPARADOR + idDemanda + SEPARADOR + DF_CSV.format(cantidad) + SEPARADOR + SEPARADOR + SEPARADOR
					+ SEPARADOR + SEPARADOR + SEPARADOR + DF_CSV.format(cola) + SEPARADOR + DF_CSV.format(wait)
					+ SEPARADOR);
	}

	public static void logNewState(double t, int idBs, double q, StateType estadoBs) {
		String state;
		switch (estadoBs) {
			case ON -> state = "on";
			case OFF -> state = "off";
			case TO_ON -> state = "to_on";
			case TO_OFF -> state = "to_off";
			default -> {
				return;
			}

		}

		if (verbosity) {
			LOGGER.debug("{} BS {} NEW_STATE q={} state={}", t, idBs, q, state);
		}
		if (eventos) {
			listaEventos.add(DF_CSV.format(t) + SEPARADOR + "BS" + SEPARADOR + idBs + SEPARADOR + "NEW_STATE"
					+ SEPARADOR + SEPARADOR + SEPARADOR + SEPARADOR + SEPARADOR + SEPARADOR + SEPARADOR + SEPARADOR
					+ DF_CSV.format(q) + SEPARADOR + SEPARADOR + state);
		}
	}

}
