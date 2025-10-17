package com.studyleague.controller;

import com.studyleague.model.Discussion;
import com.studyleague.model.DiscussionPost;
import com.studyleague.model.User;
import com.studyleague.repository.DiscussionPostRepository;
import com.studyleague.repository.DiscussionRepository;
import com.studyleague.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/discussions")
public class DiscussionController {
    private final DiscussionRepository discussionRepository;
    private final DiscussionPostRepository postRepository;
    private final UserRepository userRepository;

    public DiscussionController(DiscussionRepository discussionRepository,
                                DiscussionPostRepository postRepository,
                                UserRepository userRepository) {
        this.discussionRepository = discussionRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public record CreateDiscussion(@NotBlank String courseId, @NotBlank String title, String description) {}
    public record CreatePost(@NotBlank String content) {}

    @GetMapping("/courses/{courseId}")
    public List<Map<String, Object>> listByCourse(@PathVariable String courseId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Discussion d : discussionRepository.findByCourseIdOrderByCreatedAtDesc(courseId)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("_id", d.getId());
            m.put("courseId", d.getCourseId());
            m.put("title", d.getTitle());
            m.put("description", d.getDescription());
            m.put("creatorName", d.getCreatorName());
            m.put("createdAt", d.getCreatedAt());
            out.add(m);
        }
        return out;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateDiscussion req,
                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Discussion d = new Discussion();
        d.setCourseId(req.courseId());
        d.setTitle(req.title());
        d.setDescription(req.description());
        attachUserFromToken(d, authHeader);
        d = discussionRepository.save(d);
        return ResponseEntity.ok(Map.of("_id", d.getId()));
    }

    @GetMapping("/{discussionId}/posts")
    public List<Map<String, Object>> listPosts(@PathVariable Long discussionId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (DiscussionPost p : postRepository.findByDiscussionIdOrderByCreatedAtAsc(discussionId.toString())) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("_id", p.getId());
            m.put("discussionId", p.getDiscussionId());
            m.put("authorName", p.getAuthorName());
            m.put("content", p.getContent());
            m.put("createdAt", p.getCreatedAt());
            out.add(m);
        }
        return out;
    }

    @PostMapping("/{discussionId}/posts")
    public ResponseEntity<?> createPost(@PathVariable Long discussionId, @RequestBody CreatePost req,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Optional<Discussion> dOpt = discussionRepository.findById(discussionId);
        if (dOpt.isEmpty()) return ResponseEntity.notFound().build();
        DiscussionPost p = new DiscussionPost();
        p.setDiscussionId(discussionId.toString());
        p.setContent(req.content());
        attachUserFromToken(p, authHeader);
        if (p.getAuthorId() == null) return ResponseEntity.status(401).body(Map.of("message", "Authentication required"));
        p = postRepository.save(p);
        return ResponseEntity.ok(Map.of("_id", p.getId()));
    }

    private void attachUserFromToken(Discussion d, String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer fake.")) {
            String userId = authHeader.substring("Bearer fake.".length()).replace(".token", "").trim();
            Optional<User> userOpt = userRepository.findById(userId);
            userOpt.ifPresent(user -> {
                d.setCreatorId(user.getId());
                d.setCreatorName(user.getName());
            });
        }
    }

    private void attachUserFromToken(DiscussionPost p, String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer fake.")) {
            String userId = authHeader.substring("Bearer fake.".length()).replace(".token", "").trim();
            Optional<User> userOpt = userRepository.findById(userId);
            userOpt.ifPresent(user -> {
                p.setAuthorId(user.getId());
                p.setAuthorName(user.getName());
            });
        }
    }
}
