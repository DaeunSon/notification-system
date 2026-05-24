package liveklass.notification.domain.notification.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import liveklass.notification.domain.notification.entity.Notification;
import liveklass.notification.domain.notification.support.NotificationTestFixtures;
import liveklass.notification.domain.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("LoggingNotificationSender 테스트")
class LoggingNotificationSenderTest {

    private static final Long NOTIFICATION_ID = 10L;
    private static final Long RECEIVER_ID = 1L;

    private ListAppender<ILoggingEvent> logAppender;
    private LoggingNotificationSender sender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingNotificationSender.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);

        sender = new LoggingNotificationSender();
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingNotificationSender.class);
        logger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    @DisplayName("send() 호출 시 Mock 발송 로그가 INFO로 출력된다")
    void logsMockSendMessage() {
        User receiver = mock(User.class);
        when(receiver.getId()).thenReturn(RECEIVER_ID);

        Notification notification = NotificationTestFixtures.pending(receiver);
        ReflectionTestUtils.setField(notification, "id", NOTIFICATION_ID);

        sender.send(notification);

        assertThat(logAppender.list).hasSize(1);

        ILoggingEvent logEvent = logAppender.list.get(0);
        assertThat(logEvent.getLevel().toString()).isEqualTo("INFO");
        assertThat(logEvent.getFormattedMessage())
                .contains("[Mock 발송]")
                .contains("channel=EMAIL")
                .contains("notificationId=10")
                .contains("receiverId=1")
                .contains("title=title")
                .contains("content=content");
    }
}
