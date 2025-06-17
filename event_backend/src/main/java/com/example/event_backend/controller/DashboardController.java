package com.example.event_backend.controller;

import com.example.event_backend.model.Event;
import com.example.event_backend.model.EventParticipant;
import com.example.event_backend.model.User;
import com.example.event_backend.repository.EventParticipantRepository;
import com.example.event_backend.repository.EventRepository;
import com.example.event_backend.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventParticipantRepository eventParticipantRepository;

    public DashboardController(
            UserRepository userRepository,
            EventRepository eventRepository,
            EventParticipantRepository eventParticipantRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.eventParticipantRepository = eventParticipantRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                              a.getAuthority().equals("ROLE_ORGANIZER"))) {
            return "redirect:/dashboard/organizer";
        } else {
            return "redirect:/dashboard/participant";
        }
    }

    @GetMapping("/dashboard/organizer")
    public String organizerDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        List<Event> events = eventRepository.findByUserId(user.getId());
        
        events.forEach(event -> {
            int participantsCount = eventParticipantRepository.countByEventId(event.getId());
            event.setParticipantsCount(participantsCount);
            
            event.setStatus(determineEventStatus(event));
        });
        
        long activeEvents = events.stream()
            .filter(e -> "ACTIVE".equals(e.getStatus()))
            .count();
        
        long upcomingEvents = events.stream()
            .filter(e -> "UPCOMING".equals(e.getStatus()))
            .count();
        
        long totalEvents = events.size();
        
        long totalParticipants = events.stream()
            .mapToInt(Event::getParticipantsCount)
            .sum();
        
        List<Event> upcomingEventsList = events.stream()
            .filter(e -> "UPCOMING".equals(e.getStatus()))
            .collect(Collectors.toList());
        
        model.addAttribute("user", user);
        model.addAttribute("events", events);
        model.addAttribute("upcomingEvents", upcomingEventsList);
        model.addAttribute("stats", Map.of(
            "activeEvents", activeEvents,
            "upcomingEvents", upcomingEvents,
            "totalEvents", totalEvents,
            "totalParticipants", totalParticipants
        ));
        
        return "dashboard-organizer";
    }

    private String determineEventStatus(Event event) {
        LocalDateTime now = LocalDateTime.now();
        
        if (event.getStartDate() != null && now.isBefore(event.getStartDate())) {
            return "UPCOMING";
        } else if (event.getEndDate() != null && now.isAfter(event.getEndDate())) {
            return "FINISHED";
        }
        return "ACTIVE";
    }

    @GetMapping("/dashboard/participant")
    public String participantDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        List<EventParticipant> participations = eventParticipantRepository.findByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("participations", participations);
        return "dashboard-participant"; 
    }

    @PostMapping("/event-participants/{participationId}/cancel")
    public String cancelParticipation(
            @PathVariable Long participationId,
            RedirectAttributes redirectAttributes) {
        try {
            eventParticipantRepository.deleteById(participationId);
            redirectAttributes.addFlashAttribute("cancelSuccess", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Wystąpił błąd podczas anulowania zapisu");
        }
        return "redirect:/dashboard/participant";
    }
}