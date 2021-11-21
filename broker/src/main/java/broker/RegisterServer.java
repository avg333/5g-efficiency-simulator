package broker;

import entities.Bs;
import entities.Ue;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import types.CommunicatorType;
import types.EventType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

public class RegisterServer extends Thread {

	private final int port;
	private final Map<Integer, Bs> listaBS;
	private final Map<Integer, Ue> listaUE;
	private final Map<Long, Event> events;
	private final double T;
	private DatagramSocket sc;

	public RegisterServer(double t, int port, Map<Integer, Bs> listaBS, Map<Integer, Ue> listaUE, Map<Long, Event> events) {
		this.port = port;
		this.listaBS = listaBS;
		this.listaUE = listaUE;
		this.events = events;
		this.T = t;
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
					listaUE.put(ue.getId(), ue);
					Event trafficIngress = new Event(EventType.TRAFFIC_INGRESS, T, ue);
					events.put(trafficIngress.getId(), trafficIngress);
					ue.sendRegisterAck(ue.getId());
					System.out.print(" UE_" + ue.getId());
				} else if (type == CommunicatorType.BASE_STATION) {
					Bs bs = new Bs(x, y, sc, ad, portEntity);
					listaBS.put(bs.getId(), bs);
					Event newState = new Event(EventType.NEW_STATE, T, bs);
					events.put(newState.getId(), newState);
					bs.sendRegisterAck(bs.getId());
					System.out.print(" BS_" + bs.getId());
				}

			}
		} catch (IOException ex) {
			System.out.println("Error en el servidor de registro. Ejecuci�n finalizada");
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
			System.out.println("Error al cerrar el servidor de registro. Ejecuci�n finalizada");
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
			System.out.println("Error al cerrar los sockets. Ejecuci�n finalizada");
			System.exit(-1);
		}
	}

}
