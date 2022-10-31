package ru.practicum.shareit.itemTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.controllers.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.services.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {

    @Autowired
    private ObjectMapper objectMapper; // Встроенный в Spring маппер для преобразования объекта в строку формата JSON

    @MockBean
    private ItemService itemService; // Мокаем бин

    @MockBean
    private BookingService bookingService; // Мокаем бин

    @Autowired
    private MockMvc mvc;

    @Test
    public void createItemTest() throws Exception {
        // Assign
        ItemDto itemDto = new ItemDto(1L, "TestName", "Description", Boolean.TRUE, 1L);

        // Так как мы тестируем только контроллеры, то сервис надо замокать
        when(itemService.createItem(any(ItemDto.class), anyLong())) // когда вызывается метод createItem() с любым классом ItemDto и с любым long...
                .thenReturn(itemDto); // ... то возвращаем данное DTO

        // Act
        mvc.perform(post("/items") // Отправляем запрос на этот URL
                        .header("X-Sharer-User-Id", 1L) // Указываем header
                        .content(objectMapper.writeValueAsString(itemDto)) // Передаём itemDto в body, при помощи маппера преобразуем dto в строку формата JSON
                        .characterEncoding(StandardCharsets.UTF_8) // Кодировка
                        .contentType(MediaType.APPLICATION_JSON) // Возвращаем json
                        .accept(MediaType.APPLICATION_JSON)) // принимаем json
                // Assert
                .andExpect(status().isCreated()) // Должен прийти ответ 201
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class)) // Проверяем поле id  в JSON
                .andExpect(jsonPath("$.name", is(itemDto.getName()))) // И тд проверяем все поля
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId()), Long.class));
    }

    @Test
    public void updateItemTest() throws Exception {
        // Assign
        ItemDto itemDto = new ItemDto(1L, "TestName", "Description", Boolean.TRUE, null);
        Item item = getTestItem1();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(Boolean.TRUE);

        when(itemService.updateItem(any(), anyLong(), anyLong())).thenReturn(itemDto);

        // Act
        mvc.perform(patch("/items/{itemId}", item.getId())
                        .header("X-Sharer-User-Id", item.getOwner().getId())
                        .content(objectMapper.writeValueAsString(ItemMapper.toItemDto(item)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(nullValue())));

    }

    @Test
    public void deleteItemTest() throws Exception {
        // Assign
        Long itemId = 1L;

        // Act
        mvc.perform(delete("/items/{itemId}", itemId))
                // Assert
                .andExpect(status().isNoContent());
    }

    @Test
    public void findItemByIdTest() throws Exception {
        // Assign
        List<CommentDto> commentsDto = List.of(CommentMapper.toCommentDto(getTestComment()));
        ItemResponseDto itemResponseDto = new ItemResponseDto(1L,
                "Test ItemResponseDto Name",
                "Test ItemResponseDto Description 1",
                true,
                null,
                null,
                commentsDto);
        Item item = getTestItem1();
        item.setName(itemResponseDto.getName());
        item.setDescription(itemResponseDto.getDescription());
        item.setAvailable(Boolean.TRUE);
        User user = getTestUser1();

        when(itemService.findItemById(anyLong(), anyLong()))
                .thenReturn(itemResponseDto);
        when(itemService.getAllCommentsByItem(anyLong()))
                .thenReturn(commentsDto);
        when(bookingService.getLastBookingByItem(anyLong()))
                .thenReturn(null);
        when(bookingService.getNextBookingByItem(anyLong()))
                .thenReturn(null);

        // Act
        mvc.perform(get("/items/{itemId}", user.getId())
                        .header("X-Sharer-User-Id", 1L))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(item.getName())))
                .andExpect(jsonPath("$.description", is(item.getDescription())))
                .andExpect(jsonPath("$.available", is(item.getAvailable())))
                .andExpect(jsonPath("$.lastBooking", is(nullValue())))
                .andExpect(jsonPath("$.nextBooking", is(nullValue())))
                .andExpect(jsonPath("$.comments[0].id", is(commentsDto.get(0).getId()), Long.class));
    }

    @Test
    public void findAllItemsByUserIdTest() throws Exception {
        // Assign
        ItemResponseDto itemResponseDto1 = ItemMapper.toItemResponseDto(getTestItem1(), null, null, null);
        ItemResponseDto itemResponseDto2 = ItemMapper.toItemResponseDto(getTestItem2(), null, null, null);
        List<ItemResponseDto> items = List.of(itemResponseDto1, itemResponseDto2);

        when(itemService.findAllItemsByUserId(anyLong(), anyInt(), anyInt()))
                .thenReturn(items);
        when(itemService.getAllCommentsByItem(anyLong()))
                .thenReturn(null);
        when(bookingService.getLastBookingByItem(anyLong()))
                .thenReturn(null);
        when(bookingService.getNextBookingByItem(anyLong()))
                .thenReturn(null);

        // Act
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemResponseDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemResponseDto1.getName())))
                .andExpect(jsonPath("$[0].description", is(itemResponseDto1.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemResponseDto1.getAvailable())))
                .andExpect(jsonPath("$[0].lastBooking", is(nullValue())))
                .andExpect(jsonPath("$[0].nextBooking", is(nullValue())))
                .andExpect(jsonPath("$[0].comments", is(nullValue())))
                .andExpect(jsonPath("$[1].id", is(itemResponseDto2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(itemResponseDto2.getName())))
                .andExpect(jsonPath("$[1].description", is(itemResponseDto2.getDescription())))
                .andExpect(jsonPath("$[1].available", is(itemResponseDto2.getAvailable())))
                .andExpect(jsonPath("$[1].lastBooking", is(nullValue())))
                .andExpect(jsonPath("$[1].nextBooking", is(nullValue())))
                .andExpect(jsonPath("$[1].comments", is(nullValue())));
    }

    @Test
    public void searchItemsByNameAndDescriptionTest() throws Exception {
        // Assign
        ItemDto itemDto1 = ItemMapper.toItemDto(getTestItem1());
        ItemDto itemDto2 = ItemMapper.toItemDto(getTestItem2());
        List<ItemDto> itemDtos = List.of(itemDto1, itemDto2);

        when(itemService.searchItemsByNameAndDescription(anyString(), anyInt(), anyInt()))
                .thenReturn(itemDtos);

        // Act
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "Description 1")
                        .param("from", "0")
                        .param("size", "10")
                )
                // Assert
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(itemDtos)));
    }

    @Test
    public void postComment() throws Exception {
        // Assign
        Item item = getTestItem1();
        CommentDto commentDto = CommentMapper.toCommentDto(getTestComment());

        when(itemService.postComment(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);

        // Act
        mvc.perform(post("/items/{itemId}/comment", commentDto.getItemId())
                        .header("X-Sharer-User-Id", item.getOwner().getId())
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
    }

    private User getTestUser1() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User Name 1");
        user.setEmail("testUser1@email.ru");
        return user;
    }

    private User getTestUser2() {
        User user = new User();
        user.setId(2L);
        user.setName("Test User Name 2");
        user.setEmail("testUser2@email.ru");
        return user;
    }

    private Item getTestItem1() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item Name 1");
        item.setDescription("Test Item Description 1");
        item.setOwner(getTestUser1());
        item.setRequest(null);
        return item;
    }

    private Item getTestItem2() {
        Item item = new Item();
        item.setId(2L);
        item.setName("Test Item Name 2");
        item.setDescription("Test Item Description 2");
        item.setOwner(getTestUser2());
        item.setRequest(null);
        return item;
    }

    private Comment getTestComment() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test Comment Text 1");
        comment.setItem(getTestItem1());
        comment.setAuthor(getTestUser2());
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

}
