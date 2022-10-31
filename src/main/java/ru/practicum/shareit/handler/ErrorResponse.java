package ru.practicum.shareit.handler;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ErrorResponse {

    private final String error;
}
