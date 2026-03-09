package com.example.historyservice.dto;

import lombok.Data;

@Data
public class NoteDto {
    private int id;
    private Integer userId;
    private double mark;
    private String comment;
}
