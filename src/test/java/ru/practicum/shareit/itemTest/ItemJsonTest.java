package ru.practicum.shareit.itemTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemForRequestDto;

@JsonTest
public class ItemJsonTest {

    @Autowired
    private JacksonTester<ItemForRequestDto> json;

    @Test
    public void requestWithResponseDtoJsonTest() throws Exception {
        ItemForRequestDto itemForRequestDto = new ItemForRequestDto(1L, "Test Name", "Test Description", true, 1L);
        JsonContent<ItemForRequestDto> jsonContent = json.write(itemForRequestDto);

        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("Test Name");
        Assertions.assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        Assertions.assertThat(jsonContent).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        Assertions.assertThat(jsonContent).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
    }
}
