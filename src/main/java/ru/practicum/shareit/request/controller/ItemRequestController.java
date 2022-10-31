package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.service.RequestService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
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
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "100") Integer size) {
        return requestService.getAllResponsesForAllRequests(userId, from, size);
    }

    @GetMapping("/all")
    public List<RequestWithResponseDto> getAllRequestsByUserId(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PositiveOrZero @RequestParam(required = false, defaultValue = "0") Integer from,
            @Positive @RequestParam(required = false, defaultValue = "100") Integer size) {
        return requestService.getAllRequestsOtherUsers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestWithResponseDto getRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable long requestId) {
        return requestService.getRequestById(userId, requestId);
    }
}
