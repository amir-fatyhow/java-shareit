package ru.practicum.shareit.user;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@Entity
@Table(name = "USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false)
    @NotBlank
    @NotNull
    private String name;

    @Column(name = "EMAIL", length = 512, unique = true, nullable = false)
    @NotBlank
    @NotNull
    @Pattern(regexp = "^(.+)@(\\S+)$")
    private String email;
}
