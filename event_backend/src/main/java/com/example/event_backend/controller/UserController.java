package com.example.event_backend.controller;

import com.example.event_backend.dto.RegisterDto;
import com.example.event_backend.model.Organizer;
import com.example.event_backend.model.Participant;
import com.example.event_backend.model.Role;
import com.example.event_backend.model.User;
import com.example.event_backend.repository.UserRepository;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class UserController {

    @Autowired
    private UserRepository repo;

    @GetMapping("/register")
    public String showRegisterChoicePage() {
        return "register"; 
    }

    @GetMapping("/register-participant")
    public String showRegisterUserPage(Model model) {
        RegisterDto registerDto = new RegisterDto();
        model.addAttribute("registerDto", registerDto);
        model.addAttribute("success", false);
        return "register-participant"; 
    }

    @PostMapping("/register-participant")
    public String registerUser(
        Model model,
        @Validated(Participant.class) @ModelAttribute RegisterDto registerDto,
        BindingResult result)
    {

        if (result.hasErrors()) {
            return "register-participant"; 
        }

        User user = repo.findByEmail(registerDto.getEmail());
        if (user != null) {
            result.addError(
                new FieldError("registerDto", "email", "Konto z podanym emailem już istnieje")
            );
        }

        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            result.addError(
                new FieldError("registerDto", "confirmPassword", "Hasła nie są takie same")
            );
        }

        if (result.hasErrors()) {
            return "register-participant"; 
        }

        try {

            var bCryptEncoder = new BCryptPasswordEncoder();

            User newUser = new User();
            newUser.setFirstName(registerDto.getFirstName());
            newUser.setLastName(registerDto.getLastName());
            newUser.setEmail(registerDto.getEmail());
            newUser.setPhone(registerDto.getPhone());
            newUser.setRole(Role.PARTICIPANT); 
            newUser.setCreatedAt(LocalDate.now());
            newUser.setPassword(bCryptEncoder.encode(registerDto.getPassword()));

            repo.save(newUser);

            model.addAttribute("registerDto", new RegisterDto());
            model.addAttribute("success", true);

        } catch (Exception ex) {
            result.addError(
                new FieldError("registerDto", "firstName", ex.getMessage())
            );
        }

        return "register-participant";
    }

    @GetMapping("/register-organizer")
    public String showRegisterOrganizerPage(Model model) {
        RegisterDto registerDto = new RegisterDto();
        model.addAttribute("registerDto", registerDto);
        model.addAttribute("success", false);
        return "register-organizer"; 
    }

    @PostMapping("/register-organizer")
    public String registerOrganizer(
        Model model,
        @Validated(Organizer.class) @ModelAttribute RegisterDto registerDto,
        BindingResult result)
    {

        if (result.hasErrors()) {
            return "register-organizer"; 
        }

        User user = repo.findByEmail(registerDto.getEmail());
        if (user != null) {
            result.addError(
                new FieldError("registerDto", "email", "Konto z podanym emailem już istnieje")
            );
        }

        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            result.addError(
                new FieldError("registerDto", "confirmPassword", "Hasła nie są takie same")
            );
        }

        if (result.hasErrors()) {
            return "register-organizer"; 
        }

        try {
            var bCryptEncoder = new BCryptPasswordEncoder();

            User newUser = new User();
            newUser.setCompanyName(registerDto.getCompanyName());
            newUser.setEmail(registerDto.getEmail());
            newUser.setPhone(registerDto.getPhone());
            newUser.setRole(Role.ORGANIZER); 
            newUser.setCreatedAt(LocalDate.now());
            newUser.setPassword(bCryptEncoder.encode(registerDto.getPassword()));

            repo.save(newUser);

            model.addAttribute("registerDto", new RegisterDto());
            model.addAttribute("success", true);

        } catch (Exception ex) {
            result.addError(
                new FieldError("registerDto", "companyName", ex.getMessage())
            );
        }

        return "register-organizer";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(Model model, Authentication authentication) {
        User user = repo.findByEmail(authentication.getName());
        model.addAttribute("user", user);
        return "edit_profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute User user,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        User existingUser = repo.findByEmail(authentication.getName());
        
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhone(user.getPhone());
        
        if (existingUser.getRole() == Role.ORGANIZER) {
            existingUser.setCompanyName(user.getCompanyName());
        }
        
        repo.save(existingUser);
        
        redirectAttributes.addFlashAttribute("successMessage", "Profil został zaktualizowany!");
        return "redirect:/profile/edit";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        User user = repo.findByEmail(authentication.getName());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        if (!encoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Obecne hasło jest nieprawidłowe");
            return "redirect:/profile/edit";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nowe hasła nie są identyczne");
            return "redirect:/profile/edit";
        }
        
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "Hasło musi mieć co najmniej 8 znaków");
            return "redirect:/profile/edit";
        }
        
        user.setPassword(encoder.encode(newPassword));
        repo.save(user);
        
        redirectAttributes.addFlashAttribute("successMessage", "Hasło zostało zmienione!");
        return "redirect:/profile/edit";
    }
}
