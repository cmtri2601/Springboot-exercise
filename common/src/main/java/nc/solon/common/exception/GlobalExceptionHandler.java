package nc.solon.common.exception;

import nc.solon.common.response.ApiResponse;
import nc.solon.common.response.ApiSubError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        List<ApiSubError> subErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ApiSubError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        fieldError.getRejectedValue()))
                .toList();

        return ResponseEntity.badRequest().body(
                ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Failed",
                        "VALIDATION_ERROR",
                        request.getHeader("traceId"),
                        subErrors
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleMalformedJson(HttpMessageNotReadableException ex, WebRequest request) {
        return ResponseEntity.badRequest().body(
                ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        "Malformed JSON request",
                        "MALFORMED_JSON",
                        request.getHeader("traceId"),
                        null
                )
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                ApiResponse.error(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        "Method not allowed",
                        "METHOD_NOT_ALLOWED",
                        request.getHeader("traceId"),
                        null
                )
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusiness(BusinessException ex, WebRequest request) {
        return ResponseEntity.badRequest().body(
                ApiResponse.error(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        ex.getErrorCode(),
                        request.getHeader("traceId"),
                        null
                )
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(RuntimeException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage() != null ? ex.getMessage() : "Internal server error",
                        "RUNTIME_EXCEPTION",
                        request.getHeader("traceId"),
                        null
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Unexpected error occurred",
                        "UNEXPECTED_ERROR",
                        request.getHeader("traceId"),
                        null
                )
        );
    }
}
