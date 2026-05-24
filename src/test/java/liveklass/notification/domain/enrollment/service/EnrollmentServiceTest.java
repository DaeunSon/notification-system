package liveklass.notification.domain.enrollment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import liveklass.notification.domain.enrollment.dto.EnrollCourseRequest;
import liveklass.notification.domain.enrollment.dto.EnrollCourseResponse;
import liveklass.notification.domain.notification.entity.NotificationChannel;
import liveklass.notification.domain.notification.entity.NotificationType;
import liveklass.notification.domain.notification.event.NotificationRequestedEvent;
import liveklass.notification.domain.user.entity.User;
import liveklass.notification.domain.user.repository.UserRepository;
import liveklass.notification.global.exception.BusinessException;
import liveklass.notification.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService 테스트")
class EnrollmentServiceTest {

    private static final Long RECEIVER_ID = 1L;
    private static final Long UNKNOWN_RECEIVER_ID = 99L;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        User receiver = mock(User.class);
        lenient().when(userRepository.findById(RECEIVER_ID)).thenReturn(Optional.of(receiver));
    }

    private EnrollCourseRequest request(Long receiverId, String courseId, NotificationChannel channel) {
        return new EnrollCourseRequest(receiverId, courseId, channel);
    }

    private EnrollCourseRequest request(String courseId, NotificationChannel channel) {
        return request(RECEIVER_ID, courseId, channel);
    }

    @Nested
    @DisplayName("enroll()")
    class Enroll {

        @Test
        @DisplayName("수강 성공 시 ENROLLMENT_CONFIRMED 이벤트를 발행한다")
        void success() {
            EnrollCourseResponse response = enrollmentService.enroll(
                    request("course-1", NotificationChannel.EMAIL)
            );

            assertThat(response.status()).isEqualTo("COMPLETED");
            assertThat(response.courseId()).isEqualTo("course-1");

            NotificationRequestedEvent event = capturePublishedEvent();
            assertThat(event.request().notificationType()).isEqualTo(NotificationType.ENROLLMENT_CONFIRMED);
            assertThat(event.request().referenceId()).isEqualTo("course-1");
        }

        @Test
        @DisplayName("courseId=fail-business이면 ENROLLMENT_REJECTED 이벤트를 발행한다")
        void rejected() {
            EnrollCourseResponse response = enrollmentService.enroll(
                    request("fail-business", NotificationChannel.IN_APP)
            );

            assertThat(response.status()).isEqualTo("REJECTED");

            NotificationRequestedEvent event = capturePublishedEvent();
            assertThat(event.request().notificationType()).isEqualTo(NotificationType.ENROLLMENT_REJECTED);
            assertThat(event.request().channel()).isEqualTo(NotificationChannel.IN_APP);
        }

        @Test
        @DisplayName("존재하지 않는 수신자 ID면 예외 발생")
        void fail_userNotFound() {
            given(userRepository.findById(UNKNOWN_RECEIVER_ID)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> enrollmentService.enroll(
                            request(UNKNOWN_RECEIVER_ID, "course-1", NotificationChannel.EMAIL)
                    )
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("수강 취소 시 ENROLLMENT_CANCELED 이벤트를 발행한다")
        void success() {
            EnrollCourseResponse response = enrollmentService.cancel(
                    request("course-1", NotificationChannel.EMAIL)
            );

            assertThat(response.status()).isEqualTo("CANCELED");

            NotificationRequestedEvent event = capturePublishedEvent();
            assertThat(event.request().notificationType()).isEqualTo(NotificationType.ENROLLMENT_CANCELED);
        }

        @Test
        @DisplayName("존재하지 않는 수신자 ID면 예외 발생")
        void fail_userNotFound() {
            given(userRepository.findById(UNKNOWN_RECEIVER_ID)).willReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> enrollmentService.cancel(
                            request(UNKNOWN_RECEIVER_ID, "course-1", NotificationChannel.EMAIL)
                    )
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    private NotificationRequestedEvent capturePublishedEvent() {
        ArgumentCaptor<NotificationRequestedEvent> captor =
                ArgumentCaptor.forClass(NotificationRequestedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }
}
