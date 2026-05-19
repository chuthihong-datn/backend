package com.example.food_app.controller.user;

import com.example.food_app.dto.response.user.MenuDetailResponse;
import com.example.food_app.dto.response.user.MenuResponse;
import com.example.food_app.service.user.MenuService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public List<MenuResponse> getListMenuByCategory(@PathVariable Long id){
        return menuService.getMenusByCategory(id);
    }

    @GetMapping("/{id}")
    public MenuDetailResponse getMenuDetail(@PathVariable Long id){
        return menuService.getMenuDetail(id);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MenuResponse>> searchMenu(
            @RequestParam String keyword) {

        return ResponseEntity.ok(menuService.searchMenus(keyword));
    }
}
