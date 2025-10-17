package com.studyleague.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    @Id
    private String id = UUID.randomUUID().toString();
    
    @Column(name = "course_id", nullable = false)
    private String courseId;
    
    @Column(name = "student_id", nullable = false)
    private String studentId;
    
    @Column(name = "student_name", nullable = false)
    private String studentName;
    
    @Column(name = "student_email", nullable = false)
    private String studentEmail;
    
    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt = Instant.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;
}

