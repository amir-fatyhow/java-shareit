package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final RequestService requestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestBody ItemRequestDto itemRequestDto,
                                        @RequestHeader("X-Sharer-User-Id") long userId) {
        return requestService.createRequest(itemRequestDto, userId);
    }

    @GetMapping()
    public List<RequestWithResponseDto> getAllResponsesForAllRequests(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
            @RequestParam(required = false, defaultValue = "100") @Min(1) Integer size) {
        return requestService.getAllResponsesForAllRequests(userId, from, size);
    }

    @GetMapping("/all")
    public List<RequestWithResponseDto> getAllRequestsByUserId(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer from,
            @RequestParam(required = false, defaultValue = "100") @Min(1) Integer size) {
        return requestService.getAllRequestsOtherUsers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestWithResponseDto getRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable long requestId) {
        return requestService.getRequestById(userId, requestId);
    }
}
