package com.example.event_backend.service;

import com.example.event_backend.model.Event;
import com.example.event_backend.model.Role;
import com.example.event_backend.model.User;
import com.example.event_backend.repository.EventRepository;
import com.example.event_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    public Page<User> getFilteredUsers(String search, String roleStr, Pageable pageable) {
        try {
            Role role = (roleStr != null && !roleStr.isEmpty()) ? Role.valueOf(roleStr) : null;
            
            if (search != null && !search.isEmpty() && role != null) {
                return userRepository.findByRoleAndFirstNameContainingIgnoreCaseOrRoleAndLastNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                        role, search, 
                        role, search, 
                        role, search, 
                        pageable);
            } else if (search != null && !search.isEmpty()) {
                return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        search, search, search, pageable);
            } else if (role != null) {
                return userRepository.findByRole(role, pageable);
            } else {
                return userRepository.findAll(pageable);
            }
        } catch (IllegalArgumentException e) {
            return userRepository.findAll(pageable);
        }
    }

    public Page<Event> getFilteredEvents(String search, String category, Pageable pageable) {
        if (search != null && !search.isEmpty() && category != null && !category.isEmpty()) {
            return eventRepository.searchEventsByCategoryAndSearch(category, search, pageable);
        } else if (search != null && !search.isEmpty()) {
            return eventRepository.searchEvents(search, pageable);
        } else if (category != null && !category.isEmpty()) {
            return eventRepository.findByCategory(category, pageable);
        } else {
            return eventRepository.findAll(pageable);
        }
    }

    public List<String> getAllRoles() {
        return Arrays.stream(Role.values())
                   .map(Enum::name)
                   .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return eventRepository.findAllCategories();
    }

    @Transactional
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteEventById(Long id) {
        eventRepository.deleteById(id);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    public void saveEvent(Event event) {
        eventRepository.save(event);
    }

    public long getUserCount() {
        return userRepository.countAllUsers();
    }
    
    public long getOrganizersCount() {
        return userRepository.countByRole(Role.ORGANIZER);
    }
    
    public long getParticipantsCount() {
        return userRepository.countByRole(Role.PARTICIPANT);
    }
    
    public long getAllEventsCount() {
        return eventRepository.countAllEvents();
    }
}