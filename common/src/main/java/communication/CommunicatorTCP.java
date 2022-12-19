package communication;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.EntityType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class CommunicatorTCP extends Communicator {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    public CommunicatorTCP(final Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new DataInputStream(clientSocket.getInputStream());
    }

    public CommunicatorTCP(final EntityType type, final String ip, final int port, final double x, final double y) {
        try (final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            clientSocket = new Socket(ip, port);
            clientSocket.setSoTimeout(TIMEOUT);
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
            register(type, clientSocket.getInetAddress(), clientSocket.getPort(), x, y, packer);
        } catch (Exception e) {
            log.error("Registration failed. Execution completed", e);
            System.exit(-1);
        }
    }

    @Override
    public MessageUnpacker receiveMessage(final int dataLen) {
        try {
            final int length = in.readInt();
            byte[] data = new byte[length];
            in.readFully(data, 0, data.length);
            return MessagePack.newDefaultUnpacker(data);
        } catch (Exception e) {
            log.error("Error trying to receive a message. Execution completed", e);
            this.close();
            System.exit(-1);
        }

        return null;
    }

    @Override
    public void sendMessage(final MessageBufferPacker packer) {
        try {
            packer.close();
            final byte[] message = packer.toByteArray();
            out.writeInt(message.length);
            out.write(message);
        } catch (Exception e) {
            log.error("Error trying to send a message. Execution completed", e);
            this.close();
            System.exit(-1);
        }
    }

    @Override
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (Exception e) {
            log.error("Error trying to close the socket. Execution completed", e);
            System.exit(-1);
        }
    }

    @Override
    public String toString() {
        return "ad=" + clientSocket.getInetAddress().getHostAddress() + ", port=" + clientSocket.getPort();
    }
}
