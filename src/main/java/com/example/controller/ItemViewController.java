package com.example.controller;

import com.example.dto.ItemForm;
import com.example.entity.Category;
import com.example.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ItemViewController {
    private final ItemService service;
    public ItemViewController(ItemService service) { this.service = service; }

    @ModelAttribute("categories")
    public Category[] categories() { return Category.values(); }

    @GetMapping({"/", "/items/view"})
    public String list(@RequestParam(required = false) Category category, Model model) {
        return showList(category, false, model);
    }
    @GetMapping("/shopping")
    public String shopping(@RequestParam(required = false) Category category, Model model) {
        return showList(category, true, model);
    }
    private String showList(Category category, boolean shopping, Model model) {
        var items = service.list(category, shopping);
        model.addAttribute("items", items);
        model.addAttribute("shopping", shopping);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("totalCount", service.findAll().size());
        model.addAttribute("shoppingCount", service.list(null, true).size());
        model.addAttribute("shoppingText", service.shoppingText(items));
        return "items";
    }
    @GetMapping("/items/new")
    public String newItem(Model model) {
        model.addAttribute("itemForm", new ItemForm());
        return formPage(null, model);
    }
    @PostMapping("/items/view")
    public String create(@Valid @ModelAttribute ItemForm itemForm, BindingResult errors,
                         Model model, RedirectAttributes flash) {
        if (errors.hasErrors()) return formPage(null, model);
        service.create(itemForm);
        flash.addFlashAttribute("notice", "商品を登録しました。");
        return "redirect:/";
    }
    @GetMapping("/items/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("itemForm", ItemForm.from(service.find(id)));
        return formPage(id, model);
    }
    @PostMapping("/items/{id}/edit")
    public String update(@PathVariable Long id, @Valid @ModelAttribute ItemForm itemForm,
                         BindingResult errors, Model model, RedirectAttributes flash) {
        service.find(id);
        if (errors.hasErrors()) return formPage(id, model);
        service.edit(id, itemForm);
        flash.addFlashAttribute("notice", "変更を保存しました。");
        return "redirect:/";
    }
    private String formPage(Long id, Model model) {
        model.addAttribute("itemId", id);
        model.addAttribute("editing", id != null);
        return "item-form";
    }
    @PostMapping("/items/{id}/change")
    public String change(@PathVariable Long id, @RequestParam int delta,
                         @RequestParam(defaultValue = "false") boolean shopping,
                         @RequestParam(required = false) Category category, RedirectAttributes flash) {
        service.changeQuantity(id, delta);
        flash.addFlashAttribute("notice", "ストック数を更新しました。");
        return back(shopping, category);
    }
    @PostMapping("/items/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(defaultValue = "false") boolean shopping,
                         @RequestParam(required = false) Category category, RedirectAttributes flash) {
        service.delete(id);
        flash.addFlashAttribute("notice", "商品を削除しました。");
        return back(shopping, category);
    }
    private String back(boolean shopping, Category category) {
        return "redirect:" + (shopping ? "/shopping" : "/")
            + (category == null ? "" : "?category=" + category.name());
    }
}

