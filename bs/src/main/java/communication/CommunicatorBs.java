package communication;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.StateType;

public record CommunicatorBs(Communicator communicator) implements Communicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicatorBs.class);

    public void sendTrafficArrival(double q, StateType state, double tTrafficEgress, double tNewState, StateType nextState, double a) {
        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q);
            response.packInt(state.value);
            response.packDouble(tTrafficEgress);
            response.packDouble(tNewState);
            response.packInt(nextState.value);
            response.packDouble(a);
            communicator.sendMessage(response);
        } catch (Exception e) {
            LOGGER.error("Error packing the message Traffic Arrival. Execution completed", e);
            communicator.close();
            System.exit(-1);
        }
    }

    public void sendTrafficEgress(double q, StateType state, double tTrafficEgress, double tNewState, StateType nextState, double w,
                                  long id, double size) {
        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q);
            response.packInt(state.value);
            response.packDouble(tTrafficEgress);
            response.packDouble(tNewState);
            response.packInt(nextState.value);
            response.packDouble(w);
            response.packLong(id);
            response.packDouble(size);
            communicator.sendMessage(response);
        } catch (Exception e) {
            LOGGER.error("Error packing the message Traffic Egress. Execution completed", e);
            communicator.close();
            System.exit(-1);
        }
    }

    public void sendNewState(double q, StateType stateReceived, double tTrafficEgress, double tNewState, StateType nextState) {
        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q);
            response.packInt(stateReceived.value);
            response.packDouble(tTrafficEgress);
            response.packDouble(tNewState);
            response.packInt(nextState.value);
            communicator.sendMessage(response);
        } catch (Exception e) {
            LOGGER.error("Error packing the message New State. Execution completed", e);
            communicator.close();
            System.exit(-1);
        }
    }

    @Override
    public MessageUnpacker receiveMessage(int dataLen) {
        return communicator.receiveMessage(dataLen);
    }

    @Override
    public void sendMessage(MessageBufferPacker packer) {
        communicator.sendMessage(packer);
    }

    @Override
    public void close() {
        communicator.close();
    }
}
