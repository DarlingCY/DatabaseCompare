package icu.greenlemon.databasecompare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DatabaseCompareApplication {
    public static void main(String[] args) {
        SpringApplication.run(DatabaseCompareApplication.class, args);
    }
}
