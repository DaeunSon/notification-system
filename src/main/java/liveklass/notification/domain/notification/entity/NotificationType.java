package liveklass.notification.domain.notification.entity;


import lombok.Getter;

public enum NotificationType {

    //수강 관련 알림
    ENROLLMENT_CONFIRMED("수강 신청 확정", "참조 ID %s에 대한 수강 신청이 확정되었습니다."),
    ENROLLMENT_REJECTED("수강 신청 반려", "참조 ID %s에 대한 수강 신청이 반려되었습니다."),
    ENROLLMENT_CANCELED("수강 신청 취소", "참조 ID %s에 대한 수강 신청이 취소되었습니다."),

    //강의 관련 알림
    COURSE_OPENED("강의 개강", "참조 ID %s에 대한 강의가 개강되었습니다."),
    COURSE_DEADLINE_APPROACHING("강의 종료일 임박", "참조 ID %s에 대한 강의 종료 날짜가 임박합니다."),
    COURSE_CLOSED("강의 종료", "참조 ID %s에 대한 강의가 종료되었습니다."),
    COURSE_DRAFTED("등록되지 않은 강의", "참조 ID %s에 대한 등록되지 않은 강의입니다."),

    //결제 관련 알림
    PAYMENT_SUCCESS("결제 완료", "참조 ID %s에 대한 결제가 완료되었습니다."),
    PAYMENT_FAILED("결제 실패", "참조 ID %s에 대한 결제가 실패하였습니다."),
    PAYMENT_CANCELED("결제 취소", "참조 ID %s에 대한 결제가 취소되었습니다.");

    @Getter
    private final String title;
    private final String contentTemplate;

    NotificationType(String title, String contentTemplate) {
        this.title = title;
        this.contentTemplate = contentTemplate;
    }

    public String formatContent(String referenceId) {
        return String.format(contentTemplate, referenceId);
    }

}
