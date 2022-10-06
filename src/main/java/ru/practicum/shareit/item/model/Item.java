package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ITEMS")
@Data
public class Item {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    @NotBlank
    @NotNull
    private String name;

    @Column(name = "DESCRIPTION")
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
