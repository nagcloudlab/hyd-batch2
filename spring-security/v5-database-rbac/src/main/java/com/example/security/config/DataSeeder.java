package com.example.security.config;

import com.example.security.entity.Role;
import com.example.security.entity.User;
import com.example.security.repository.RoleRepository;
import com.example.security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create roles
        Role roleUser = roleRepository.save(new Role("ROLE_USER"));
        Role roleManager = roleRepository.save(new Role("ROLE_MANAGER"));
        Role roleAdmin = roleRepository.save(new Role("ROLE_ADMIN"));

        // Create users
        User user = new User("user", passwordEncoder.encode("password"), "John User");
        user.setRoles(Set.of(roleUser));
        userRepository.save(user);

        User manager = new User("manager", passwordEncoder.encode("password"), "Jane Manager");
        manager.setRoles(Set.of(roleUser, roleManager));
        userRepository.save(manager);

        User admin = new User("admin", passwordEncoder.encode("password"), "Super Admin");
        admin.setRoles(Set.of(roleUser, roleManager, roleAdmin));
        userRepository.save(admin);

        System.out.println("=== Seeded 3 roles and 3 users ===");
    }
}
