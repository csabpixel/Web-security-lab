package app.vulnerable.config;

import app.vulnerable.model.CreditCard;
import app.vulnerable.model.Product;
import app.vulnerable.model.User;
import app.vulnerable.repository.CreditCardRepository;
import app.vulnerable.repository.ProductRepository;
import app.vulnerable.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      ProductRepository productRepository,
                                      CreditCardRepository creditCardRepository,
                                      JdbcTemplate jdbc) {
        return args -> {
            jdbc.execute(
                "CREATE ALIAS IF NOT EXISTS SLEEP "
                + "FOR \"app.vulnerable.service.SqliService.sleep\""
            );
            jdbc.execute(
                "CREATE ALIAS IF NOT EXISTS SLEEP_MS "
                + "FOR \"app.vulnerable.service.SqliService.sleepMs\""
            );

            userRepository.save(new User("admin", "admin@test.com", "ADMIN", "S3cretAdminP@ss"));
            userRepository.save(new User("user",  "user@test.com",  "USER",  "userpass"));
            userRepository.save(new User("guest", "guest@test.com", "GUEST", "guestpass"));

            productRepository.save(new Product("Apple",1.20));
            productRepository.save(new Product("Banana",0.80));
            productRepository.save(new Product("Carrot",0.50));
            productRepository.save(new Product("Egg",4.90));
            productRepository.save(new Product("Coconut",2.30));
            productRepository.save(new Product("Onion",3.10));

            creditCardRepository.save(new CreditCard("Nagy Lambda",      "4111-1111-1111-1111", "123", "12/27"));
            creditCardRepository.save(new CreditCard("Kétszer Kettő",    "5500-0000-0000-0004", "456", "06/26"));
            creditCardRepository.save(new CreditCard("Minusz Végtelen", "3400-0000-0000-009",  "7890", "03/28"));
        };
    }
}
