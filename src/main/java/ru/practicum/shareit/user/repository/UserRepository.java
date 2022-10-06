package ru.practicum.shareit.user.repository;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

public interface UserRepository extends JpaRepository<User, Long> {

}
