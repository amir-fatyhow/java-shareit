package ru.practicum.shareit.requestTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class RequestControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestService requestService;


    @Autowired
    private MockMvc mvc;


    @Test
    void saveTest() throws Exception {
        // Assign
        User requestor = user();
        ItemRequest itemRequest = itemRequest(requestor);
        ItemRequestDto itemRequestDto = RequestMapper.toItemRequestDto(itemRequest);

        when(requestService.save(any(ItemRequestDto.class), anyLong()))
                .thenReturn(itemRequestDto);

        // Act
        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", requestor.getId())
                        .content(objectMapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                        // Assert
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                        .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())));
    }

    @Test
    void findAllResponsesForAllRequestsTest() throws Exception {
        // Assign
        User requestor = user();
        ItemRequest itemRequest1 = itemRequest(requestor);
        ItemRequest itemRequest2 = itemRequest(requestor);
        RequestWithResponseDto requestWithResponseDto1 = RequestMapper.toRequestWithResponseDto(itemRequest1, null);
        RequestWithResponseDto requestWithResponseDto2 = RequestMapper.toRequestWithResponseDto(itemRequest2, null);
        List<RequestWithResponseDto> responses = List.of(requestWithResponseDto1, requestWithResponseDto2);

        when(requestService.findAllResponsesForAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(responses);

        // Act
        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", requestor.getId()))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(requestWithResponseDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestWithResponseDto1.getDescription())))
                .andExpect(jsonPath("$[0].items", is(requestWithResponseDto1.getItems())))
                .andExpect(jsonPath("$[1].id", is(requestWithResponseDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(requestWithResponseDto2.getDescription())))
                .andExpect(jsonPath("$[1].items", is(requestWithResponseDto2.getItems())));
    }

    @Test
    void findAllRequestsByUserIdTest() throws Exception {
        // Assign
        User requestor = user();
        ItemRequest itemRequest1 = itemRequest(requestor);
        ItemRequest itemRequest2 = itemRequest(requestor);
        RequestWithResponseDto requestWithResponseDto1 = RequestMapper.toRequestWithResponseDto(itemRequest1, null);
        RequestWithResponseDto requestWithResponseDto2 = RequestMapper.toRequestWithResponseDto(itemRequest2, null);
        List<RequestWithResponseDto> responses = List.of(requestWithResponseDto1, requestWithResponseDto2);

        when(requestService.findAllByUserId(anyLong(), anyInt(), anyInt()))
                .thenReturn(responses);

        // Act
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", requestor.getId()))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(requestWithResponseDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestWithResponseDto1.getDescription())))
                .andExpect(jsonPath("$[0].items", is(requestWithResponseDto1.getItems())))
                .andExpect(jsonPath("$[1].id", is(requestWithResponseDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(requestWithResponseDto2.getDescription())))
                .andExpect(jsonPath("$[1].items", is(requestWithResponseDto2.getItems())));
    }

    @Test
    void findByIdTest() throws Exception {
        // Assign
        User requestor = user();
        ItemRequest itemRequest = itemRequest(requestor);
        RequestWithResponseDto requestWithResponseDto = RequestMapper.toRequestWithResponseDto(itemRequest, null);

        when(requestService.findById(anyLong(), anyLong()))
                .thenReturn(requestWithResponseDto);
        // Act
        mvc.perform(get("/requests/{requestId}", itemRequest.getId())
                        .header("X-Sharer-User-Id", requestor.getId()))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestWithResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestWithResponseDto.getDescription())))
                .andExpect(jsonPath("$.items", is(requestWithResponseDto.getItems())));
    }

    private ItemRequest itemRequest(User requestor) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Test Description Request");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User Name");
        user.setEmail("testUser@email.ru");
        return user;
    }

}
