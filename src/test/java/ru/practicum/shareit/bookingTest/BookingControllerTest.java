package ru.practicum.shareit.bookingTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    @Test
    void saveTest() throws Exception {
        // Assign
        Booking booking = booking();

        when(bookingService.save(any(BookingDto.class), anyLong()))
                .thenReturn(BookingMapper.toBookingResponseDto(booking));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", booking.getBooker().getId())
                        .content(mapper.writeValueAsString(BookingMapper.toBookingDto(booking)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))

                        // Assert
                        .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                        .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void updateTest() throws Exception {
        // Assign
        Booking booking = booking();
        booking.setStatus(BookingStatus.APPROVED);
        Long ownerId = 1L;

        when(bookingService.update(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(BookingMapper.toBookingResponseDto(booking));
        // Act
        mvc.perform(patch("/bookings/{bookingId}", booking.getId())
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", ownerId)
                        .content(mapper.writeValueAsString(BookingMapper.toBookingResponseDto(booking)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))

                        // Assert
                        .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                        .andExpect(jsonPath("$.status", is(booking.getStatus().toString()), BookingStatus.class))
                        .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                        .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class));
    }

    @Test
    void findByIdTest() throws Exception {
        // Assign
        Booking booking = booking();
        Long userId = 1L;

        when(bookingService.findById(anyLong(), anyLong()))
                .thenReturn(BookingMapper.toBookingResponseDto(booking));
        // Act
        mvc.perform(get("/bookings/{bookingId}", booking.getId())
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(BookingMapper.toBookingResponseDto(booking)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))

                        // Assert
                        .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                        .andExpect(jsonPath("$.status", is(booking.getStatus().toString()), BookingStatus.class))
                        .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                        .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class));
    }

    @Test
    void findAllTest() throws Exception {
        // Assign
        Booking booking1 = booking();
        Booking booking2 = booking();
        booking2.setId(2L);
        List<BookingResponseDto> bookings = List.of(BookingMapper.toBookingResponseDto(booking1),
                BookingMapper.toBookingResponseDto(booking2));

        Long bookerId = 1L;

        when(bookingService.findByState(anyString(), anyLong(), anyInt(), anyInt()))
                .thenReturn(bookings);
        // Act
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", bookerId))
                // Assert
                .andExpect(jsonPath("$[0].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(booking1.getStatus().toString()), BookingStatus.class))
                .andExpect(jsonPath("$[0].item.id", is(booking1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(booking2.getId()), Long.class))
                .andExpect(jsonPath("$[1].status", is(booking2.getStatus().toString()), BookingStatus.class))
                .andExpect(jsonPath("$[1].item.id", is(booking2.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].booker.id", is(booking2.getBooker().getId()), Long.class));
    }

    @Test
    void findAllByOwnerTest() throws Exception {
        // Assign
        Booking booking1 = booking();
        Booking booking2 = booking();
        booking2.setId(2L);
        List<BookingResponseDto> bookings = List.of(BookingMapper.toBookingResponseDto(booking1), BookingMapper.toBookingResponseDto(booking2));

        Long ownerId = 1L;

        when(bookingService.findOwnerItems(anyString(), anyLong(), anyInt(), anyInt()))
                .thenReturn(bookings);
        // Act
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId))
                // Assert
                .andExpect(jsonPath("$[0].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(booking1.getStatus().toString()), BookingStatus.class))
                .andExpect(jsonPath("$[0].item.id", is(booking1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(booking2.getId()), Long.class))
                .andExpect(jsonPath("$[1].status", is(booking2.getStatus().toString()), BookingStatus.class))
                .andExpect(jsonPath("$[1].item.id", is(booking2.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].booker.id", is(booking2.getBooker().getId()), Long.class));
    }

    private Booking booking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusSeconds(10));
        booking.setEnd(LocalDateTime.now().plusSeconds(20));
        booking.setItem(item());
        booking.setBooker(user());
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setName("User");
        user.setEmail("user@email.ru");
        return user;
    }

    private Item item() {
        Item item = new Item();
        item.setId(1L);
        item.setName("item");
        item.setDescription("description");
        item.setOwner(user());
        item.setRequest(null);
        return item;
    }

}
