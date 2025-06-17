package com.example.event_backend.controller;

import com.example.event_backend.model.Event;
import com.example.event_backend.model.User;
import com.example.event_backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("userCount", adminService.getUserCount());
        model.addAttribute("allEventsCount", adminService.getAllEventsCount());
        return "dashboard-admin";
    }

    @GetMapping("/users")
    public String showUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName,asc") String sort,
            Model model) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;

        Page<User> usersPage = adminService.getFilteredUsers(search, role, PageRequest.of(page - 1, size, Sort.by(direction, sortField)));
        
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("allRoles", adminService.getAllRoles());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("sort", sort);
        
        return "admin-users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        adminService.deleteUserById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/events")
    public String showEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title,asc") String sort,
            Model model) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;

        Page<Event> eventsPage = adminService.getFilteredEvents(search, category, PageRequest.of(page - 1, size, Sort.by(direction, sortField)));
        
        model.addAttribute("events", eventsPage.getContent());
        model.addAttribute("allCategories", adminService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);
        
        return "admin-events";
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        adminService.deleteEventById(id);
        return "redirect:/admin/events";
    }

    @GetMapping("/events/edit/{id}")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event event = adminService.getEventById(id);
        if (event == null) {
            return "redirect:/admin/events";
        }
        model.addAttribute("event", event);
        return "edit-event";
    }

    @PostMapping("/events/update")
    public String updateEvent(@ModelAttribute("event") @Valid Event event,
                            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("event", event);
            return "edit-event";
        }
        adminService.saveEvent(event);
        return "redirect:/admin/events";
    }
}