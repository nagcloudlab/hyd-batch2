package com.example.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/*


create table roles (
    id serial primary key,
    name varchar(255) not null unique,
    description text
);


*/

@Getter
@Setter
@Entity
@jakarta.persistence.Table(name = "roles")
public class Role {

    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(nullable = false, unique = true)
    private String name;

    @jakarta.persistence.Column
    private String description;

}
