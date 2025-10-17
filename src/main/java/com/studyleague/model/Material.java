package com.studyleague.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "course_id", nullable = false)
    private String courseId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // URL or reference to file in object storage; simple for now
    @Column(name = "resource_url")
    private String resourceUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}



