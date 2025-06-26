package nc.solon.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import nc.solon.common.dto.PersonInDTO;

/**
 * The type Person event.
 */
@Data
@AllArgsConstructor
public class PersonEvent {
    private EventType eventType;
    private Long personId;
    private PersonInDTO payload;

  /**
   * The enum Event type.
   */
  public enum EventType {
    /**
     * Create event type.
     */
    CREATE,
    /**
     * Update event type.
     */
    UPDATE,
    /**
     * Delete event type.
     */
    DELETE
    }
}
