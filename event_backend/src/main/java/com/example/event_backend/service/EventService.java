package com.example.event_backend.service;


import com.example.event_backend.model.Event;
import com.example.event_backend.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    // Pobieranie wszystkich wydarzeń
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // Pobieranie wydarzenia po ID
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    // Tworzenie nowego wydarzenia
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    // Usuwanie wydarzenia po ID
    public boolean deleteEvent(Long id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        }
        return false; 
    }
}

