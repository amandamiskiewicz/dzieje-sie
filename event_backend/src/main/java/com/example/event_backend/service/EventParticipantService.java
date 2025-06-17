package com.example.event_backend.service;

import com.example.event_backend.model.Event;
import com.example.event_backend.model.EventParticipant;
import com.example.event_backend.repository.EventParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventParticipantService {
    
    private final EventParticipantRepository eventParticipantRepository;

    public EventParticipantService(EventParticipantRepository eventParticipantRepository) {
        this.eventParticipantRepository = eventParticipantRepository;
    }

    public List<EventParticipant> getParticipantsByEvent(Event event) {
        return eventParticipantRepository.findByEventIdWithUser(event.getId());
    }

    @Transactional
    public void removeParticipant(Long participantId) {
        eventParticipantRepository.deleteById(participantId);
    }
}