package nc.solon.common.response;


import lombok.Data;
import nc.solon.common.dto.PersonOutDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

@Data
public class ApiResponse<T> {
    private Instant timestamp;
    private int status;
    private String message;
    private String errorCode; // e.g., VALIDATION_ERROR, USER_NOT_FOUND
    private String traceId;   // Useful in distributed systems (e.g., from Sleuth, OpenTelemetry)
    private List<ApiSubError> subErrors; // Field validation, etc.
    private T data;

    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    public ApiResponse(int status, String message, String errorCode, String traceId, List<ApiSubError> subErrors, T data) {
        this.timestamp = Instant.now();
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.traceId = traceId;
        this.subErrors = subErrors;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", null, null, null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, null, null, null, data);
    }

    public static <T> ApiResponse<T> error(int status, String message, String errorCode, String traceId, List<ApiSubError> subErrors) {
        return new ApiResponse<>(status, message, errorCode, traceId, subErrors, null);
    }

    public static <T> ApiResponse<T> notFound() {
        return new ApiResponse<>(404, "Not Found", null, null, null, null);
    }

    public static <T> ApiResponse<T> notFound(String errorCode) {
        return new ApiResponse<>(404, "Not Found", errorCode, null, null, null);
    }

    public static <T> ApiResponse<T> notFound(String errorCode, String traceId) {
        return new ApiResponse<>(404, "Not Found", errorCode, traceId, null, null);
    }

    public static <T> ApiResponse<T> accepted() {
        return new ApiResponse<>(202, "Accepted", null, null, null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> responseSuccess(T data) {
        return ResponseEntity.ok(success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> responseNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound());
    }

    public static <T> ResponseEntity<ApiResponse<T>> responseAccepted() {
        return ResponseEntity.accepted().body(ApiResponse.accepted());
    }
}