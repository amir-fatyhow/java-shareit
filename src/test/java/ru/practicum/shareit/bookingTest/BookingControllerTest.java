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
import java.time.format.DateTimeFormatter;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    private final BookingDto bookingDto1 = new BookingDto(1L,
            1L,
            1L,
            LocalDateTime.now().plusSeconds(10),
            LocalDateTime.now().plusSeconds(20),
            BookingStatus.WAITING);

    @Test
    public void createBookingTest() throws Exception {
        // Assign
        Booking booking = getTestBooking();

        when(bookingService.createBooking(any(BookingDto.class), anyLong()))
                .thenReturn(BookingMapper.toBookingResponseDto(booking));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", booking.getBooker().getId())
                        .content(mapper.writeValueAsString(BookingMapper.toBookingDto(booking)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
//                .andExpect(jsonPath("$.start", is(booking.getStart().format(dateTimeFormatter))))
//                .andExpect(jsonPath("$.end", is(booking.getEnd().format(dateTimeFormatter))))
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    public void ownerDecisionTest() throws Exception {
        // Assign
        Booking booking = getTestBooking();
        booking.setStatus(BookingStatus.APPROVED);
        Long ownerId = 1L;

        when(bookingService.ownerDecision(anyLong(), anyLong(), anyBoolean()))
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString()), BookingStatus.class))
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class));
    }

    @Test
    public void getBookingTest() throws Exception {
        // Assign
        Booking booking = getTestBooking();
        Long userId = 1L;

        when(bookingService.getBooking(anyLong(), anyLong()))
                .thenReturn(BookingMapper.toBookingResponseDto(booking));
        // Act
        mvc.perform(get("/bookings/{bookingId}", booking.getId())
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(BookingMapper.toBookingResponseDto(booking)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString()), BookingStatus.class))
                .andExpect(jsonPath("$.item.id", is(booking.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(booking.getBooker().getId()), Long.class));
    }

    @Test
    public void getAllBookingsByBookerTest() throws Exception {
        // Assign
        Booking booking1 = getTestBooking();
        Booking booking2 = getTestBooking();
        booking2.setId(2L);
        List<BookingResponseDto> bookings = List.of(BookingMapper.toBookingResponseDto(booking1), BookingMapper.toBookingResponseDto(booking2));

        Long bookerId = 1L;

        when(bookingService.getAllBookingsByBooker(anyString(), anyLong(), anyInt(), anyInt()))
                .thenReturn(bookings);
        // Act
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", bookerId))
                // Assert
                .andExpect(status().isOk())
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
    public void getAllBookingsByOwnerTest() throws Exception {
        // Assign
        Booking booking1 = getTestBooking();
        Booking booking2 = getTestBooking();
        booking2.setId(2L);
        List<BookingResponseDto> bookings = List.of(BookingMapper.toBookingResponseDto(booking1), BookingMapper.toBookingResponseDto(booking2));

        Long ownerId = 1L;

        when(bookingService.getAllBookingsByOwner(anyString(), anyLong(), anyInt(), anyInt()))
                .thenReturn(bookings);
        // Act
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(booking1.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(booking1.getStatus().toString()), BookingStatus.class))
                .andExpect(jsonPath("$[0].item.id", is(booking1.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(booking1.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].id", is(booking2.getId()), Long.class))
                .andExpect(jsonPath("$[1].status", is(booking2.getStatus().toString()), BookingStatus.class))
                .andExpect(jsonPath("$[1].item.id", is(booking2.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].booker.id", is(booking2.getBooker().getId()), Long.class));
    }

    private Booking getTestBooking() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusSeconds(10));
        booking.setEnd(LocalDateTime.now().plusSeconds(20));
        booking.setItem(getTestItem());
        booking.setBooker(getTestUser());
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    private User getTestUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User Name 1");
        user.setEmail("testUser1@email.ru");
        return user;
    }

    private Item getTestItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item Name 1");
        item.setDescription("Test Item Description 1");
        item.setOwner(getTestUser());
        item.setRequest(null);
        return item;
    }

}
