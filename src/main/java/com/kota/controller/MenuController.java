package com.kota.controller;

import com.kota.dto.response.MenuItemResponse;
import com.kota.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getAvailableItems() {
        return ResponseEntity.ok(menuService.getAllAvailableItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.toResponse(menuService.getItemById(id)));
    }
}
