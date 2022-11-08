package ru.practicum.shareit.request.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.model.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.dto.RequestWithResponseDto;
import ru.practicum.shareit.request.model.entity.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requester) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        if (itemRequestDto.getCreated() != null) {
            itemRequest.setCreated(itemRequestDto.getCreated());
        } else {
            itemRequest.setCreated(LocalDateTime.now());
        }
        itemRequest.setRequestor(requester);
        return itemRequest;
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated()
        );
    }

    public static RequestWithResponseDto toRequestWithResponseDto(
            ItemRequest itemRequest, List<ItemForRequestDto> responses) {
        return new RequestWithResponseDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                responses);
    }
}
