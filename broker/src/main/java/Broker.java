import entities.Bs;
import entities.Ue;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import types.actionType;
import types.stateType;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Broker extends Thread {

    public static final char VECTOR_DE_DISTANCIAS = 'v';
    protected static double t = 0;
    protected static Map<Integer, Bs> listaBS = new TreeMap<>();
    protected static Map<Integer, Ue> listaUE = new TreeMap<>();
    protected static Map<Long, Evento> listaEventos = new TreeMap<>();
    private static double T_FINAL;
    private static char algoritmo = VECTOR_DE_DISTANCIAS;
    private static long contadorTareas = 0;

    public Broker(String[] args) {
        int puerto = 3000;
        boolean verbosity = false;
        boolean eventos = false;
        T_FINAL = Double.parseDouble(args[0]);

        int i = 0;
        while (i < args.length) {
            switch (args[i]) {
                case "-p", "--port" -> puerto = Integer.parseInt(args[++i]);
                case "-a", "--algorithm" -> algoritmo = args[++i].charAt(0);
                case "-v", "--verbosity" -> verbosity = true;
                case "-c", "--csv" -> eventos = true;
                case "-h", "--help" -> {
                    System.out.println(HELP_MSG);
                    System.exit(0);
                }
            }
            i++;
        }

        System.out.println("Broker iniciado con los par�metros:\n\tport=" + puerto + " algorithm=" + algoritmo
                + " verbosity=" + verbosity + " csv=" + eventos);
        System.out.print("\nPulsa enter para iniciar la simulaci�n. ");

        new HiloServidorRegistro(puerto).start();
        Logger.setSettings(verbosity, eventos);
    }

    public static void main(String[] args) {
        new Broker(args).run();
    }

    private static Evento obtenerProximoEvento() {
        Evento evento = null;

        double t = -1;
        for (Map.Entry<Long, Evento> entry : listaEventos.entrySet()) {
            Evento eventoAux = entry.getValue();
            double tAux = eventoAux.getT();
            if (tAux < t || evento == null) {
                t = tAux;
                evento = eventoAux;
            }
        }

        return evento;
    }

    private static void procesarEvento(Evento evento) {
        if (evento == null)
            return;

        listaEventos.remove(evento.getId());
        t = evento.getT();

        try {
			switch (evento.getTipo()) {
                case TRAFFIC_INGRESS -> procesarTRAFFIC_INGRESS(evento);
				case TRAFFIC_EGRESS -> procesarTRAFFIC_EGRESS(evento);
				case NEW_STATE -> procesarNEW_STATE(evento);
			}
        } catch (Exception e) {
            System.out.println("Error al intentar empaquetar/desempaquetar un mensaje. Ejecuci�n finalizada");
            System.exit(-1);
        }

    }

    private static void procesarTRAFFIC_INGRESS(Evento evento) throws IOException {
        Ue ue = (Ue) evento.getEntidad();
        MessageBufferPacker requestTI = MessagePack.newDefaultBufferPacker();
        int eventCode = actionType.getCodeByActionType(actionType.TRAFFIC_INGRESS);
        requestTI.packInt(eventCode).close();
        MessageUnpacker responseTI = ue.communicate(requestTI);

        long idTarea = contadorTareas++;
        double xUe = responseTI.unpackDouble();
        double yUe = responseTI.unpackDouble();
        double size = responseTI.unpackDouble();
        double delay = responseTI.unpackDouble();
        responseTI.close();

        Evento trafficIngress = new Evento(actionType.TRAFFIC_INGRESS, t + delay, ue);
        listaEventos.put(trafficIngress.getId(), trafficIngress);

        if (size == -1)
            return;

        ue.addTask(xUe, yUe, size, delay);
        Logger.logTRAFFIC_INGRESS(t, ue.getId(), xUe, yUe, idTarea, size, delay);

        Bs bs = obtenerBS(xUe, yUe);
        Logger.logTRAFFIC_ROUTE(t, ue.getId(), bs.getId(), idTarea, size);

        MessageBufferPacker requestTA = MessagePack.newDefaultBufferPacker();
        eventCode = actionType.getCodeByActionType(actionType.TRAFFIC_ARRIVE);
        requestTA.packInt(eventCode).packDouble(t).packLong(idTarea).packDouble(size);
        requestTA.close();
        MessageUnpacker responseTA = bs.communicate(requestTA);

        double q = responseTA.unpackDouble();
        stateType state = stateType.getStateTypeByCode(responseTA.unpackInt());
        double tTrafficEgress = responseTA.unpackDouble();
        double tNewState = responseTA.unpackDouble();
        int nextState = responseTA.unpackInt();
        double a = responseTA.unpackDouble();
        responseTA.close();

        Logger.logTRAFFIC_ARRIVAL(t, bs.getId(), idTarea, size, q, a);

        if (bs.getState() == stateType.HISTERISIS) {
            Logger.logNEW_STATE(t, bs.getId(), q, state);
            listaEventos.remove(bs.getIdEventNextState());
        } else if (state != bs.getState())
            Logger.logNEW_STATE(t, bs.getId(), q, state);

        crearEventos(bs, tNewState, tTrafficEgress, stateType.getStateTypeByCode(nextState));

        bs.addQ(q, t);
        bs.setState(state);
    }

    private static void procesarTRAFFIC_EGRESS(Evento evento) throws IOException {
        Bs bs = (Bs) evento.getEntidad();
        int eventCode = actionType.getCodeByActionType(actionType.TRAFFIC_EGRESS);

        MessageBufferPacker requestTE = MessagePack.newDefaultBufferPacker();
        requestTE.packInt(eventCode).packDouble(t).close();
        MessageUnpacker responseTE = bs.communicate(requestTE);

        double q = responseTE.unpackDouble();
        stateType state = stateType.getStateTypeByCode(responseTE.unpackInt());
        double tTrafficEgress = responseTE.unpackDouble();
        double tNewState = responseTE.unpackDouble();
        int nextState = responseTE.unpackInt();
        double w = responseTE.unpackDouble();
        long id = responseTE.unpackLong();
        double size = responseTE.unpackDouble();
        responseTE.close();

        Logger.logTRAFFIC_EGRESS(t, bs.getId(), id, size, q, w);

        if (state != bs.getState())
            Logger.logNEW_STATE(t, bs.getId(), q, state);

        crearEventos(bs, tNewState, tTrafficEgress, stateType.getStateTypeByCode(nextState));

        bs.addQ(q, t);
        bs.addW(w);
        bs.setState(state);
    }

    private static void procesarNEW_STATE(Evento evento) throws IOException {
        Bs bs = (Bs) evento.getEntidad();
        stateType nextState = bs.getNextStateBs();
        int eventCode = actionType.getCodeByActionType(actionType.NEW_STATE);
        int eventCode2 = stateType.getCodeByStateType(nextState);

        MessageBufferPacker requestNS = MessagePack.newDefaultBufferPacker();
        requestNS.packInt(eventCode).packInt(eventCode2).close();
        MessageUnpacker responseNS = bs.communicate(requestNS);

        double q = responseNS.unpackDouble();
        stateType state = stateType.getStateTypeByCode(responseNS.unpackInt());
        double tTrafficEgress = responseNS.unpackDouble();
        double tNewState = responseNS.unpackDouble();
        nextState = stateType.getStateTypeByCode(responseNS.unpackInt());
        responseNS.close();

        if (state != bs.getState())
            Logger.logNEW_STATE(t, bs.getId(), q, state);

        crearEventos(bs, tNewState, tTrafficEgress, nextState);

        bs.setState(state);
    }

    private static void crearEventos(Bs bs, double tNewState, double tTrafficEgress, stateType nextState) {
        if (tNewState > 0) {
            Evento newState = new Evento(actionType.NEW_STATE, t + tNewState, bs);
            listaEventos.put(newState.getId(), newState);
            bs.setNextState(nextState);
            bs.setIdEventNextState(newState.getId());
        }

        if (tTrafficEgress > -1) {
            Evento trafficEgress = new Evento(actionType.TRAFFIC_EGRESS, t + tTrafficEgress, bs);
            listaEventos.put(trafficEgress.getId(), trafficEgress);
        }
    }

    private static Bs obtenerBS(double xUe, double yUe) {

        if (listaBS.isEmpty()) {
            System.out.println("Error: La lista de BS est� vac�a. Ejecuci�n finalizada");
            System.exit(-1);
        }

        Bs bs = null;
        double distanciaMin = -1, distancia = 0;

        for (Map.Entry<Integer, Bs> entry : listaBS.entrySet()) {
            Bs bsAux = entry.getValue();
            switch (algoritmo) {
                case VECTOR_DE_DISTANCIAS:
                    distancia = vectorDeDistancias(xUe, yUe, bsAux.getX(), bsAux.getY());
                    break;
            }
            if (distancia < distanciaMin || bs == null) {
                distanciaMin = distancia;
                bs = bsAux;
            }
        }

        return bs;
    }

    public static double vectorDeDistancias(double xUe, double yUe, double xBs, double yBs) {
        final double cateto1 = xUe - xBs;
        final double cateto2 = yUe - yBs;
        return Math.sqrt(cateto1 * cateto1 + cateto2 * cateto2);
    }

    @Override
    public void run() {
        Scanner in = new Scanner(System.in);
        in.nextLine();
        in.close();
        HiloServidorRegistro.cerrarServidorRegistro();

        final long start = System.currentTimeMillis();
        while (t <= T_FINAL) {
            final Evento evento = obtenerProximoEvento();
            procesarEvento(evento);
            Logger.printProgress(t, T_FINAL);
        }
        final long finish = System.currentTimeMillis();

        HiloServidorRegistro.cerrarSockets();
        Logger.imprimirResultados(finish - start);
    }


    private static final String HELP_MSG ="""
                        Argumentos obligatorios:
                        \t<T_FINAL>
                         Argumentos opcionales:
                        \t[-p|--port <PUERTO>] [-a|--algorithm <ALGORITMO>] [-v|--verbosity] [-c|--csv] [-h|--help]
                        "Informaci�n sobre los argumentos:
                        \t<T_FINAL>\tValor m�ximo de tiempo que puede alcanzar la simulaci�n.
                        \t-p|--port <PUERTO>
                        \t\t\tCambia el puerto en el que se esperan comunicaciones de las entidades.
                        \t\t\tValor por defecto: 3000.
                        \t-a|--algorithm <ALGORITMO>
                        \t\t\tDefine el algoritmo a usar para el encaminamiento de tareas. Valores permitidos:
                        \t\t\t\tv[ector de distancias]
                        \t\t\tValor por defecto: v.
                        \t-c|--csv\tGenera un archivo csv al final de la simulaci�n con todos los eventos y su informaci�n.
                        \t-v|--verbosity\tImprime por consola los eventos con su informaci�n a medida que suceden en la simulaci�n.
                        \t-h|--help\tImprime por consola la lista de posibles argumentos con su explicaci�n.""";

}
