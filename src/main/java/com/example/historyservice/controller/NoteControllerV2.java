package com.example.historyservice.controller;

import com.example.historyservice.dto.NoteDto;
import com.example.historyservice.dto.NoteFullPlanDto;
import com.example.historyservice.dto.NoteFullRecipeDto;
import com.example.historyservice.service.V2.NoteServiceV2;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/notes")
public class NoteControllerV2 {

    private final NoteServiceV2 noteService;

    @PostMapping()
    public NoteDto createNote(@Valid @RequestBody NoteDto noteDto,
                              @RequestParam int userId) {
        return noteService.createNoteForUser(noteDto, userId);
    }

    @GetMapping("/recipes")
    public List<NoteFullRecipeDto> getUserHistoryForRecipes(@RequestParam int userId) {
        return noteService.findFullHistoryByUserIdForRecipe(userId);
    }

    @GetMapping("/plans")
    public List<NoteFullPlanDto> getUserHistoryForPlans(@RequestParam int userId) {
        return noteService.findFullHistoryByUserIdForPlans(userId);
    }


    @DeleteMapping("/{id}")
    public void deleteNote(@PathVariable int id,
                           @RequestParam int userId) {
        noteService.deleteNote(id, userId);
    }
}
