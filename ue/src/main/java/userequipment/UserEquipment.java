package userequipment;

import static communication.model.base.DtoIdentifier.TRAFFIC_INGRESS_REQUEST;
import static types.EntityType.USER_EQUIPMENT;

import communication.ClientCommunicator;
import communication.model.TrafficIngressResponseDto;
import communication.model.base.Dto;
import distribution.Distribution;
import domain.Position;
import entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import task.Task;
import task.TaskGenerator;
import types.EntityType;

@Slf4j
public class UserEquipment extends BaseEntity {

  private static final EntityType ENTITY_TYPE = USER_EQUIPMENT;
  private static final int MAX_MSG_LEN = getMaxMsgLen(TRAFFIC_INGRESS_REQUEST);

  private final Distribution mobilityDist;
  private final TaskGenerator taskGenerator;

  public UserEquipment(
      final ClientCommunicator communicator,
      final Position position,
      final Distribution mobilityDist,
      final TaskGenerator taskGenerator) {
    super(communicator, position);
    this.mobilityDist = mobilityDist;
    this.taskGenerator = taskGenerator;
    log.info(
        "UserEquipment created with communicator: {}, position: {}, mobilityDist: {}, taskGenerator: {}",
        communicator,
        position,
        mobilityDist,
        taskGenerator);
  }

  public static void main(final String[] args) {
    new Thread(new UserEquipmentFactory().createUserEquipment(args)).start();
  }

  @Override
  protected final int getMsgLen() {
    return MAX_MSG_LEN;
  }

  @Override
  protected final EntityType getEntityType() {
    return ENTITY_TYPE;
  }

  @Override
  protected final Dto processAction(final Dto dto) {
    return switch (dto.getIdentifier()) {
      case TRAFFIC_INGRESS_REQUEST -> processTrafficIngress();
      default -> processNotSupportedAction(dto);
    };
  }

  /*
   * This method generates a task and sends it to the broker.
   * The broker then redirects it to a base station.
   * The method also includes the time when the user equipment will generate the next task.
   */
  private Dto processTrafficIngress() {
    final Task task = taskGenerator.generateTask();
    position.move(mobilityDist.getRandom(), mobilityDist.getRandom());
    log.debug("Generated task {} in position {}", task, position);

    return new TrafficIngressResponseDto(
        position.getX(), position.getY(), task.size(), task.tUntilNextTask());
  }
}
