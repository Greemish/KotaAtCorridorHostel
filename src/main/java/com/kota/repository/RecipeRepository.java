package com.kota.repository;

import com.kota.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByMenuItemId(Long menuItemId);

    void deleteByMenuItemId(Long menuItemId);
}
