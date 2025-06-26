package nc.solon.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The type Camunda log.
 */
@Data
@AllArgsConstructor
public class CamundaLogEvent {
    /**
     * The Message.
     */
    String message;
    /**
     * The Error message.
     */
    String errorMessage;
    /**
     * The Data.
     */
    Object data;
}
