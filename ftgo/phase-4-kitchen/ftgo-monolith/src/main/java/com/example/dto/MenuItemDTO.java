package com.example.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemDTO {

    private Long id;
    private String name;
    private BigDecimal price;
    private boolean available;
}
