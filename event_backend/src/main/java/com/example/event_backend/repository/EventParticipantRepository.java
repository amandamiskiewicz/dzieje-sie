package com.example.event_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.event_backend.model.Event;
import com.example.event_backend.model.User;
import com.example.event_backend.model.EventParticipant;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {

    boolean existsByUserAndEvent(User user, Event event);

    long countByEvent(Event event);

    List<EventParticipant> findByEvent(Event event);

    List<EventParticipant> findByUser(User user);

    int countByEventId(Long eventId);

    @Modifying
    @Query("DELETE FROM EventParticipant ep WHERE ep.id = :id AND ep.user.id = :userId")
    void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT ep FROM EventParticipant ep JOIN FETCH ep.user WHERE ep.event.id = :eventId")
    List<EventParticipant> findByEventIdWithUser(@Param("eventId") Long eventId);

}
