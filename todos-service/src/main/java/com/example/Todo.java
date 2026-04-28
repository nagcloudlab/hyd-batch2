package com.example;

import java.util.Locale.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Todo {

    private Long id;
    private String title;
    private boolean completed;
    private TodoCategory category;

}
