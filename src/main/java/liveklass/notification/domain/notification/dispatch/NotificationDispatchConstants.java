package liveklass.notification.domain.notification.dispatch;

public final class NotificationDispatchConstants {

    private NotificationDispatchConstants() {
    }

    /** PROCESSING이 이 시간(분) 이상 지속되면 스턱으로 보고 복구한다. */
    public static final int STUCK_PROCESSING_THRESHOLD_MINUTES = 10;
}
