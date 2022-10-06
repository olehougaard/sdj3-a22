package dk.via.bank;

import dk.via.bank.dao.DatabaseHelper;
import dk.via.bank.dto.Account;
import dk.via.bank.dto.Customer;
import dk.via.bank.dto.ExchangeRate;
import dk.via.bank.dto.transaction.AbstractTransaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class Config {
    public static final String JDBC_URL = "jdbc:postgresql://localhost:5433/postgres?currentSchema=bank";
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = "password";

    @Bean
    public DatabaseHelper<Account> getAccountHelper() {
        return new DatabaseHelper<>(JDBC_URL, USERNAME, PASSWORD);
    }

    @Bean
    public<T> DatabaseHelper<Customer> getCustomerHelper() {
        return new DatabaseHelper<>(JDBC_URL, USERNAME, PASSWORD);
    }

    @Bean
    public<T> DatabaseHelper<ExchangeRate> getExchangeRateHelper() {
        return new DatabaseHelper<>(JDBC_URL, USERNAME, PASSWORD);
    }

    @Bean
    public<T> DatabaseHelper<AbstractTransaction> getTransactionHelper() {
        return new DatabaseHelper<>(JDBC_URL, USERNAME, PASSWORD);
    }
}
