package com.example.event_backend.controller;

import com.example.event_backend.model.Event;
import com.example.event_backend.repository.EventParticipantRepository;
import com.example.event_backend.repository.EventRepository;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventParticipantRepository eventParticipantRepository;

    @GetMapping("/")
    public String home(@RequestParam(value = "city", required = false) String city, Model model) {
        List<String> popularCities = List.of(
                "Warszawa", "Kraków", "Wrocław", "Gdańsk", "Poznań",
                "Łódź", "Szczecin", "Lublin", "Online", "Katowice"
        );

        List<Event> allEvents = eventRepository.findAll();

        allEvents.forEach(event -> {
            int participantsCount = eventParticipantRepository.countByEventId(event.getId());
            event.setParticipantsCount(participantsCount);
        });

        if (city != null && !city.isEmpty()) {
            allEvents = allEvents.stream()
                    .filter(event -> city.equalsIgnoreCase(event.getCity()))
                    .collect(Collectors.toList());
            model.addAttribute("selectedCity", city);
        }

        List<Event> featuredEvents = allEvents.stream()
                .sorted(Comparator.comparingInt(Event::getParticipantsCount).reversed())
                .limit(6)
                .collect(Collectors.toList());

        List<List<Event>> featuredEventSlides = ListUtils.partition(featuredEvents, 3);

        model.addAttribute("featuredEventSlides", featuredEventSlides);

        Map<String, List<Event>> categorizedEvents = allEvents.stream()
                .collect(Collectors.groupingBy(Event::getCategory));

        model.addAttribute("featuredEvents", featuredEvents);
        model.addAttribute("categorizedEvents", categorizedEvents);
        model.addAttribute("cities", popularCities);

        return "index";
    }
    @GetMapping("/search")
    public String searchEvents(@RequestParam(required = false) String search, 
                            @RequestParam(required = false) String city,
                            Model model) {
        return "redirect:/events?search=" + (search != null ? search : "") + 
            "&city=" + (city != null ? city : "");
    }
}
