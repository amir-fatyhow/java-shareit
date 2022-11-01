package ru.practicum.shareit.request.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.item.dto.ItemForRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RequestWithResponseDto {

    private long id;

    private String description;

    private LocalDateTime created;

    private List<ItemForRequestDto> items;
}
