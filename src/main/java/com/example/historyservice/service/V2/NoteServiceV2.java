package com.example.historyservice.service.V2;

import com.example.historyservice.dto.NoteDto;
import com.example.historyservice.dto.NoteFullPlanDto;
import com.example.historyservice.dto.NoteFullRecipeDto;

import java.util.List;

public interface NoteServiceV2 {

    NoteDto createNoteForUser(NoteDto noteDto, int userId);
    List<NoteFullRecipeDto> findFullHistoryByUserIdForRecipe(int userId);

    List<NoteFullPlanDto> findFullHistoryByUserIdForPlans(int userId);

    void deleteNote(int id, int userId);
}
