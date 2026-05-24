package liveklass.notification.domain.enrollment.controller;

import jakarta.validation.Valid;
import liveklass.notification.domain.enrollment.dto.EnrollCourseRequest;
import liveklass.notification.domain.enrollment.dto.EnrollCourseResponse;
import liveklass.notification.domain.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<EnrollCourseResponse> enroll(@Valid @RequestBody EnrollCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.enroll(request));
    }

    @PostMapping("/cancel")
    public ResponseEntity<EnrollCourseResponse> cancel(@Valid @RequestBody EnrollCourseRequest request) {
        return ResponseEntity.ok(enrollmentService.cancel(request));
    }
}
