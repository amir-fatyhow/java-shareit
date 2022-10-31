package ru.practicum.shareit.user.services;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
public interface UserService {

    UserDto createUser(UserDto userDto) throws BadRequestException;

    UserDto updateUser(UserDto userDto, long userId);

    void deleteUser(long userId);

    List<UserDto> findAllUsers();

    UserDto findUserById(long userId);

    User checkUser(long userId) throws NotFoundException;


}
