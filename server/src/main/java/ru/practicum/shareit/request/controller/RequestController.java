package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.service.RequestService;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    public ItemRequestDto save(@RequestBody ItemRequestDto itemRequestDto,
                               @RequestHeader("X-Sharer-User-Id") long userId) {
        return requestService.save(itemRequestDto, userId);
    }

    @GetMapping
    public List<RequestWithResponseDto> findAllResponsesForAllRequests(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "100") Integer size) {
        return requestService.findAllResponsesForAllRequests(userId, from, size);
    }

    @GetMapping("/all")
    public List<RequestWithResponseDto> findAllByUserId(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "100") Integer size) {
        return requestService.findAllByUserId(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestWithResponseDto findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @PathVariable long requestId) {
        return requestService.findById(userId, requestId);
    }
}
