package liveklass.notification.domain.enrollment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import liveklass.notification.domain.notification.entity.NotificationChannel;

public record EnrollCourseRequest(

        @NotNull
        Long receiverId,

        @NotBlank
        String courseId,

        @NotNull
        NotificationChannel channel
) {
}
