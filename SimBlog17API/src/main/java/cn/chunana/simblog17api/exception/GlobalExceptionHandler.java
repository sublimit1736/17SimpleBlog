package cn.chunana.simblog17api.exception;

import cn.chunana.simblog17api.common.Status;
import cn.chunana.simblog17api.dto.response.ApiStatusResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiStatusResponse<String> handleValidationException(MethodArgumentNotValidException exception) {
        String message = joinMessages(exception.getBindingResult().getFieldErrors()
                                               .stream()
                                               .map(this::formatFieldError)
                                               .toList());

        return ApiStatusResponse.fail(Status.INVALID_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiStatusResponse<String> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = joinMessages(exception.getConstraintViolations()
                                               .stream()
                                               .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                                               .toList());

        return ApiStatusResponse.fail(Status.INVALID_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiStatusResponse<String> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        String message = exception.getName() + ": invalid value '" + exception.getValue() + "'";
        return ApiStatusResponse.fail(Status.INVALID_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiStatusResponse<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ignored) {
        return ApiStatusResponse.fail(Status.INVALID_REQUEST, "Malformed JSON request body");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiStatusResponse<String> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ApiStatusResponse.fail(Status.INVALID_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiStatusResponse<String> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.warn("Data integrity violation: {}", exception.getMessage());
        return ApiStatusResponse.fail(Status.INVALID_REQUEST, "数据冲突，请检查是否存在重复数据");
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private String joinMessages(List<String> messages) {
        return String.join("; ", messages);
    }

    @ExceptionHandler(Exception.class)
    public ApiStatusResponse<Void> handleUnexpectedException(Exception exception) {
        log.error("Unexpected exception: ", exception);
        return ApiStatusResponse.fail(Status.UNEXPECTED_ERROR);
    }
}


