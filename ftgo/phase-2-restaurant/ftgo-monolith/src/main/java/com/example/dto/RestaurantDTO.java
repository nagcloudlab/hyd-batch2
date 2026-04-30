package com.example.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDTO {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private boolean active;
    private List<MenuItemDTO> menuItems;
}
