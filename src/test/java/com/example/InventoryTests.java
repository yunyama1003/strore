package com.example;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.example.dto.ItemForm;
import com.example.entity.*;
import com.example.repository.ItemRepository;
import com.example.service.ItemService;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:inventory-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class InventoryTests {
    @Autowired MockMvc mvc;
    @Autowired ItemService service;
    @Autowired ItemRepository repository;

    @BeforeEach
    void clear() { repository.deleteAll(); }

    private ItemForm form(String name, int quantity, int minimum, Category category) {
        ItemForm form = new ItemForm();
        form.setName(name);
        form.setQuantity(quantity);
        form.setMinimumQuantity(minimum);
        form.setCategory(category);
        return form;
    }

    @Test
    void rendersEmptyAndPopulatedPagesAndCompletesCrud() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(content().string(containsString("日用品を登録しましょう")));
        mvc.perform(get("/items/new")).andExpect(status().isOk());
        mvc.perform(post("/items/view").with(csrf()).param("name", "歯磨き粉")
            .param("category", "TOILETRIES").param("quantity", "0")
            .param("minimumQuantity", "1").param("note", "詰め替え"))
            .andExpect(redirectedUrl("/"));
        Item item = service.findAll().getFirst();
        assertThat(item.getCreatedAt()).isNotNull();
        mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(content().string(containsString("歯磨き粉")));
        mvc.perform(get("/shopping")).andExpect(status().isOk())
            .andExpect(content().string(containsString("リストをコピー")));
        mvc.perform(get("/items/{id}/edit", item.getId())).andExpect(status().isOk());
        mvc.perform(post("/items/{id}/edit", item.getId()).with(csrf())
            .param("name", "新しい歯磨き粉").param("category", "TOILETRIES")
            .param("quantity", "2").param("minimumQuantity", "1").param("note", "変更済み"))
            .andExpect(redirectedUrl("/"));
        assertThat(service.find(item.getId()).getName()).isEqualTo("新しい歯磨き粉");
        assertThat(service.list(null, true)).isEmpty();
        mvc.perform(post("/items/{id}/delete", item.getId()).with(csrf()))
            .andExpect(redirectedUrl("/"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void classifiesBoundariesAndFiltersShoppingAndCategories() throws Exception {
        Item zero = service.create(form("歯磨き粉", 0, 0, Category.TOILETRIES));
        Item low = service.create(form("シャンプー", 1, 1, Category.BATH));
        Item ok = service.create(form("ティッシュ", 2, 1, Category.PAPER));
        assertThat(zero.getStatus()).isEqualTo(StockStatus.OUT);
        assertThat(low.getStatus()).isEqualTo(StockStatus.LOW);
        assertThat(ok.getStatus()).isEqualTo(StockStatus.OK);
        assertThat(service.list(null, true)).extracting(Item::getId).containsExactly(zero.getId(), low.getId());
        assertThat(service.list(Category.PAPER, false)).extracting(Item::getId).containsExactly(ok.getId());
        assertThat(service.list(Category.PAPER, true)).isEmpty();
        assertThat(service.shoppingText(service.list(null, true)))
            .contains("歯磨き粉（現在のストック：0個）").doesNotContain("ティッシュ");
        mvc.perform(get("/").param("category", "BATH"))
            .andExpect(status().isOk()).andExpect(content().string(containsString("シャンプー")));
    }

    @Test
    void rejectsInvalidFormsAndMissingIds() throws Exception {
        for (String value : new String[]{"-1", "1.5", "abc", "", "10000"}) {
            mvc.perform(post("/items/view").with(csrf()).param("name", "洗剤")
                .param("category", "KITCHEN").param("quantity", value).param("minimumQuantity", "1"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("itemForm", "quantity"));
        }
        mvc.perform(post("/items/view").with(csrf()).param("name", "  ")
            .param("quantity", "1").param("minimumQuantity", "-1").param("category", "WRONG"))
            .andExpect(model().attributeHasFieldErrors("itemForm", "name", "minimumQuantity", "category"));
        assertThat(repository.count()).isZero();
        mvc.perform(get("/items/99999/edit")).andExpect(status().isNotFound());
        mvc.perform(post("/items/99999/delete").with(csrf())).andExpect(status().isNotFound());
        mvc.perform(post("/items/99999/change").with(csrf()).param("delta", "1"))
            .andExpect(status().isNotFound());
        mvc.perform(get("/?category=WRONG")).andExpect(status().isBadRequest());
    }

    @Test
    void clampsCountsAndPreservesFilterAndRejectsForgedDelta() throws Exception {
        Item item = service.create(form("洗剤", 0, 1, Category.KITCHEN));
        mvc.perform(post("/items/{id}/change", item.getId()).with(csrf()).param("delta", "-1"))
            .andExpect(redirectedUrl("/"));
        assertThat(service.find(item.getId()).getQuantity()).isZero();
        mvc.perform(post("/items/{id}/change", item.getId()).with(csrf())
            .param("delta", "1").param("category", "KITCHEN").param("shopping", "true"))
            .andExpect(redirectedUrl("/shopping?category=KITCHEN"));
        mvc.perform(post("/items/{id}/change", item.getId()).with(csrf()).param("delta", "-99"))
            .andExpect(status().isBadRequest());
        assertThat(service.find(item.getId()).getQuantity()).isEqualTo(1);
        service.updateQuantity(item.getId(), 9999);
        service.changeQuantity(item.getId(), 1);
        assertThat(service.find(item.getId()).getQuantity()).isEqualTo(9999);
    }

    @Test
    void jsonApiCannotBypassValidationOrCsrf() throws Exception {
        Item item = service.create(form("洗剤", 1, 1, Category.KITCHEN));
        mvc.perform(post("/items/{id}/delete", item.getId())).andExpect(status().isForbidden());
        for (String value : new String[]{"-1", "null", "1.5"}) {
            mvc.perform(patch("/items/{id}", item.getId()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":" + value + "}"))
                .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/items").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\",\"quantity\":-1}"))
            .andExpect(status().isBadRequest());
        assertThat(service.find(item.getId()).getQuantity()).isEqualTo(1);
    }

    @Test
    void concurrentIncrementsDoNotLoseUpdates() throws Exception {
        Item item = service.create(form("ティッシュ", 0, 1, Category.PAPER));
        try (var executor = Executors.newFixedThreadPool(4)) {
            var tasks = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < 12; i++) tasks.add(executor.submit(() -> service.changeQuantity(item.getId(), 1)));
            for (var task : tasks) task.get(15, TimeUnit.SECONDS);
        }
        assertThat(service.find(item.getId()).getQuantity()).isEqualTo(12);
    }
}
