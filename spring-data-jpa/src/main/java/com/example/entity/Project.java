package com.example.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Project {
    @Id
    private int id;
    private String name;
    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY, cascade = { jakarta.persistence.CascadeType.PERSIST,
            jakarta.persistence.CascadeType.MERGE })
    private List<Employee> employees;
}
