package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> save(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                       @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Post request {}, userId={}", itemRequestDto, userId);
        return requestClient.save(itemRequestDto, userId);
    }

    @GetMapping()
    public ResponseEntity<Object> findAllResponsesForAllRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                                 @PositiveOrZero @NotNull @RequestParam(required = false, defaultValue = "0") Integer from,
                                                                 @Positive @NotNull @RequestParam(required = false, defaultValue = "100") Integer size) {
        log.info("Get all responses by userId={}, from={}, size={}", userId, from, size);
        return requestClient.findAllResponsesForAllRequests(userId, from, size);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllByUserId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                  @PositiveOrZero @NotNull @RequestParam(required = false, defaultValue = "0") Integer from,
                                                  @Positive @NotNull @RequestParam(required = false, defaultValue = "100") Integer size) {
        log.info("Get all requests by userId={}, from={}, size={}", userId, from, size);
        return requestClient.findAllByUserId(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @PathVariable long requestId) {
        log.info("Get request requestId={}, userId={}", userId, requestId);
        return requestClient.findById(userId, requestId);
    }
}
