package com.studyleague.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "practice_tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeTest {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "course_id", nullable = false)
    private String courseId;

    // "coding" or "mcq"
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    // For coding tasks
    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;

    @Column(name = "language")
    private String language; // e.g., "cpp", "java", "python"

    @ElementCollection
    @CollectionTable(name = "practice_test_cases", joinColumns = @JoinColumn(name = "practice_test_id"))
    private List<PracticeTestCase> testCases = new ArrayList<>();

    // For MCQ
    @ElementCollection
    @CollectionTable(name = "practice_test_options", joinColumns = @JoinColumn(name = "practice_test_id"))
    @Column(name = "option_text")
    private List<String> options = new ArrayList<>();

    @Column(name = "correct_option_index")
    private Integer correctOptionIndex;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}


