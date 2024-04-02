package ru.x5.dnd.telegrambot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.x5.dnd.telegrambot.config.TestConfiguration;

@SpringBootTest
@Import(TestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TelegramBotApplicationTests {

	@Test
	void contextLoads() {
	}

}
