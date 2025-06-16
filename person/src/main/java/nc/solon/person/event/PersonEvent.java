package nc.solon.person.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nc.solon.person.dto.PersonInDTO;

/** The type Person event. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonEvent {
  private EventType eventType;
  private Long personId;
  private PersonInDTO payload;

  /** The enum Event type. */
  public enum EventType {
    /** Create event type. */
    CREATE,
    /** Update event type. */
    UPDATE,
    /** Delete event type. */
    DELETE
  }
}
