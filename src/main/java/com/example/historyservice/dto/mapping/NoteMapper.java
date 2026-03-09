package com.example.historyservice.dto.mapping;

import com.example.historyservice.dto.NoteDto;
import com.example.historyservice.entity.Note;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NoteMapper {
    Note toEntity(NoteDto noteDto);
    NoteDto toDto(Note note);

    @Mapping(target = "id",ignore = true)
    void updateFromDto(NoteDto noteDto, @MappingTarget Note note);
}
