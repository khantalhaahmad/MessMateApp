package com.example.messmateapp.data.model;

import com.example.messmateapp.domain.model.MenuItem;
import com.example.messmateapp.domain.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class RestaurantMapper {

    public static Restaurant map(RestaurantDto dto) {

        if (dto == null) return null;

        List<MenuItem> menuItems = new ArrayList<>();

        // 🔥 CORRECT NESTED ACCESS
        if (dto.getMenu() != null && dto.getMenu().getItems() != null) {
            for (MenuItemDto itemDto : dto.getMenu().getItems()) {
                menuItems.add(
                        new MenuItem(
                                itemDto.getId(),
                                itemDto.getName(),
                                itemDto.getDescription(),
                                itemDto.getPrice(),
                                itemDto.getImage(),
                                itemDto.isVeg()
                        )
                );
            }
        }

        return new Restaurant(
                dto.getId(),
                dto.getName(),
                dto.getLocation(),
                dto.getBanner(),
                dto.getRating(),
                menuItems   // ✅ NOW PROPERLY FILLED
        );
    }
}
