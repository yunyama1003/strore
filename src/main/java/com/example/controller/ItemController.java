package com.example.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.ItemRequest;
import com.example.dto.UpdateQuantityRequest;
import com.example.entity.Item;
import com.example.service.ItemService;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    public Item create(@RequestBody ItemRequest request) {
        return service.create(request.getName(), request.getQuantity());
    }

    @GetMapping
    public List<Item> list() {
        return service.findAll();
    }

    @PatchMapping("/{id}")
    public Item update(
        @PathVariable Long id,
        @RequestBody UpdateQuantityRequest request
    ) {
        return service.updateQuantity(id, request.getQuantity());
    }
}

