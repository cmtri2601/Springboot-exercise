package nc.solon.person.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nc.solon.person.dto.PersonInDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonEvent {
    public enum EventType { CREATE, UPDATE, DELETE }

    private EventType eventType;
    private Long personId;
    private PersonInDTO payload;
}
