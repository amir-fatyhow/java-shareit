package ru.practicum.shareit.user.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserDto save(UserDto userDto);

    UserDto update(Map<Object, Object> fields, long userId) throws JsonMappingException;

    UserDto findById(long userId);

    List<UserDto> findAll();

    void deleteById(long userId);

}
