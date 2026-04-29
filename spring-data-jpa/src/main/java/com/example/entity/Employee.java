package com.example.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Employee {
    @Id
    private int id;
    private String name;
    @ManyToMany(fetch = FetchType.LAZY, cascade = { jakarta.persistence.CascadeType.PERSIST,
            jakarta.persistence.CascadeType.MERGE })
    @JoinTable(name = "employee_project", joinColumns = { @JoinColumn(name = "employee_id") }, inverseJoinColumns = {
            @JoinColumn(name = "project_id") })
    List<Project> projects;
}
