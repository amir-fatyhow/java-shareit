package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.User;

public class UserRowMapper {

    public static UserDto toItemDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
