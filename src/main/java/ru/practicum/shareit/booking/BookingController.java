package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingDto findById(@PathVariable long bookingId,
                               @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping("/owner")
    public List<BookingDto> findOwnerItems(@RequestParam(defaultValue = "ALL") String state,
                               @RequestHeader("X-Sharer-User-Id") long ownerId) {
        return bookingService.findOwnerItems(ownerId, state);
    }

    @GetMapping
    public List<BookingDto> findByState(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.findByState(userId, state);
    }

    @PostMapping
    public BookingDto save(@RequestBody @Valid BookingDto bookingDto, @RequestHeader("X-Sharer-User-Id") long bookerId) {
        return bookingService.save(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto update(@PathVariable long bookingId,
                          @RequestHeader("X-Sharer-User-Id") long ownerId,
                          @RequestParam boolean approved) throws JsonMappingException {
        return bookingService.update(bookingId, ownerId, approved);
    }
}
