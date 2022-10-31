package ru.practicum.shareit.requestTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

@JsonTest
public class RequestJsonTest {

    @Autowired
    private JacksonTester<RequestWithResponseDto> json;

    @Test
    public void requestWithResponseDtoJsonTest() throws Exception {
        RequestWithResponseDto requestWithResponseDto = new RequestWithResponseDto(1L, "Test Description", LocalDateTime.now(), new ArrayList<>());
        JsonContent<RequestWithResponseDto> jsonContent = json.write(requestWithResponseDto);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        Assertions.assertThat(jsonContent).extractingJsonPathArrayValue("$.items").isEqualTo(Collections.emptyList());
    }
}
