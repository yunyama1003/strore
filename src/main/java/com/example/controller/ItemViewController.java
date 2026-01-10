package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.service.ItemService;

@Controller
@RequestMapping("/items")
public class ItemViewController {

    private final ItemService service;

    public ItemViewController(ItemService service) {
        this.service = service;
    }

    @GetMapping("/view")
    public String view(Model model) {
        model.addAttribute("items", service.findAll());
        return "items"; // ← HTMLファイル名
    }
 // ★ フォームからのPOST処理
    @PostMapping("/view")
    public String create(
            @RequestParam String name,
            @RequestParam int quantity
    ) {
        service.create(name, quantity);

        // リダイレクト（二重送信防止）
        return "redirect:/items/view";
    }
    // ★ + / -
    @PostMapping("/{id}/change")
    public String changeQuantity(
            @PathVariable Long id,
            @RequestParam int delta
    ) {
        service.changeQuantity(id, delta);
        return "redirect:/items/view";
    }

    // ★ 削除
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/items/view";
    }
}

