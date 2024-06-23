package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        // проверяем выполнение необходимых условий
        String userEmail = user.getEmail();
        if (userEmail == null || userEmail.isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }

        if (emailIsBusy(userEmail, 0L)) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        // формируем дополнительные данные
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        // сохраняем новую публикацию в памяти приложения
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        // проверяем необходимые условия
        Long id = newUser.getId();
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        String email = newUser.getEmail();

        if (emailIsBusy(email, id)) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (users.containsKey(id)) {

            User oldUser = users.get(id);

            String username = newUser.getUsername();

            if (username != null && !username.isBlank()) {
                oldUser.setUsername(username);
            }

            String password = newUser.getPassword();

            if (password != null && !password.isBlank()) {
                oldUser.setPassword(password);
            }

            if (email != null && !email.isBlank()) {
                oldUser.setEmail(email);
            }

            return oldUser;
        }
        throw new NotFoundException("ользователь с id = " + newUser.getId() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean emailIsBusy(String email, long id) {
        boolean emailIsFree = users.values()
                .stream()
                .filter(user -> user.getId() != id)
                .map(User::getEmail)
                .filter(emailOfStream -> emailOfStream.equals(email))
                .findAny()
                .isEmpty();
        return !emailIsFree;
    }
}