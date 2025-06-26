package nc.solon.common.exception;

import lombok.Getter;

/**
 * The type Business exception.
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * -- GETTER --
     *  Gets error code.
     */
    private final String errorCode;

    /**
     * Instantiates a new Business exception.
     *
     * @param message   the message
     * @param errorCode the error code
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}
