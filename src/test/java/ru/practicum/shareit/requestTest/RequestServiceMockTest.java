package ru.practicum.shareit.requestTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceMockTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    private RequestServiceImpl requestService;

    private static final String USER_NOT_FOUND = "Пользователя с указанным Id не существует.";
    private static final String REQUEST_NOT_FOUND = "Запрос с указанным Id не существует.";

    @BeforeEach
    void setUp() {
        requestService = new RequestServiceImpl(requestRepository, itemRepository, userRepository);
    }

    @Test
    void saveTest() {
        // Assign
        User requestor = user();
        ItemRequest itemRequest = itemRequest(requestor);
        ItemRequestDto itemRequestDto = RequestMapper.toItemRequestDto(itemRequest);


        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(requestor));

        when(requestRepository.save(any(ItemRequest.class)))
                .thenReturn(itemRequest);
        // Act
        ItemRequestDto actualItemRequestDto = requestService
                .save(itemRequestDto, requestor.getId());
        // Assert
        assertNotNull(actualItemRequestDto);
        assertEquals(itemRequestDto.getDescription(), actualItemRequestDto.getDescription());
    }

    @Test
    void saveUserNotFoundTest() {
        // Assign
        User requestor = user();
        ItemRequest itemRequest = itemRequest(requestor);
        ItemRequestDto itemRequestDto = RequestMapper.toItemRequestDto(itemRequest);


        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () ->
                requestService.save(itemRequestDto, requestor.getId()));

        // Assert
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

   @Test
   void findAllResponsesForAllRequestsTest() {
        // Assign
        User requestor = user();
        ItemRequest itemRequest1 = itemRequest(requestor);
        ItemRequest itemRequest2 = itemRequest(requestor);
        List<ItemRequest> itemRequests = List.of(itemRequest1, itemRequest2);
        RequestWithResponseDto requestWithResponseDto1 = RequestMapper.toRequestWithResponseDto(itemRequest1, null);
        RequestWithResponseDto requestWithResponseDto2 = RequestMapper.toRequestWithResponseDto(itemRequest2, null);
        List<RequestWithResponseDto> responses = List.of(requestWithResponseDto1, requestWithResponseDto2);

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);

        when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(anyLong(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(itemRequests));

        // Act
        List<RequestWithResponseDto> actualResponses = requestService.findAllResponsesForAllRequests(requestor.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualResponses);
        Assertions.assertEquals(responses.size(), actualResponses.size());
    }

    @Test
    void findAllResponsesForAllRequestsUserNotFoundTest() {
        // Assign
        User requestor = user();

        when(userRepository.existsById(anyLong()))
                .thenReturn(false);

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () ->
                requestService.findAllResponsesForAllRequests(requestor.getId(), 0, 100));

        // Assert
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void findAllRequestsOtherUsersTest() {
        // Assign
        User requestor = user();
        User otherUser = user();
        otherUser.setId(2L);
        otherUser.setEmail("2@test.ru");
        ItemRequest itemRequest1 = itemRequest(requestor);
        ItemRequest itemRequest2 = itemRequest(otherUser);
        List<ItemRequest> itemRequests = List.of(itemRequest2);
        RequestWithResponseDto requestWithResponseDto1 = RequestMapper.toRequestWithResponseDto(itemRequest1, null);
        RequestWithResponseDto requestWithResponseDto2 = RequestMapper.toRequestWithResponseDto(itemRequest2, null);
        List<RequestWithResponseDto> responses = List.of(requestWithResponseDto2);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(requestor));

        when(requestRepository.findAllByRequestorNot(any(User.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(itemRequests));

        // Act
        List<RequestWithResponseDto> actualResponses = requestService.findAllByUserId(requestor.getId(), 0, 10);

        // Assert
        Assertions.assertNotNull(actualResponses);
        Assertions.assertEquals(responses.size(), actualResponses.size());
    }

    @Test
    void getByIdTest() {
        // Assert
        User requestor = user();
        ItemRequest itemRequest = itemRequest(requestor);
        Item item1 = item(itemRequest);
        Item item2 = item(itemRequest);
        item2.setId(2L);
        item2.setName("Test Name2");
        List<Item> items = List.of(item1, item2);

       when(userRepository.existsById(anyLong()))
               .thenReturn(true);

        when(requestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));

        when(itemRepository.findAllByRequestIdOrderByRequestIdDesc(anyLong()))
                .thenReturn(items);

        // Act
        RequestWithResponseDto actualRequest = requestService.findById(requestor.getId(), itemRequest.getId());

        // Assert
        Assertions.assertNotNull(actualRequest);
        Assertions.assertEquals(items.size(), actualRequest.getItems().size());
    }

    @Test
    void findByIdUserNotFoundTest() {
        // Assert
        User requestor = user();
        ItemRequest itemRequest = itemRequest(requestor);

        when(userRepository.existsById(anyLong()))
                .thenReturn(false);

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () ->
                requestService.findById(requestor.getId(), itemRequest.getId()));

        // Assert
        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void findByIdRequestNotFoundTest() {
        // Assert
        User requestor = user();
        ItemRequest itemRequest = itemRequest(requestor);

        when(userRepository.existsById(anyLong()))
                .thenReturn(true);

        when(requestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () ->
                requestService.findById(requestor.getId(), itemRequest.getId()));

        // Assert
        assertEquals(REQUEST_NOT_FOUND, exception.getMessage());
    }

    @Test
    void findAllByUserIdUserNotFoundTest() {
        // Assert
        User requestor = user();

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        ShareItNotFoundException exception = assertThrows(ShareItNotFoundException.class, () ->
                requestService.findAllByUserId(requestor.getId(), 0, 100));

        // Assert
        assertEquals(USER_NOT_FOUND, exception.getMessage());
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

    private Item item(ItemRequest itemRequest) {
        Item item = new Item();
        item.setId(1L);
        item.setName("TestName");
        item.setDescription("Description");
        item.setAvailable(Boolean.TRUE);
        item.setOwner(user());
        item.setRequest(itemRequest);
        return item;
    }
}
