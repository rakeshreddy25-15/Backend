package com.studyleague.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grade {
    @Id
    private String id = UUID.randomUUID().toString();
    
    @Column(nullable = false)
    private int score;
    
    @Column(name = "max_score", nullable = false)
    private int maxScore;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;
}

