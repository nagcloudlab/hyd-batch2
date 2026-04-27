package com.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

// @Data
@Setter
@Getter
@Entity
public class Girl {
    @Id
    private int id;
    private String name;
    @OneToOne(mappedBy = "girlfriend", fetch = jakarta.persistence.FetchType.LAZY)
    private Boy boyfriend;
}
