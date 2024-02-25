package domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Position {

  private Double x;

  private Double y;

  public void move(final Double x, final Double y) {
    this.x += x;
    this.y += y;
  }
}
