package ru.practicum.shareit.userTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Test
    void saveTest() throws Exception {
        // Assign
        UserDto userDto = new UserDto(1L, " Name", "userDto@email.ru");

        when(userService.save(any(UserDto.class)))
                .thenReturn(userDto);

        // Act
        mvc.perform(post("/users")
                        .header("X-Sharer-User-Id", userDto.getId())
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))

                        // Assert
                        .andExpect(jsonPath("$.id").value(userDto.getId()))
                        .andExpect(jsonPath("$.name").value(userDto.getName()))
                        .andExpect(jsonPath("$.email").value(userDto.getEmail())
                );
    }

    @Test
    void updateTest() throws Exception {
        // Assign
        UserDto userDto = new UserDto(1L, " Name", "userDto@email.ru");

        when(userService.update(any(Map.class), anyLong()))
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
    void deleteTest() throws Exception {
        // Assign
        Long userId = 1L;

        // Act
        mvc.perform(delete("/users/{userId}", userId))
                // Assert
                .andExpect(status().isOk());
    }


    @Test
    void findByIdTest() throws Exception {
        // Assign
        UserDto userDto = new UserDto(1L, " Name", "userDto@email.ru");

        when(userService.findById(anyLong()))
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
    void findAllTest() throws Exception {
        // Assign
        UserDto userDto1 = new UserDto(1L, "Name 1", "userDto1@email.ru");
        UserDto userDto2 = new UserDto(2L, "Name 2", "userDto2@email.ru");
        UserDto userDto3 = new UserDto(3L, "Name 3", "userDto3@email.ru");
        List<UserDto> users = List.of(userDto1, userDto2, userDto3);

        when(userService.findAll())
                .thenReturn(users);

        // Act
        mvc.perform(get("/users"))
                // Assert
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(users)));
    }
}
