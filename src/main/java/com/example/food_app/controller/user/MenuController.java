package com.example.food_app.controller.user;

import com.example.food_app.dto.response.user.MenuDetailResponse;
import com.example.food_app.dto.response.user.MenuResponse;
import com.example.food_app.service.user.MenuService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/menus")
@AllArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping
    public List<MenuResponse> getListMenu(){
        return menuService.getListMenu();
    }

    @GetMapping("/category/{id}")
    public List<MenuResponse> getListMenuByCategory(@PathVariable BigInteger id){
        return menuService.getMenusByCategory(id);
    }

    @GetMapping("/{id}")
    public MenuDetailResponse getMenuDetail(@PathVariable BigInteger id){
        return menuService.getMenuDetail(id);
    }
}
