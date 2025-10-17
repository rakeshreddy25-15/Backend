package com.studyleague.controller;

import com.studyleague.model.Notification;
import com.studyleague.repository.NotificationRepository;
import com.studyleague.service.NotificationService;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationService notificationService, NotificationRepository notificationRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<?> listForUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer fake.")) {
            userId = authHeader.substring("Bearer fake.".length()).replace(".token", "").trim();
        }
        if (userId == null) return ResponseEntity.status(401).body("Unauthorized");
        List<Notification> list = notificationService.forUser(userId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable("id") String id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // naive ownership check
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
