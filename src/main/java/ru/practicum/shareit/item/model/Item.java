package ru.practicum.shareit.item.model;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ITEMS")
@Getter
@Setter
public class Item {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    @NotBlank
    @NotNull
    private String name;

    @Column(name = "DESCRIPTION", length = 1024)
    @NotBlank
    @NotNull
    private String description;

    @Column(name = "IS_AVAILABLE")
    @NotNull
    private Boolean available;

    @Column(name = "OWNER_ID")
    private long ownerId;

    @Column(name = "REQUEST_ID")
    private Long requestId;
}
