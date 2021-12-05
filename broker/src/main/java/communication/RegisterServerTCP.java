package communication;

import broker.Event;
import entities.Bs;
import entities.Ue;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.EntityType;
import types.EventType;

import java.io.IOException;
import java.net.*;
import java.util.Map;

public class RegisterServerTCP extends Thread implements RegisterServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServerTCP.class);

    private final int port;
    private final Map<Integer, Bs> listaBS;
    private final Map<Integer, Ue> listaUE;
    private final Map<Long, Event> events;
    private final double t;

    public RegisterServerTCP(double t, int port, Map<Integer, Bs> listaBS, Map<Integer, Ue> listaUE, Map<Long, Event> events) {
        this.port = port;
        this.listaBS = listaBS;
        this.listaUE = listaUE;
        this.events = events;
        this.t = t;
    }


    @Override
    public void run() {
        LOGGER.info("Registered entities:");
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                final Communicator communicatorTCP = new CommunicatorTCP(clientSocket);
                final MessageUnpacker unpacker = communicatorTCP.receiveMessage(0);
                final EntityType type = EntityType.getCommunicatorTypeTypeByCode(unpacker.unpackInt());

                if (type == EntityType.UNADMITTED) {
                    unpacker.close();
                    return;
                }

                final double x = unpacker.unpackDouble();
                final double y = unpacker.unpackDouble();
                unpacker.close();

                if (type == EntityType.USER_EQUIPMENT) {
                    final Ue ue = new Ue(x, y, communicatorTCP);
                    final long eventId = Event.getNextId();
                    final Event trafficIngress = new Event(eventId, t, EventType.TRAFFIC_INGRESS, ue);
                    listaUE.put(ue.getId(), ue);
                    events.put(trafficIngress.id(), trafficIngress);
                    ue.sendRegisterAck(ue.getId());
                    LOGGER.info("UE [id={}] {}", ue.getId(), communicatorTCP);
                } else if (type == EntityType.BASE_STATION) {
                    final Bs bs = new Bs(x, y, communicatorTCP);
                    final long eventId = Event.getNextId();
                    final Event newState = new Event(eventId, t, EventType.NEW_STATE, bs);
                    listaBS.put(bs.getId(), bs);
                    events.put(newState.id(), newState);
                    bs.sendRegisterAck(bs.getId());
                    LOGGER.info("BS [id={}] {}", bs.getId(), communicatorTCP);
                }

            }
        } catch (Exception e) {
            LOGGER.error("Log server error.. Execution completed", e);
            System.exit(-1);
        }
    }

    @Override
    public void closeRegister() {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            final DatagramSocket scAux = new DatagramSocket();
            final InetAddress adR = InetAddress.getByName("localhost");
            packer.packInt(0).close();
            final byte[] message = packer.toByteArray();
            final DatagramPacket dp = new DatagramPacket(message, message.length, adR, port);
            scAux.send(dp);
            scAux.close();
        } catch (IOException e) {
            LOGGER.error("Failed to shut down the log server. Execution completed", e);
            System.exit(-1);
        }
    }

    @Override
    public void closeSockets() {
        try {
            for (var entry : listaBS.entrySet())
                entry.getValue().closeSocket();

            for (var entry : listaUE.entrySet())
                entry.getValue().closeSocket();

        } catch (Exception e) {
            LOGGER.error("Failed to close the sockets. Execution completed", e);
            System.exit(-1);
        }
    }

}
