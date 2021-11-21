package broker;

import entities.Bs;
import entities.Ue;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.CommunicatorType;
import types.EventType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class RegisterServer extends Thread {
	private static final Logger LOGGER = LoggerFactory.getLogger(RegisterServer.class);

	private final int port;
	private final Map<Integer, Bs> listaBS;
	private final Map<Integer, Ue> listaUE;
	private final Map<Long, Event> events;
	private final double t;
	private DatagramSocket sc;

	public RegisterServer(double t, int port, Map<Integer, Bs> listaBS, Map<Integer, Ue> listaUE, Map<Long, Event> events) {
		this.port = port;
		this.listaBS = listaBS;
		this.listaUE = listaUE;
		this.events = events;
		this.t = t;
	}

	@Override
	public void run() {
		System.out.print("Registradas las entidades:");
		try {
			sc = new DatagramSocket(port);
			while (true) {
				final byte[] data = new byte[50];
				final DatagramPacket dp = new DatagramPacket(data, data.length);
				sc.receive(dp);
				final MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(dp.getData());
				final CommunicatorType type = CommunicatorType.getCommunicatorTypeTypeByCode(unpacker.unpackInt());

				if (type == CommunicatorType.UNADMITTED) {
					unpacker.close();
					return;
				}

				final double x = unpacker.unpackDouble();
				final double y = unpacker.unpackDouble();
				final InetAddress ad = dp.getAddress();
				final int portEntity = dp.getPort();
				unpacker.close();

				if (type == CommunicatorType.USER_EQUIPMENT) {
					final Ue ue = new Ue(x, y, sc, ad, portEntity);
					final Event trafficIngress = new Event(EventType.TRAFFIC_INGRESS, t, ue);
					listaUE.put(ue.getId(), ue);
					events.put(trafficIngress.getId(), trafficIngress);
					ue.sendRegisterAck(ue.getId());
					System.out.print(" UE_" + ue.getId());
				} else if (type == CommunicatorType.BASE_STATION) {
					final Bs bs = new Bs(x, y, sc, ad, portEntity);
					final Event newState = new Event(EventType.NEW_STATE, t, bs);
					listaBS.put(bs.getId(), bs);
					events.put(newState.getId(), newState);
					bs.sendRegisterAck(bs.getId());
					System.out.print(" BS_" + bs.getId());
				}

			}
		} catch (Exception e) {
			LOGGER.error("Log server error.. Execution completed", e);
			System.exit(-1);
		}
	}

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

	public void closeSockets() {
		try {
			for (var entry : listaBS.entrySet())
				entry.getValue().closeSocket();

			for (var entry : listaUE.entrySet())
				entry.getValue().closeSocket();

			sc.close();
		} catch (Exception e) {
			LOGGER.error("Failed to close the sockets. Execution completed", e);
			System.exit(-1);
		}
	}

}
