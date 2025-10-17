package com.studyleague.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "practice_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSubmission {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "practice_test_id", nullable = false)
    private String practiceTestId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    // For coding tests
    @Column(name = "code_answer", columnDefinition = "TEXT")
    private String codeAnswer;

    // For MCQ tests: chosen option index
    @Column(name = "selected_option_index")
    private Integer selectedOptionIndex;

    // Auto-evaluated correctness for MCQ. For coding, left null.
    private Boolean correct;

    // For coding evaluations
    @Column(name = "passed")
    private Integer passed;

    @Column(name = "total")
    private Integer total;

    @Column(name = "stderr", columnDefinition = "TEXT")
    private String stderr;

    @Column(name = "score")
    private Integer score;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();
}


