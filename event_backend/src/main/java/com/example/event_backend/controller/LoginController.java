package com.example.event_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm(
        @RequestParam(value = "error", required = false) String error,
        @RequestParam(value = "logout", required = false) String logout,
        Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Nieprawidłowy email lub hasło");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Zostałeś wylogowany");
        }
        
        return "login"; 
    }
}