package liveklass.notification.domain.notification.template;

import static org.assertj.core.api.Assertions.assertThat;

import liveklass.notification.domain.notification.dto.RenderedNotificationMessage;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationMessageFactory 테스트")
class NotificationMessageFactoryTest {

    private final NotificationMessageFactory factory = new NotificationMessageFactory();

    @Test
    @DisplayName("EMAIL 채널은 인사·맺음말이 포함된 본문을 생성한다")
    void renderEmail() {
        RenderedNotificationMessage message = factory.render(
                NotificationType.ENROLLMENT_CONFIRMED,
                NotificationChannel.EMAIL,
                "course-1"
        );

        assertThat(message.title()).isEqualTo("수강 신청 확정");
        assertThat(message.content()).contains("안녕하세요");
        assertThat(message.content()).contains("course-1");
        assertThat(message.content()).contains("LiveKlass 알림팀");
    }

    @Test
    @DisplayName("IN_APP 채널은 짧은 본문을 생성한다")
    void renderInApp() {
        RenderedNotificationMessage message = factory.render(
                NotificationType.ENROLLMENT_CONFIRMED,
                NotificationChannel.IN_APP,
                "course-1"
        );

        assertThat(message.title()).isEqualTo("수강 신청 확정");
        assertThat(message.content()).isEqualTo("참조 ID course-1에 대한 수강 신청이 확정되었습니다.");
        assertThat(message.content()).doesNotContain("안녕하세요");
    }
}
