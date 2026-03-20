package com.example.historyservice.feignclient;

import com.example.historyservice.dto.EatingPlanDto;
import com.example.historyservice.dto.RecipeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "menu-service")
public interface MenuClient {

    @GetMapping("/api/v2/recipes/{id}")
    RecipeDto getRecipeById(@PathVariable("id") int recipeId,
                            @RequestParam int userId);

    @GetMapping("/api/v2/plans/{id}")
    EatingPlanDto getEatingPlan(@PathVariable("id") int planId,
                                @RequestParam int userId);

    @GetMapping("/api/v2/plans/{id}/recipes/{recipeId}/exist")
    boolean existEatingPlanWithRecipe(@PathVariable int id,
                                      @PathVariable int recipeId,
                                      @RequestParam int userId);
}
