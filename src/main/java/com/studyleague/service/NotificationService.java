package com.studyleague.service;

import com.studyleague.model.Notification;
import com.studyleague.model.Enrollment;
import com.studyleague.repository.EnrollmentRepository;
import com.studyleague.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final EnrollmentRepository enrollmentRepository;

    public NotificationService(NotificationRepository notificationRepository, EnrollmentRepository enrollmentRepository) {
        this.notificationRepository = notificationRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public void notifyCourse(String courseId, String title, String description) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        for (Enrollment e : enrollments) {
            Notification n = new Notification(e.getStudentId(), title, description);
            notificationRepository.save(n);
        }
    }

    public void notifyUser(String userId, String title, String description) {
        Notification n = new Notification(userId, title, description);
        notificationRepository.save(n);
    }

    public List<Notification> forUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
