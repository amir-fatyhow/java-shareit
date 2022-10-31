package ru.practicum.shareit.request.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ItemRequestDto {

    private long id;

    private String description;

    private LocalDateTime created;
}
