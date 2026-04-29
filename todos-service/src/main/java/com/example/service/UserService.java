package com.example.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.CreateUserDto;
import com.example.entity.Role;
import com.example.entity.User;
import com.example.repository.RoleRepository;
import com.example.repository.UserRepository;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(CreateUserDto createUserDto) {

        User user = new User();
        user.setUsername(createUserDto.getUsername());
        user.setEmail(createUserDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(createUserDto.getPassword()));

        List<Role> roles = roleRepository.findAllById(List.of(2L));
        // convert List<Role> to Set<Role>
        Set<Role> authorities = Set.copyOf(roles);
        user.setRoles(authorities);

        userRepository.save(user);

        // List<GrantedAuthority> grantedAuthorities = savedUser.getRoles().stream()
        // .map(role -> (GrantedAuthority) role::getName)
        // .toList();

        // Authentication authentication = new UsernamePasswordAuthenticationToken(
        // savedUser.getUsername(),
        // null,
        // grantedAuthorities);

        // SecurityContextHolder.getContext().setAuthentication(authentication);

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        User user = userOptional.get();
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(),
                true,
                true,
                true,
                user.getRoles().stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                role.getName()))
                        .toList());
        return userDetails;
    }

}
