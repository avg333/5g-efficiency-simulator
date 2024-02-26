package communication.model.factory;

import communication.model.CloseBrokerDto;
import communication.model.CloseEntityDto;
import communication.model.NewStateRequestDto;
import communication.model.NewStateResponseDto;
import communication.model.RegisterRequestDto;
import communication.model.RegisterResponseDto;
import communication.model.TrafficArrivalRequestDto;
import communication.model.TrafficArrivalResponseDto;
import communication.model.TrafficEgressRequestDto;
import communication.model.TrafficEgressResponseDto;
import communication.model.TrafficIngressRequestDto;
import communication.model.TrafficIngressResponseDto;
import communication.model.base.Dto;
import communication.model.base.DtoIdentifier;
import domain.Position;
import domain.Task;
import java.io.IOException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import types.BsStateType;
import types.EntityType;

public class DtoFactory {

  public Dto createDto(byte[] bytes) throws IOException {
    try (final MessageUnpacker messageUnpacker = MessagePack.newDefaultUnpacker(bytes)) {
      final byte code = messageUnpacker.unpackByte();
      final DtoIdentifier dtoIdentifier = DtoIdentifier.getDtoIdentifierByCode(code);
      return switch (dtoIdentifier) {
        case CLOSE_BROKER -> createCloseBrokerDto(messageUnpacker);
        case CLOSE_ENTITY -> createCloseRequestDto(messageUnpacker);
        case NEW_STATE_REQUEST -> createNewStateRequestDto(messageUnpacker);
        case NEW_STATE_RESPONSE -> createNewStateResponseDto(messageUnpacker);
        case REGISTER_REQUEST -> createRegisterRequestDto(messageUnpacker);
        case REGISTER_RESPONSE -> createRegisterResponseDto(messageUnpacker);
        case TRAFFIC_ARRIVAL_REQUEST -> createTrafficArrivalRequestDto(messageUnpacker);
        case TRAFFIC_ARRIVAL_RESPONSE -> createTrafficArrivalResponseDto(messageUnpacker);
        case TRAFFIC_EGRESS_REQUEST -> createTrafficEgressRequestDto(messageUnpacker);
        case TRAFFIC_EGRESS_RESPONSE -> createTrafficEgressResponseDto(messageUnpacker);
        case TRAFFIC_INGRESS_REQUEST -> createTrafficIngressRequestDto(messageUnpacker);
        case TRAFFIC_INGRESS_RESPONSE -> createTrafficIngressResponseDto(messageUnpacker);
      };
    }
  }

  private CloseBrokerDto createCloseBrokerDto(final MessageUnpacker messageUnpacker) {
    return new CloseBrokerDto();
  }

  private CloseEntityDto createCloseRequestDto(final MessageUnpacker messageUnpacker) {
    return new CloseEntityDto();
  }

  private NewStateRequestDto createNewStateRequestDto(final MessageUnpacker messageUnpacker)
      throws IOException {
    final byte stateValue = messageUnpacker.unpackByte();
    final BsStateType state = BsStateType.getStateTypeByCode(stateValue);
    return new NewStateRequestDto(state);
  }

  private NewStateResponseDto createNewStateResponseDto(final MessageUnpacker messageUnpacker)
      throws IOException {
    final double q = messageUnpacker.unpackDouble();
    final BsStateType stateReceived = BsStateType.getStateTypeByCode(messageUnpacker.unpackByte());
    final double tTrafficEgress = messageUnpacker.unpackDouble();
    final double tNewState = messageUnpacker.unpackDouble();
    final BsStateType nextState = BsStateType.getStateTypeByCode(messageUnpacker.unpackByte());
    return new NewStateResponseDto(q, stateReceived, tTrafficEgress, tNewState, nextState);
  }

  private RegisterRequestDto createRegisterRequestDto(final MessageUnpacker messageUnpacker)
      throws IOException {
    final int typeValue = messageUnpacker.unpackInt();
    final EntityType type = EntityType.getCommunicatorTypeTypeByCode(typeValue);
    final double x = messageUnpacker.unpackDouble();
    final double y = messageUnpacker.unpackDouble();
    final Position position = new Position(x, y);
    return new RegisterRequestDto(type, position);
  }

  private RegisterResponseDto createRegisterResponseDto(final MessageUnpacker messageUnpacker)
      throws IOException {
    int id = messageUnpacker.unpackInt();
    return new RegisterResponseDto(id);
  }

  private TrafficArrivalRequestDto createTrafficArrivalRequestDto(
      final MessageUnpacker messageUnpacker) throws IOException {
    final long id = messageUnpacker.unpackLong();
    final double size = messageUnpacker.unpackDouble();
    final double tArrive = messageUnpacker.unpackDouble();
    return new TrafficArrivalRequestDto(new Task(id, size, tArrive, 0.0));
  }

  private TrafficArrivalResponseDto createTrafficArrivalResponseDto(
      final MessageUnpacker messageUnpacker) throws IOException {
    final double q = messageUnpacker.unpackDouble();
    final BsStateType state = BsStateType.getStateTypeByCode(messageUnpacker.unpackByte());
    final double tTrafficEgress = messageUnpacker.unpackDouble();
    final double tNewState = messageUnpacker.unpackDouble();
    final BsStateType nextState = BsStateType.getStateTypeByCode(messageUnpacker.unpackByte());
    final double a = messageUnpacker.unpackDouble();
    return new TrafficArrivalResponseDto(q, state, tTrafficEgress, tNewState, nextState, a);
  }

  private TrafficEgressRequestDto createTrafficEgressRequestDto(
      final MessageUnpacker messageUnpacker) throws IOException {
    final double t = messageUnpacker.unpackDouble();
    return new TrafficEgressRequestDto(t);
  }

  private TrafficEgressResponseDto createTrafficEgressResponseDto(
      final MessageUnpacker messageUnpacker) throws IOException {
    final double q = messageUnpacker.unpackDouble();
    final BsStateType state = BsStateType.getStateTypeByCode(messageUnpacker.unpackByte());
    final double tTrafficEgress = messageUnpacker.unpackDouble();
    final double tNewState = messageUnpacker.unpackDouble();
    final BsStateType nextState = BsStateType.getStateTypeByCode(messageUnpacker.unpackByte());
    final double w = messageUnpacker.unpackDouble();
    final long id = messageUnpacker.unpackLong();
    final double size = messageUnpacker.unpackDouble();
    return new TrafficEgressResponseDto(
        q, state, tTrafficEgress, tNewState, nextState, w, id, size);
  }

  private TrafficIngressRequestDto createTrafficIngressRequestDto(
      final MessageUnpacker messageUnpacker) throws IOException {
    return new TrafficIngressRequestDto();
  }

  private TrafficIngressResponseDto createTrafficIngressResponseDto(
      final MessageUnpacker messageUnpacker) throws IOException {
    double x = messageUnpacker.unpackDouble();
    double y = messageUnpacker.unpackDouble();
    double size = messageUnpacker.unpackDouble();
    double tUntilNextTask = messageUnpacker.unpackDouble();
    return new TrafficIngressResponseDto(x, y, size, tUntilNextTask);
  }
}
