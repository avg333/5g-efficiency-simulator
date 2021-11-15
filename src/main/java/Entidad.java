import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Entidad {

    private final int id;
    private double x;
    private double y;
    private final DatagramSocket sc;
    private final InetAddress ad;
    private final int puerto;

    public Entidad(int id, double x, double y, DatagramSocket sc, InetAddress ad, int puerto) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.sc = sc;
        this.ad = ad;
        this.puerto = puerto;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public MessageUnpacker comunicar(MessageBufferPacker packer) {
        MessageUnpacker unpacker = null;
        try {
            byte[] dataRequest = packer.toByteArray();
            sc.send(new DatagramPacket(dataRequest, dataRequest.length, ad, puerto));
            byte[] dataResponse = new byte[100];
            sc.receive(new DatagramPacket(dataResponse, dataResponse.length));
            unpacker = MessagePack.newDefaultUnpacker(dataResponse);
        } catch (IOException e) {
            System.out.println("Error al enviar/recibir un mensaje. Ejecuci�n finalizada");
            System.exit(-1);
        }

        return unpacker;
    }

    public void cerrarSocket() {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            packer.packInt(Evento.CLOSE).close();
            final byte[] dataRequest = packer.toByteArray();
            sc.send(new DatagramPacket(dataRequest, dataRequest.length, ad, puerto));
        } catch (IOException e) {
            System.out.println("Error al cerrar el socket. Ejecuci�n finalizada");
            System.exit(-1);
        }
    }

}
