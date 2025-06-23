package nc.solon.common.response;

import lombok.Data;

@Data
public class ApiSubError {
    private String field;
    private String message;
    private Object rejectedValue;

    public ApiSubError(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }
}