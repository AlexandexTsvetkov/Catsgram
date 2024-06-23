package ru.yandex.practicum.catsgram.model;

import lombok.EqualsAndHashCode;

import java.time.Instant;

@EqualsAndHashCode(of = { "email" })
public class User {

    private Long id;
    private String username;
    private String email;
    private String password;
    private Instant registrationDate;
}
