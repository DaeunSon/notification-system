package liveklass.notification.domain.enrollment.dto;

public record EnrollCourseResponse(

        String courseId,
        String status
) {
    public static EnrollCourseResponse completed(String courseId) {
        return new EnrollCourseResponse(courseId, "COMPLETED");
    }

    public static EnrollCourseResponse rejected(String courseId) {
        return new EnrollCourseResponse(courseId, "REJECTED");
    }

    public static EnrollCourseResponse canceled(String courseId) {
        return new EnrollCourseResponse(courseId, "CANCELED");
    }
}
