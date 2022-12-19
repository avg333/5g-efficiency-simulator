package communication;

import broker.Event;
import entities.Bs;
import entities.Ue;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class RegisterServerTCP extends RegisterServer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public RegisterServerTCP(double t, int port, Map<Integer, Bs> listBs, Map<Integer, Ue> listUe, Map<Long, Event> listEvent) {
        super(t, port, listBs, listUe, listEvent);
    }

    @Override
    public void runServer() {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                final Communicator communicator = new CommunicatorTCP(clientSocket);
                final MessageUnpacker messageUnpacker = communicator.receiveMessage(0);

                final boolean closeRegister = processRegister(communicator, messageUnpacker);
                if (closeRegister) return;

            }
        } catch (Exception e) {
            log.error("Log server error.. Execution completed", e);
            System.exit(-1);
        }
    }

    @Override
    public void closeRegisterServer(final MessageBufferPacker packer) {
        try (final Socket clientSocket = new Socket("localhost", port);
             final DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
            packer.close();
            final byte[] message = packer.toByteArray();
            out.writeInt(message.length);
            out.write(message);
        } catch (IOException e) {
            log.error("Failed to shut down the log server. Execution completed", e);
            System.exit(-1);
        }
    }

    void close() {
        // The TCP server closes automatically after complete the register of the entities
    }

}
