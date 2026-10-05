package edu.cit.gabutan.channel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface TianggeEventRepository
        extends JpaRepository<TianggeEvent, Long> {

    Optional<TianggeEvent> findByEventId(String eventId);
}
