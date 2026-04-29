package com.example.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // ===== PUBLIC ENDPOINTS =====

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

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    // ===== PRIVATE ENDPOINTS (now protected!) =====

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {

        System.out.println("------------------------------------");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated User: " + authentication.getName());
        authentication.getAuthorities().forEach(System.out::println);

        System.out.println("------------------------------------");

        model.addAttribute("message", "Welcome " + auth.getName() + "! This is the DASHBOARD (protected)");
        model.addAttribute("secretData", "Revenue: $1,000,000 | Users: 50,000");
        model.addAttribute("username", auth.getName());
        model.addAttribute("roles", auth.getAuthorities().toString());
        return "dashboard";
    }

    @GetMapping("/admin")
    public String admin(Model model, Authentication auth) {
        model.addAttribute("message", "Admin panel - only ADMIN role can see this!");
        model.addAttribute("secretData", "DB Password: super-secret-123");
        model.addAttribute("username", auth.getName());
        model.addAttribute("roles", auth.getAuthorities().toString());
        return "admin";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication auth) {
        model.addAttribute("message", "Profile page for: " + auth.getName());
        model.addAttribute("secretData", "Email: " + auth.getName() + "@example.com | Phone: 555-1234");
        model.addAttribute("username", auth.getName());
        model.addAttribute("roles", auth.getAuthorities().toString());
        return "profile";
    }
}
