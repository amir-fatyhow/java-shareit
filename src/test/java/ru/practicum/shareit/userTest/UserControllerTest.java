package ru.practicum.shareit.userTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controllers.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.services.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private ObjectMapper mapper; // Встроенный в Spring маппер для преобразования объекта в строку формата JSON

    @MockBean
    private UserService userService; // Мокаем бин

    @Autowired
    private MockMvc mvc; // Позволяет выполнять запросы

    @Test
    public void createUserTest() throws Exception {
        // Assign
        UserDto userDto = new UserDto(1L, "Test UserDto Name", "testUserDto@email.ru");

        // Так как мы тестируем только контроллеры, то сервис надо замокать
        when(userService.createUser(any(UserDto.class)))
                .thenReturn(userDto);

        // Act
        mvc.perform(post("/users") // Отправляем запрос на этот URL
                        .header("X-Sharer-User-Id", userDto.getId()) // Указываем header
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8) // Кодировка// Передаём userDto в body, при помощи маппера преобразуем dto в строку формата JSON
                        .contentType(MediaType.APPLICATION_JSON) // Возвращаем json
                        .accept(MediaType.APPLICATION_JSON)) // принимаем json
                // Assert
                .andExpect(status().isCreated()) // Должен прийти ответ 201
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail())
                );
    }

    @Test
    public void updateUserTest() throws Exception {
        // Assign
        UserDto userDto = new UserDto(1L, "Test UserDto Name", "testUserDto@email.ru");

        when(userService.updateUser(any(UserDto.class), anyLong()))
                .thenReturn(userDto);

        // Act
        mvc.perform(patch("/users/{userId}", userDto.getId())
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    public void deleteUserTest() throws Exception {
        // Assign
        Long userId = 1L;

        // Act
        mvc.perform(delete("/users/{userId}", userId))
                // Assert
                .andExpect(status().isOk());
    }


    @Test
    public void findUserByIdTest() throws Exception {
        // Assign
        UserDto userDto = new UserDto(1L, "Test UserDto Name", "testUserDto@email.ru");

        when(userService.findUserById(anyLong()))
                .thenReturn(userDto);

        // Act
        mvc.perform(get("/users/{userId}", userDto.getId()))
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    public void findAllUsersTest() throws Exception {
        // Assign
        UserDto userDto1 = new UserDto(1L, "Test UserDto Name 1", "testUserDto1@email.ru");
        UserDto userDto2 = new UserDto(2L, "Test UserDto Name 2", "testUserDto2@email.ru");
        UserDto userDto3 = new UserDto(3L, "Test UserDto Name 3", "testUserDto3@email.ru");
        List<UserDto> users = List.of(userDto1, userDto2, userDto3);

        when(userService.findAllUsers())
                .thenReturn(users);

        // Act
        mvc.perform(get("/users"))
                // Assert
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(users)));
    }
}
