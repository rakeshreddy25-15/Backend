package com.studyleague.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Submission {
    @Id
    private String id = UUID.randomUUID().toString();
    
    @Column(name = "assignment_id", nullable = false)
    private String assignmentId;
    
    @Column(name = "student_id", nullable = false)
    private String studentId;
    
    @Column(name = "student_name", nullable = false)
    private String studentName;
    
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();
    
    @Column(columnDefinition = "TEXT")
    private String text;
    
    @ElementCollection
    @CollectionTable(name = "submission_files", joinColumns = @JoinColumn(name = "submission_id"))
    @Column(name = "file_name")
    private List<String> fileNames = new ArrayList<>();
    
    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Grade grade;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private Assignment assignment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;
}

