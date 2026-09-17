package edu.cit.gabutan.notification;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(
            NotificationRepository repository) {

        this.repository = repository;
    }

    public void saveMessage(String message) {

        repository.save(
                new Notification(message));
    }
}