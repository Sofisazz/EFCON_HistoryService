package com.example.historyservice.controller;

import com.example.historyservice.dto.NoteDto;
import com.example.historyservice.service.V1.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/notes")
public class NoteControllerV1 {

    private final NoteService noteService;

    @GetMapping()
    public Page<NoteDto> getAllNotes(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                     @RequestParam(value = "limit", defaultValue = "5") @Min(1) @Max(100) Integer limit) {
        return noteService.findAllRecipes(PageRequest.of(offset, limit));
    }

    @GetMapping("/{id}")
    public NoteDto getNote(@PathVariable int id){
        return noteService.findNoteById(id);
    }

    @PostMapping()
    public NoteDto createNote(@Valid @RequestBody NoteDto noteDto) {
        return noteService.createNote(noteDto);
    }

    @PutMapping("/{id}")
    public NoteDto changeNote(@PathVariable int id, @Valid @RequestBody NoteDto noteDto) {
        return noteService.updateNoteById(id, noteDto);
    }

    @DeleteMapping("/{id}")
    public void DeleteNote(@PathVariable int id) {
        noteService.deleteNoteById(id);
    }
}
