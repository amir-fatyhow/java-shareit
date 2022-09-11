package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    private long id;

    @NotBlank
    @NotNull
    private String name;

    @NotBlank
    @NotNull
    @Pattern(regexp = "^(.+)@(\\S+)$")
    private String email;
}
