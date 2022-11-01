package ru.practicum.shareit.request.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ItemRequestDto {

    private long id;

    @NotBlank
    @NotNull
    private String description;

    private LocalDateTime created;
}
