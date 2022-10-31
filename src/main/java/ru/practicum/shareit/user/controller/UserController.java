package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto save(@RequestBody UserDto userDto) {
        return userService.save(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody Map<Object, Object> fields, @PathVariable long userId) throws JsonMappingException {
        return userService.update(fields, userId);
    }

    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{userId}")
    public UserDto findUserById(@PathVariable long userId) {
        return userService.findUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable long userId) {
        userService.deleteById(userId);
    }
}
