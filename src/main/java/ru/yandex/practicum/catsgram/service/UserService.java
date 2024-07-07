package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final Map<Long, User> users;

    public UserService() {
        users = new HashMap<>();
    }

    public Collection<User> findAll() {
        return users.values();
    }


    public User create(User user) {
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

    public User update(User newUser) {
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

    public Optional<User> findUserById(long id) {
        User user = users.get(id);

        if (user != null) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }
}
