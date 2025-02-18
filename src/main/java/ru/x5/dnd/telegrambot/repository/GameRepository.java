package ru.x5.dnd.telegrambot.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;
import ru.x5.dnd.telegrambot.model.Game;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

public interface GameRepository extends CrudRepository<Game, Long> {

    Optional<Game> findFirstByChatIdAndMessageIdAndMessageThreadId(@NotNull String chatId, @NotNull String messageId, String messageThreadId);

    Stream<Game> findByChatIdAndGameDateAfter(@NotNull String chatId, @NotNull LocalDate gameDateAfter);
}
