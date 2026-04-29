package com.example.security.controller;

import com.example.security.entity.Role;
import com.example.security.entity.User;
import com.example.security.repository.RoleRepository;
import com.example.security.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // List all users
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin-users";
    }

    // Toggle user enabled/disabled
    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success",
                "User '" + user.getUsername() + "' " + (user.isEnabled() ? "enabled" : "disabled"));
        return "redirect:/admin/users";
    }

    // Update user roles
    @PostMapping("/users/{id}/roles")
    public String updateRoles(@PathVariable Long id,
                              @RequestParam(required = false) List<Long> roleIds,
                              RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow();

        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> newRoles = new HashSet<>(roleRepository.findAllById(roleIds));
            user.setRoles(newRoles);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success",
                    "Roles updated for '" + user.getUsername() + "'");
        }

        return "redirect:/admin/users";
    }

    // Delete user
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow();
        userRepository.delete(user);
        redirectAttributes.addFlashAttribute("success", "User '" + user.getUsername() + "' deleted");
        return "redirect:/admin/users";
    }

    // Add new user (from admin panel)
    @PostMapping("/users/add")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String fullName,
                          @RequestParam(required = false) List<Long> roleIds,
                          RedirectAttributes redirectAttributes) {

        if (userRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Username '" + username + "' already exists!");
            return "redirect:/admin/users";
        }

        User user = new User(username, passwordEncoder.encode(password), fullName);
        if (roleIds != null) {
            user.setRoles(new HashSet<>(roleRepository.findAllById(roleIds)));
        }
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "User '" + username + "' created");
        return "redirect:/admin/users";
    }
}
