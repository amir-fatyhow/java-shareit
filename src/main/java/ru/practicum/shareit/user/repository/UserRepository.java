package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

public interface UserRepository {
    List<UserDto> getAllUsers();
    UserDto createUser(User user);

    UserDto getUserById(long userId);

    UserDto updateUser(Map<Object,Object> fields, long userId);

    void deleteUser(long userId);

}
