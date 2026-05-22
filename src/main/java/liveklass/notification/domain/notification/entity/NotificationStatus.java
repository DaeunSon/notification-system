package liveklass.notification.domain.notification.entity;

public enum NotificationStatus {
    PENDING, //발송 대기
    PROCESSING, //발송 시도중
    SUCCESS, //발송 성공
    FAILED, //발송 실패 (retryCount < 3, 발송 재시도 대기)
    DEAD; //최종 실패 (retryCount >= 3, 수동 재처리 필요)
}
