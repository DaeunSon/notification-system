package liveklass.notification.global.exception;

/**
 * API 오류 응답 공통 형식.
 * GlobalExceptionHandler에서 BusinessException 등을 이 형태로 변환해 반환
 */
public record ErrorResponse(
        int status,
        String code,
        String message,
        String detail
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                ""
        );
    }
    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                detail
        );
    }
}
