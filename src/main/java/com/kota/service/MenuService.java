package com.kota.service;

import com.kota.dto.request.CreateMenuItemRequest;
import com.kota.dto.request.RecipeRequest;
import com.kota.dto.response.MenuItemResponse;
import com.kota.exception.ResourceNotFoundException;
import com.kota.model.Ingredient;
import com.kota.model.MenuItem;
import com.kota.model.Recipe;
import com.kota.repository.IngredientRepository;
import com.kota.repository.MenuItemRepository;
import com.kota.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    public List<MenuItemResponse> getAllAvailableItems() {
        return menuItemRepository.findByAvailableTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getAllItems() {
        return menuItemRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MenuItem getItemById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }

    @Transactional
    public MenuItemResponse createItem(CreateMenuItemRequest request, String performedBy) {
        MenuItem item = new MenuItem();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setPreparationMinutes(request.getPreparationMinutes());
        item.setImageUrl(request.getImageUrl());
        item.setAvailable(true);
        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse updateItem(Long id, CreateMenuItemRequest request, String performedBy) {
        MenuItem item = getItemById(id);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setPreparationMinutes(request.getPreparationMinutes());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id, String performedBy) {
        MenuItem item = getItemById(id);
        item.setAvailable(false);
        menuItemRepository.save(item);
    }

    @Transactional
    public void updateRecipe(Long menuItemId, List<RecipeRequest> recipeRequests, String performedBy) {
        MenuItem item = getItemById(menuItemId);
        recipeRepository.deleteByMenuItemId(menuItemId);
        for (RecipeRequest rr : recipeRequests) {
            Ingredient ingredient = ingredientRepository.findById(rr.getIngredientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient", rr.getIngredientId()));
            Recipe recipe = new Recipe();
            recipe.setMenuItem(item);
            recipe.setIngredient(ingredient);
            recipe.setQuantity(rr.getQuantity());
            recipeRepository.save(recipe);
        }
    }

    public MenuItemResponse toResponse(MenuItem item) {
        MenuItemResponse r = new MenuItemResponse();
        r.setId(item.getId());
        r.setName(item.getName());
        r.setDescription(item.getDescription());
        r.setPrice(item.getPrice());
        r.setAvailable(item.isAvailable());
        r.setPreparationMinutes(item.getPreparationMinutes());
        r.setImageUrl(item.getImageUrl());
        return r;
    }
}
