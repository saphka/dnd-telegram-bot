package ru.x5.dnd.telegrambot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String chatId;

    @NotNull
    private String messageId;

    private String messageThreadId;

    @NotNull
    private String author;

    @NotNull
    private LocalDate gameDate;

    @NotNull
    private Integer maxPlayers;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GameRegistration> gameRegistrations = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull String getChatId() {
        return chatId;
    }

    public void setChatId(@NotNull String chatId) {
        this.chatId = chatId;
    }

    public @NotNull String getMessageId() {
        return messageId;
    }

    public void setMessageId(@NotNull String messageId) {
        this.messageId = messageId;
    }

    public String getMessageThreadId() {
        return messageThreadId;
    }

    public void setMessageThreadId(String messageThreadId) {
        this.messageThreadId = messageThreadId;
    }

    public @NotNull String getAuthor() {
        return author;
    }

    public void setAuthor(@NotNull String author) {
        this.author = author;
    }

    public @NotNull LocalDate getGameDate() {
        return gameDate;
    }

    public void setGameDate(@NotNull LocalDate gameDate) {
        this.gameDate = gameDate;
    }

    public List<GameRegistration> getGameRegistrations() {
        return gameRegistrations;
    }

    public void setGameRegistrations(List<GameRegistration> gameRegistrations) {
        this.gameRegistrations = gameRegistrations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Game game = (Game) o;
        return Objects.equals(id, game.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", chatId='" + chatId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", messageThreadId='" + messageThreadId + '\'' +
                ", author='" + author + '\'' +
                ", gameDate=" + gameDate +
                ", maxPlayers=" + maxPlayers +
                ", status=" + status +
                '}';
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}
