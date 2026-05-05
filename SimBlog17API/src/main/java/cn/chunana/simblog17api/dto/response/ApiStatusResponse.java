package cn.chunana.simblog17api.dto.response;

import cn.chunana.simblog17api.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
@Builder
@AllArgsConstructor
public class ApiStatusResponse<T> {
    private final Integer statusCode;
    private final String  statusMessage;
    private final Long    timeStamp;
    private final String  traceId;
    private final T       data;

    public ApiStatusResponse(Status status, T data) {
        statusCode    = status.getCode();
        statusMessage = status.getMessage();
        timeStamp     = System.currentTimeMillis();
        traceId       = MDC.get("traceId");
        this.data     = data;
    }

    public static <T> ApiStatusResponse<T> ok() {
        return new ApiStatusResponse<>(Status.SUCCESS, null);
    }

    public static <T> ApiStatusResponse<T> ok(T data) {
        return new ApiStatusResponse<>(Status.SUCCESS, data);
    }

    public static <T> ApiStatusResponse<T> ok(Status status, T data) {
        return new ApiStatusResponse<>(status, data);
    }

    public static <T> ApiStatusResponse<T> fail(Status status) {
        return new ApiStatusResponse<>(status, null);
    }

    public static <T> ApiStatusResponse<T> fail(Status status, T data) {
        return new ApiStatusResponse<>(status, data);
    }
}
