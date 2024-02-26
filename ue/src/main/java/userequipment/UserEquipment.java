package userequipment;

import static communication.model.base.DtoIdentifier.TRAFFIC_INGRESS_REQUEST;
import static types.EntityType.USER_EQUIPMENT;

import communication.Communicator;
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

  private static final int MSG_LEN = getMaxMsgLen(TRAFFIC_INGRESS_REQUEST);

  private final Distribution mobilityDist;

  private final TaskGenerator taskGenerator;

  public UserEquipment(
      Position position,
      Communicator communicator,
      Distribution mobilityDist,
      TaskGenerator taskGenerator) {
    super(position, communicator);
    this.mobilityDist = mobilityDist;
    this.taskGenerator = taskGenerator;

    log.info("Registered in {}", communicator);
  }

  public static void main(String[] args) {
    new Thread(new UserEquipmentFactory().createUserEquipment()).start();
  }

  @Override
  protected final int getMsgLen() {
    return MSG_LEN;
  }

  @Override
  protected final EntityType getEntityType() {
    return USER_EQUIPMENT;
  }

  @Override
  protected final void processAction(final Dto dto) {
    switch (dto.getIdentifier()) {
      case TRAFFIC_INGRESS_REQUEST -> processTrafficIngress();
      default -> processNotSupportedAction(dto);
    }
  }

  /*
   * This method generates a task and sends it to the broker.
   * The broker then redirects it to a base station.
   * The method also includes the time when the user equipment will generate the next task.
   */
  protected void processTrafficIngress() {
    final Task task = taskGenerator.generateTask();
    position.move(mobilityDist.getRandom(), mobilityDist.getRandom());
    sendMessage(
        new TrafficIngressResponseDto(
            position.getX(), position.getY(), task.size(), task.tUntilNextTask()));
    log.debug("Generated task {} in position {}", task, position);
  }
}
