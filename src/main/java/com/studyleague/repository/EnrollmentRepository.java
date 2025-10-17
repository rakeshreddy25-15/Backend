package com.studyleague.repository;

import com.studyleague.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {
    List<Enrollment> findByCourseId(String courseId);
    Optional<Enrollment> findByCourseIdAndStudentId(String courseId, String studentId);
    boolean existsByCourseIdAndStudentId(String courseId, String studentId);
}

