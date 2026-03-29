package com.example.historyservice.dto;

import lombok.Data;

@Data
public class NoteFullPlanDto {
    private int id;
    private Integer userId;
    private String recipeName;
    private Integer mark;
    private String comment;

    private String  type;
    private String location;
    private int numberOfPeople;
}
