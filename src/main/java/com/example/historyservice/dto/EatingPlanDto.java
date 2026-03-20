package com.example.historyservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EatingPlanDto {

    private int id;
    private int numberOfPeople;
    private LocalDate date;
    private String  type;
    private String status;
    private Integer recipeId;
}
