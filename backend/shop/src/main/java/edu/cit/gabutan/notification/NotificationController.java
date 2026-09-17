package edu.cit.gabutan.notification;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(
            NotificationRepository repository) {

        this.repository = repository;
    }

    @GetMapping
    public List<Notification> getNotifications() {

        return repository.findAll();
    }
}