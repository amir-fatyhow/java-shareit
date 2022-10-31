package ru.practicum.shareit.userTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

@JsonTest
public class UserJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    public void userDtoJsonTest() throws Exception {
        UserDto userDto = new UserDto(1L, "Name", "email@email.ru");
        JsonContent<UserDto> jsonContent = json.write(userDto);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("Name");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.email").isEqualTo("email@email.ru");
    }
}
