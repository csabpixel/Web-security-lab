package app.vulnerable.config;

import app.vulnerable.model.User;
import app.vulnerable.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, JdbcTemplate jdbc) {
        return args -> {
            jdbc.execute(
                "CREATE ALIAS IF NOT EXISTS SLEEP "
                + "FOR \"app.vulnerable.service.SqliService.sleep\""
            );
            jdbc.execute(
                "CREATE ALIAS IF NOT EXISTS SLEEP_MS "
                + "FOR \"app.vulnerable.service.SqliService.sleepMs\""
            );

            userRepository.save(new User("admin", "admin@test.com", "ADMIN"));
            userRepository.save(new User("user", "user@test.com", "USER"));
            userRepository.save(new User("guest", "guest@test.com", "GUEST"));
        };
    }
}