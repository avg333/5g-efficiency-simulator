package communication.model.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.fail;

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
import java.util.List;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

class DtoFactoryTest {

  @Test
  void shouldCreateDtoSuccessfully() {
    final List<Dto> dtos =
        List.of(
            createCloseBrokerDto(),
            createCloseEntityDto(),
            createNewStateRequestDto(),
            createNewStateResponseDto(),
            createRegisterRequestDto(),
            createRegisterResponseDto(),
            createTrafficArrivalRequestDto(),
            createTrafficArrivalResponseDto(),
            createTrafficEgressRequestDto(),
            createTrafficEgressResponseDto(),
            createTrafficIngressRequestDto(),
            createTrafficIngressResponseDto());
    final DtoFactory dtoFactory = new DtoFactory();
    dtos.forEach(
        dto -> {
          try {
            assertThat(dtoFactory.createDto(dto.toByteArray()))
                .isNotNull()
                .isInstanceOf(dto.getClass())
                .usingRecursiveComparison()
                .isEqualTo(dto);
          } catch (IOException e) {
            fail("Exception while creating the dto " + dto.getClass().getSimpleName(), e);
          }
        });
  }

  private <T extends Dto> T createDto(final Class<T> dtoClass, final DtoIdentifier identifier) {
    return Instancio.of(dtoClass).set(field(Dto::getIdentifier), identifier).create();
  }

  private Dto createCloseBrokerDto() {
    return createDto(CloseBrokerDto.class, CloseBrokerDto.IDENTIFIER);
  }

  private Dto createCloseEntityDto() {
    return createDto(CloseEntityDto.class, CloseEntityDto.IDENTIFIER);
  }

  private Dto createNewStateRequestDto() {
    return createDto(NewStateRequestDto.class, NewStateRequestDto.IDENTIFIER);
  }

  private Dto createNewStateResponseDto() {
    return createDto(NewStateResponseDto.class, NewStateResponseDto.IDENTIFIER);
  }

  private Dto createRegisterRequestDto() {
    return createDto(RegisterRequestDto.class, RegisterRequestDto.IDENTIFIER);
  }

  private Dto createRegisterResponseDto() {
    return createDto(RegisterResponseDto.class, RegisterResponseDto.IDENTIFIER);
  }

  private Dto createTrafficArrivalRequestDto() {
    return createDto(TrafficArrivalRequestDto.class, TrafficArrivalRequestDto.IDENTIFIER);
  }

  private Dto createTrafficArrivalResponseDto() {
    return createDto(TrafficArrivalResponseDto.class, TrafficArrivalResponseDto.IDENTIFIER);
  }

  private Dto createTrafficEgressRequestDto() {
    return createDto(TrafficEgressRequestDto.class, TrafficEgressRequestDto.IDENTIFIER);
  }

  private Dto createTrafficEgressResponseDto() {
    return createDto(TrafficEgressResponseDto.class, TrafficEgressResponseDto.IDENTIFIER);
  }

  private Dto createTrafficIngressRequestDto() {
    return createDto(TrafficIngressRequestDto.class, TrafficIngressRequestDto.IDENTIFIER);
  }

  private Dto createTrafficIngressResponseDto() {
    return createDto(TrafficIngressResponseDto.class, TrafficIngressResponseDto.IDENTIFIER);
  }
}
