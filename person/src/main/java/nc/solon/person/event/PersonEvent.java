package nc.solon.person;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonEvent {
    public enum EventType { CREATE, UPDATE, DELETE }

    private EventType eventType;
    private Long personId;
    private String payload; // JSON representation of PersonInDTO
}
