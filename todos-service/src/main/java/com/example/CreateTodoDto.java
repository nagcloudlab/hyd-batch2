package com.example;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateTodoDto {
    private String title;
    private boolean completed;
    private String category;
}
