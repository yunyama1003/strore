package com.example.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.example.dto.ItemRequest;
import com.example.dto.UpdateQuantityRequest;
import com.example.entity.Item;
import com.example.service.ItemService;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;
    public ItemController(ItemService service) { this.service = service; }

    @PostMapping
    public Item create(@Valid @RequestBody ItemRequest request) {
        return service.create(request);
    }
    @GetMapping
    public List<Item> list() { return service.findAll(); }
    @PatchMapping("/{id}")
    public Item update(@PathVariable Long id, @Valid @RequestBody UpdateQuantityRequest request) {
        return service.updateQuantity(id, request.getQuantity());
    }
}

