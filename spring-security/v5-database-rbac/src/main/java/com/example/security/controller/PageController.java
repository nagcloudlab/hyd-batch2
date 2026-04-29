package com.example.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("roles", auth.getAuthorities().toString());
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("roles", auth.getAuthorities().toString());
        return "profile";
    }

    @GetMapping("/manager")
    public String managerPage(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        return "manager";
    }
}
