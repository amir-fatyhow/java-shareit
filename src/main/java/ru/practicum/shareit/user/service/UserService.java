package ru.practicum.shareit.user.service;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ShareItNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;

@Service
public interface UserService {

    UserDto save(UserDto userDto);

    UserDto update(Map<Object, Object> fields, long userId) throws JsonMappingException;

    void deleteById(long userId);

    List<UserDto> findAll();

    UserDto findUserById(long userId);

    User checkUser(long userId) throws ShareItNotFoundException;


}
