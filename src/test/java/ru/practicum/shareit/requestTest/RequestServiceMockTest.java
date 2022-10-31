package ru.practicum.shareit.requestTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemStorage;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.request.repositories.RequestStorage;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositories.UserStorage;
import ru.practicum.shareit.user.services.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestServiceMockTest {

    @Mock
    private RequestStorage requestStorage;

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserService userService;

    @Mock
    private UserStorage userStorage;

    private RequestServiceImpl requestService;

    @BeforeEach
    void init() {
        requestService = new RequestServiceImpl(requestStorage, itemStorage, userService);
    }

    @Test
    public void createRequestTest() throws Exception {
        // Assign
        User requestor = getTestUser();
        ItemRequest itemRequest = getTestItemRequest(requestor);
        ItemRequestDto itemRequestDto = RequestMapper.toItemRequestDto(itemRequest);


        Mockito.when(userService.checkUser(anyLong())).thenReturn(requestor);
        Mockito.when(requestStorage.save(any(ItemRequest.class)))
                .thenReturn(itemRequest);
        // Act
        ItemRequestDto actualItemRequestDto = requestService
                .createRequest(itemRequestDto, requestor.getId());
        // Assert
        assertNotNull(actualItemRequestDto);
        assertEquals(itemRequestDto.getDescription(), actualItemRequestDto.getDescription());
    }

    @Test
    public void createRequestErrorTest() {
        // Assign
        User requestor = getTestUser();
        ItemRequest itemRequest = getTestItemRequest(requestor);
        ItemRequestDto itemRequestDto = RequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setDescription(null);

        // Act
        BadRequestException thrown = Assertions.assertThrows(BadRequestException.class, () -> {
            ItemRequestDto actualItemRequestDto = requestService
                    .createRequest(itemRequestDto, requestor.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "ItemRequest Description is empty");
    }

    @Test
    public void checkItemRequestTest() throws Exception {
        // Assign
        User requestor = getTestUser();
        ItemRequest itemRequest = getTestItemRequest(requestor);

        when(requestStorage.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));

        // Act
        ItemRequest actualItemRequest = requestService.checkItemRequest(itemRequest.getId());

        // Assert
        Assertions.assertNotNull(actualItemRequest);
        Assertions.assertEquals(itemRequest.getId(), actualItemRequest.getId());
        Assertions.assertEquals(itemRequest.getRequestor().getId(), actualItemRequest.getRequestor().getId());
        Assertions.assertEquals(itemRequest.getDescription(), actualItemRequest.getDescription());
    }

    @Test
    public void checkItemRequestErrorTest() {
        // Assign
        User requestor = getTestUser();
        ItemRequest itemRequest = getTestItemRequest(requestor);

        when(requestStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        NotFoundException thrown = Assertions.assertThrows(NotFoundException.class, () -> {
            ItemRequest actualItemRequest = requestService.checkItemRequest(itemRequest.getId());
        });

        // Assert
        assertEquals(thrown.getMessage(), "ItemRequest by ID: 1  - not found");
    }

    @Test
    public void getAllResponsesForAllRequestsTest() throws Exception {
        // Assign
        User requestor = getTestUser();
        ItemRequest itemRequest1 = getTestItemRequest(requestor);
        ItemRequest itemRequest2 = getTestItemRequest(requestor);
        List<ItemRequest> itemRequests = List.of(itemRequest1, itemRequest2);
        RequestWithResponseDto requestWithResponseDto1 = RequestMapper.toRequestWithResponseDto(itemRequest1, null);
        RequestWithResponseDto requestWithResponseDto2 = RequestMapper.toRequestWithResponseDto(itemRequest2, null);
        List<RequestWithResponseDto> responses = List.of(requestWithResponseDto1, requestWithResponseDto2);

        when(userService.checkUser(anyLong()))
                .thenReturn(requestor);
        when(requestStorage.findAllByRequestor_IdOrderByCreatedDesc(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(itemRequests));

        // Act
        List<RequestWithResponseDto> actualResponses = requestService.getAllResponsesForAllRequests(requestor.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualResponses);
        Assertions.assertEquals(responses.size(), actualResponses.size());
    }

    @Test
    public void getAllRequestsOtherUsersTest() throws Exception {
        // Assign
        User requestor = getTestUser();
        User otherUser = getTestUser();
        otherUser.setId(2L);
        otherUser.setEmail("2@test.ru");
        ItemRequest itemRequest1 = getTestItemRequest(requestor);
        ItemRequest itemRequest2 = getTestItemRequest(otherUser);
        List<ItemRequest> itemRequests = List.of(itemRequest2);
        RequestWithResponseDto requestWithResponseDto1 = RequestMapper.toRequestWithResponseDto(itemRequest1, null);
        RequestWithResponseDto requestWithResponseDto2 = RequestMapper.toRequestWithResponseDto(itemRequest2, null);
        List<RequestWithResponseDto> responses = List.of(requestWithResponseDto2);

        when(userService.checkUser(anyLong()))
                .thenReturn(requestor);
        when(requestStorage.findAllByRequestorNot(any(User.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(itemRequests));

        // Act
        List<RequestWithResponseDto> actualResponses = requestService.getAllRequestsOtherUsers(requestor.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualResponses);
        Assertions.assertEquals(responses.size(), actualResponses.size());
    }

    @Test
    public void getRequestByIdTest() {
        // Assert
        User requestor = getTestUser();
        ItemRequest itemRequest = getTestItemRequest(requestor);
        Item item1 = getTestItem(itemRequest);
        Item item2 = getTestItem(itemRequest);
        item2.setId(2L);
        item2.setName("Test Name2");
        List<Item> items = List.of(item1, item2);

        when(userService.checkUser(anyLong()))
                .thenReturn(requestor);
        when(requestStorage.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        when(itemStorage.findAllByRequest_IdOrderByRequestIdDesc(anyLong()))
                .thenReturn(items);

        // Act
        RequestWithResponseDto actualRequest = requestService.getRequestById(requestor.getId(), itemRequest.getId());

        // Assert
        Assertions.assertNotNull(actualRequest);
        Assertions.assertEquals(items.size(), actualRequest.getItems().size());
    }

    private ItemRequest getTestItemRequest(User requestor) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Test Description Request");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    private User getTestUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User Name");
        user.setEmail("testUser@email.ru");
        return user;
    }

    private Item getTestItem(ItemRequest itemRequest) {
        Item item = new Item();
        item.setId(1L);
        item.setName("TestName");
        item.setDescription("Description");
        item.setAvailable(Boolean.TRUE);
        item.setOwner(getTestUser());
        item.setRequest(itemRequest);
        return item;
    }
}
