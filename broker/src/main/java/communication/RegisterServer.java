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
import java.util.Map;

public abstract class RegisterServer extends Thread {
    final int port;
    final Map<Integer, Bs> listBs;
    final Map<Integer, Ue> listUe;
    final Map<Long, Event> listEvent;
    final double t;
    private final Logger log = LoggerFactory.getLogger(getClass());

    RegisterServer(double t, int port, Map<Integer, Bs> listBs, Map<Integer, Ue> listUe, Map<Long, Event> listEvent) {
        this.port = port;
        this.listBs = listBs;
        this.listUe = listUe;
        this.listEvent = listEvent;
        this.t = t;
    }

    @Override
    public void run() {
        log.info("Registered entities:");
        runServer();
    }

    public void closeSockets() {
        try {
            for (var entry : listBs.entrySet())
                entry.getValue().closeSocket();

            for (var entry : listUe.entrySet())
                entry.getValue().closeSocket();

            close();
        } catch (Exception e) {
            log.error("Failed to close the sockets. Execution completed", e);
            System.exit(-1);
        }
    }

    public void closeRegister() {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packInt(EntityType.BROKER.value);
            closeRegisterServer(packer);
        } catch (IOException e) {
            log.error("Failed to shut down the log server. Execution completed", e);
            System.exit(-1);
        }
    }

    abstract void closeRegisterServer(MessageBufferPacker packer);

    abstract void runServer();

    abstract void close();

    boolean processRegister(Communicator communicator, MessageUnpacker messageUnpacker) throws IOException {
        final int typeInt = messageUnpacker.unpackInt();
        final EntityType type = EntityType.getCommunicatorTypeTypeByCode(typeInt);

        if (type == EntityType.BROKER) {
            messageUnpacker.close();
            return true;
        }

        final double x = messageUnpacker.unpackDouble();
        final double y = messageUnpacker.unpackDouble();
        messageUnpacker.close();


        registerEntity(type, x, y, communicator);
        return false;
    }

    void registerEntity(EntityType type, double x, double y, Communicator communicator) {
        if (type == EntityType.USER_EQUIPMENT) {
            final Ue ue = new Ue(x, y, communicator);
            final long eventId = Event.getNextId();
            final Event trafficIngress = new Event(eventId, t, EventType.TRAFFIC_INGRESS, ue);
            listUe.put(ue.getId(), ue);
            listEvent.put(trafficIngress.id(), trafficIngress);
            ue.sendRegisterAck(ue.getId());
            log.info("UE [id={}] {}", ue.getId(), communicator);
        } else if (type == EntityType.BASE_STATION) {
            final Bs bs = new Bs(x, y, communicator);
            final long eventId = Event.getNextId();
            final Event newState = new Event(eventId, t, EventType.NEW_STATE, bs);
            listBs.put(bs.getId(), bs);
            listEvent.put(newState.id(), newState);
            bs.sendRegisterAck(bs.getId());
            log.info("BS [id={}] {}", bs.getId(), communicator);
        }
    }
}
