package com.example.security;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // ===== PUBLIC ENDPOINTS (anyone can access) =====

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Welcome! This is the HOME page (public)");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("message", "This is the ABOUT page (public)");
        return "about";
    }

    // ===== PRIVATE ENDPOINTS (should be protected - but NO security yet!) =====

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("message", "This is the DASHBOARD (private) - but anyone can access it now!");
        model.addAttribute("secretData", "Revenue: $1,000,000 | Users: 50,000");
        return "dashboard";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("message", "This is the ADMIN panel (private) - but anyone can access it now!");
        model.addAttribute("secretData", "DB Password: super-secret-123");
        return "admin";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("message", "This is the PROFILE page (private) - but anyone can access it now!");
        model.addAttribute("secretData", "Email: user@example.com | Phone: 555-1234");
        return "profile";
    }
}
