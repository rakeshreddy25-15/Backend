package com.studyleague.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.Instant;

@Entity
public class DiscussionPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String discussionId;
    private String authorId;
    private String authorName;
    private String content;
    private Instant createdAt = Instant.now();

    public DiscussionPost() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDiscussionId() { return discussionId; }
    public void setDiscussionId(String discussionId) { this.discussionId = discussionId; }
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
