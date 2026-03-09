package com.example.historyservice.service;

import com.example.historyservice.dto.NoteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoteService {
    Page<NoteDto> findAllRecipes(Pageable pageable);
    NoteDto findNoteById(int id);
    NoteDto createNote(NoteDto noteDto);
    NoteDto updateNoteById(int id, NoteDto noteDto);
    void deleteNoteById(int id);
}
