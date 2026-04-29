package com.example.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/*


create table todos (
    id serial primary key,
    title varchar(255) not null,
    description text,
    completed boolean default false,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp,
    user_id int references users(id) on delete cascade
);

*/

@Setter
@Getter
@Entity
@jakarta.persistence.Table(name = "todos")
public class Todo {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(nullable = false)
    private String title;

    @jakarta.persistence.Column
    private String description;

    @jakarta.persistence.Column
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private TodoCategory category;

    @jakarta.persistence.Column(nullable = false)
    private boolean completed;

    @jakarta.persistence.Column(name = "created_at", nullable = true, updatable = false)
    private java.time.LocalDateTime createdAt;

    @jakarta.persistence.Column(name = "updated_at", nullable = true)
    private java.time.LocalDateTime updatedAt;

    @jakarta.persistence.ManyToOne
    @jakarta.persistence.JoinColumn(name = "user_id", nullable = false)
    private User user;

}
