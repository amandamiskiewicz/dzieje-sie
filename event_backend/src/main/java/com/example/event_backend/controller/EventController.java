package com.example.event_backend.controller;

import com.example.event_backend.dto.EventDto;
import com.example.event_backend.model.Event;
import com.example.event_backend.model.EventParticipant;
import com.example.event_backend.model.User;
import com.example.event_backend.repository.EventParticipantRepository;
import com.example.event_backend.repository.EventRepository;
import com.example.event_backend.repository.UserRepository;
import com.example.event_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventParticipantRepository eventParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/images/";

    @GetMapping
    public String listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventsPage;
        
        if (search != null && !search.isEmpty()) {
            eventsPage = eventRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrCityContainingIgnoreCase(
                    search, search, search, pageable);
        } else if (category != null && !category.isEmpty() && !category.equals("all")) {
            eventsPage = eventRepository.findByCategory(category, pageable);
        } else if (city != null && !city.isEmpty()) {
            eventsPage = eventRepository.findByCity(city, pageable);
        } else {
            eventsPage = eventRepository.findAll(pageable);
        }

        model.addAttribute("events", eventsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("currentCategory", category != null ? category : "all");
        model.addAttribute("categories", eventRepository.findAllCategories());
        model.addAttribute("cities", eventRepository.findDistinctCities());
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("cityQuery", city != null ? city : "");
        
        return "events";
    }

    @GetMapping("/create_event")
    public String showEventForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        model.addAttribute("eventDto", new EventDto());
        return "create_event";
    }

    @PostMapping("/create_event")
    public String createEvent(@ModelAttribute("eventDto") @Valid EventDto eventDto,
                            BindingResult result,
                            @RequestParam("image") MultipartFile image,
                            Principal principal) throws IOException {
        if (principal == null) {
            return "redirect:/login";
        }

        if (image.isEmpty()) {
            result.rejectValue("imageUrl", null, "Image is required");
        }

        if (result.hasErrors()) {
            return "create_event";
        }

        String imageUrl = saveImage(image);

        User user = userService.findByEmail(principal.getName());
        Event event = new Event(
                eventDto.getTitle(),
                eventDto.getDescription(),
                imageUrl,
                eventDto.getStartDate(),
                eventDto.getEndDate(),
                eventDto.getMaxParticipants(),
                eventDto.getPrice(),
                eventDto.getCity(),
                eventDto.getStreet(),
                eventDto.getPostalCode(),
                eventDto.getCountry(),
                eventDto.getCategory(),
                user
        );

        eventRepository.save(event);
        return "redirect:/events";
    }

    private String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile.isEmpty()) {
            return null;
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath);

        return fileName;
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid event Id: " + id));
        User user = userService.findByEmail(principal.getName());

        if (!event.getUser().getId().equals(user.getId())) {
            return "redirect:/events";
        }

        EventDto eventDto = new EventDto();
        eventDto.setTitle(event.getTitle());
        eventDto.setDescription(event.getDescription());
        eventDto.setImageUrl(event.getImageUrl());
        eventDto.setStartDate(event.getStartDate());
        eventDto.setEndDate(event.getEndDate());
        eventDto.setMaxParticipants(event.getMaxParticipants());
        eventDto.setPrice(event.getPrice());
        eventDto.setCity(event.getCity());
        eventDto.setStreet(event.getStreet());
        eventDto.setPostalCode(event.getPostalCode());
        eventDto.setCountry(event.getCountry());
        eventDto.setCategory(event.getCategory());

        model.addAttribute("eventDto", eventDto);
        return "edit_event";
    }

    @PostMapping("/edit/{id}")
    public String updateEvent(@PathVariable("id") Long id, 
                            @Valid @ModelAttribute("eventDto") EventDto eventDto, 
                            BindingResult result, 
                            @RequestParam(value = "image", required = false) MultipartFile imageFile,
                            Principal principal) throws IOException {
        if (result.hasErrors()) {
            return "edit_event";
        }

        if (principal == null) {
            return "redirect:/login";
        }

        Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid event Id: " + id));
        User user = userService.findByEmail(principal.getName());

        if (!event.getUser().getId().equals(user.getId())) {
            return "redirect:/events";
        }

        event.setTitle(eventDto.getTitle());
        event.setDescription(eventDto.getDescription());

        if (!imageFile.isEmpty()) {
            event.setImageUrl(saveImage(imageFile));
        }

        event.setStartDate(eventDto.getStartDate());
        event.setEndDate(eventDto.getEndDate());
        event.setMaxParticipants(eventDto.getMaxParticipants());
        event.setPrice(eventDto.getPrice());
        event.setCity(eventDto.getCity());
        event.setStreet(eventDto.getStreet());
        event.setPostalCode(eventDto.getPostalCode());
        event.setCountry(eventDto.getCountry());
        event.setCategory(eventDto.getCategory());

        eventRepository.save(event);
        return "redirect:/events";
    }

    @GetMapping("/delete/{id}")
    public String deleteEvent(@PathVariable("id") Long id, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid event Id: " + id));
        User user = userService.findByEmail(principal.getName());

        if (!event.getUser().getId().equals(user.getId())) {
            return "redirect:/events";
        }

        List<EventParticipant> participants = eventParticipantRepository.findByEvent(event);
        eventParticipantRepository.deleteAll(participants);
        eventRepository.delete(event);

        return "redirect:/events";
    }

    @GetMapping("/event/{id}")
    public String showEventDetails(@PathVariable("id") Long id, Model model, Principal principal) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid event Id: " + id));

        boolean isRegistered = false;
        if (principal != null) {
            User user = userRepository.findByEmail(principal.getName());
            if (user != null) {
                isRegistered = eventParticipantRepository.existsByUserAndEvent(user, event);
            }
        }

        long participantsCount = eventParticipantRepository.countByEvent(event);
        int availableSpots = event.getMaxParticipants() - (int)participantsCount;

        model.addAttribute("event", event);
        model.addAttribute("isAuthenticated", principal != null);
        model.addAttribute("isRegistered", isRegistered);
        model.addAttribute("availableSpots", availableSpots);
        model.addAttribute("participantsCount", participantsCount);

        return "event_details";
    }

    @PostMapping("/event/{id}/register")
    public String registerForEvent(@PathVariable("id") Long id, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id: " + id));

        User user = userRepository.findByEmail(principal.getName());
        
        if (user == null) {
            return "redirect:/login";
        }

        if (eventParticipantRepository.existsByUserAndEvent(user, event)) {
            return "redirect:/events/event/" + id + "?error=already_registered";
        }

        long participantCount = eventParticipantRepository.countByEvent(event);
        if (participantCount >= event.getMaxParticipants()) {
            return "redirect:/events/event/" + id + "?error=max_participants_reached";
        }

        EventParticipant participant = new EventParticipant();
        participant.setUser(user);
        participant.setEvent(event);
        eventParticipantRepository.save(participant);

        return "redirect:/events/event/" + id + "?success=true";
    }
}