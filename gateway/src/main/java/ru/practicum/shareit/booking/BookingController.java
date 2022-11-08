package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.UnsupportedStatus;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@GetMapping
	public ResponseEntity<Object> findByState(@RequestHeader("X-Sharer-User-Id") long userId,
											  @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
											  @PositiveOrZero @NotNull @RequestParam(name = "from", defaultValue = "0") Integer from,
											  @Positive @NotNull @RequestParam(name = "size", defaultValue = "100") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new UnsupportedStatus("Unknown state: " + stateParam));

		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.findByState(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> findOwnerItems(@RequestParam(name = "state", defaultValue = "ALL") String stateParam,
												 @RequestHeader("X-Sharer-User-Id") long userId,
												 @PositiveOrZero @NotNull @RequestParam(required = false, defaultValue = "0") Integer from,
												 @Positive @NotNull @RequestParam(required = false, defaultValue = "100") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new UnsupportedStatus("Unknown state: " + stateParam));

		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.findOwnerItems(userId, state, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> save(@RequestHeader("X-Sharer-User-Id") long userId,
									   @RequestBody @Valid BookItemRequestDto requestDto) {
		validateDates(requestDto.getStart(), requestDto.getEnd());
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.save(userId, requestDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> findById(@RequestHeader("X-Sharer-User-Id") long userId,
										   @PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.findById(userId, bookingId);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> update(@PathVariable long bookingId,
										 @RequestHeader("X-Sharer-User-Id") long userId,
										 @RequestParam boolean approved) {
		log.info("Patch booking by owner decision bookingId={}, userId={}, approved={}", bookingId, userId, approved);
		return bookingClient.update(bookingId, userId, approved);
	}

	private void validateDates(LocalDateTime start, LocalDateTime end) {
		LocalDateTime current = LocalDateTime.now();
		if ((start.isEqual(current) || start.isAfter(current)) && end.isAfter(start)) {
			log.info("Time is OK");
		} else {
			throw new ValidationException("Неверное значение даты бронирования.");
		}
	}
}
