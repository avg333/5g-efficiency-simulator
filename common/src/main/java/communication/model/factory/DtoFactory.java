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
import java.io.IOException;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

public class DtoFactory {

  public Dto createDto(final byte[] bytes) throws IOException {
    try (final MessageUnpacker messageUnpacker = MessagePack.newDefaultUnpacker(bytes)) {
      return switch (DtoIdentifier.fromValue(messageUnpacker.unpackByte())) {
        case REGISTER_REQUEST -> new RegisterRequestDto(messageUnpacker);
        case REGISTER_RESPONSE -> new RegisterResponseDto(messageUnpacker);
        case CLOSE_BROKER -> new CloseBrokerDto();
        case CLOSE_ENTITY -> new CloseEntityDto();
        case TRAFFIC_INGRESS_REQUEST -> new TrafficIngressRequestDto();
        case TRAFFIC_INGRESS_RESPONSE -> new TrafficIngressResponseDto(messageUnpacker);
        case TRAFFIC_ARRIVAL_REQUEST -> new TrafficArrivalRequestDto(messageUnpacker);
        case TRAFFIC_ARRIVAL_RESPONSE -> new TrafficArrivalResponseDto(messageUnpacker);
        case TRAFFIC_EGRESS_REQUEST -> new TrafficEgressRequestDto(messageUnpacker);
        case TRAFFIC_EGRESS_RESPONSE -> new TrafficEgressResponseDto(messageUnpacker);
        case NEW_STATE_REQUEST -> new NewStateRequestDto(messageUnpacker);
        case NEW_STATE_RESPONSE -> new NewStateResponseDto(messageUnpacker);
      };
    }
  }
}
