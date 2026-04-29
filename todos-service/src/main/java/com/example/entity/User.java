package com.example.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*

create table users (
    id serial primary key,
    username varchar(255) not null unique,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp,
    active boolean default true
);


*/

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(nullable = false, unique = true)
    private String username;

    @jakarta.persistence.Column(nullable = false, unique = true)
    private String email;

    @jakarta.persistence.Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @jakarta.persistence.Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @jakarta.persistence.Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @jakarta.persistence.Column(nullable = false)
    private boolean active = true;

    @jakarta.persistence.ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @jakarta.persistence.JoinTable(name = "user_roles", joinColumns = @jakarta.persistence.JoinColumn(name = "user_id"), inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "role_id"))
    private java.util.Set<Role> roles;

    @PrePersist
    void onCreate() {
        Date now = new Date();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = new Date();
    }

}
