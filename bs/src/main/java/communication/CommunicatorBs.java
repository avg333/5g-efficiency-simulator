package communication;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.StateType;

public record CommunicatorBs(Communicator communicator) implements Communicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicatorBs.class);

    private static final String ERROR_PACKING = "Error packing the message. Execution completed";

    public void sendTrafficArrival(double q, StateType state, double tTrafficEgress, double tNewState, StateType nextState, double a) {
        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q);
            response.packInt(StateType.getCodeByStateType(state));
            response.packDouble(tTrafficEgress);
            response.packDouble(tNewState);
            response.packInt(StateType.getCodeByStateType(nextState));
            response.packDouble(a);
            communicator.sendMessage(response);
        } catch (Exception e) {
            LOGGER.error(ERROR_PACKING, e);
            communicator.close();
            System.exit(-1);
        }
    }

    public void sendTrafficEgress(double q, StateType state, double tTrafficEgress, double tNewState, StateType nextState, double w,
                                  long id, double size) {
        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q);
            response.packInt(StateType.getCodeByStateType(state));
            response.packDouble(tTrafficEgress);
            response.packDouble(tNewState);
            response.packInt(StateType.getCodeByStateType(nextState));
            response.packDouble(w);
            response.packLong(id);
            response.packDouble(size);
            communicator.sendMessage(response);
        } catch (Exception e) {
            LOGGER.error(ERROR_PACKING, e);
            communicator.close();
            System.exit(-1);
        }
    }

    public void sendNewState(double q, StateType stateReceived, double tTrafficEgress, double tNewState, StateType nextState) {
        try (MessageBufferPacker response = MessagePack.newDefaultBufferPacker()) {
            response.packDouble(q);
            response.packInt(StateType.getCodeByStateType(stateReceived));
            response.packDouble(tTrafficEgress);
            response.packDouble(tNewState);
            response.packInt(StateType.getCodeByStateType(nextState));
            communicator.sendMessage(response);
        } catch (Exception e) {
            LOGGER.error(ERROR_PACKING, e);
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
