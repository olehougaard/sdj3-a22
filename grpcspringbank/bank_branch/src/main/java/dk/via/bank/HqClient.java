package dk.via.bank;

import dk.via.bank.dto.Account;
import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.Customer;
import dk.via.bank.dto.ExchangeRate;
import dk.via.bank.dto.parameters.AccountSpecification;
import dk.via.bank.dto.parameters.TransactionSpecification;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.*;

public class HqClient {
    private final URI endpoint;
    private final RestTemplate restTemplate;

    public HqClient(URI endpoint) {
        this.endpoint = endpoint;
        this.restTemplate = new RestTemplate();
    }

    private String customerURI(String cpr) {
        return endpoint + "customers/" + cpr;
    }

    private String accountURI(AccountNumber accountNumber) {
        return endpoint + "accounts/" + accountNumber;
    }

    private String accountURI(Account account) {
        return accountURI(account.getAccountNumber());
    }

    public Customer createCustomer(String cpr, String name, String address) {
        Customer customer = new Customer(cpr, name, address);
        return restTemplate.postForEntity(endpoint + "customers", customer, Customer.class).getBody();
    }

    public Customer getCustomer(String cpr) {
        return restTemplate.getForObject(customerURI(cpr), Customer.class);
    }

    public Account createAccount(Customer customer, int regNumber, String currency) {
        AccountSpecification account = new AccountSpecification(regNumber, currency, customer.getCpr());
        return restTemplate.postForEntity(endpoint + "accounts", account, Account.class).getBody();
    }

    public Account getAccount(AccountNumber accountNumber) {
        return restTemplate.getForObject(accountURI(accountNumber), Account.class);
    }

    public void cancelAccount(Account account) {
        restTemplate.delete(accountURI(account));
    }

    public List<Account> getAccounts(Customer customer) {
        UriBuilder builder = UriComponentsBuilder.fromUri(endpoint);
        builder.path("accounts");
        builder.queryParam("cpr", customer.getCpr());
        Traverson traverson = new Traverson(builder.build(), MediaTypes.HAL_JSON);
        CollectionModel<Account> exchangeRates = traverson.follow()
                .toObject(new TypeReferences.CollectionModelType<Account>() {});
        Collection<Account> accounts = Objects.requireNonNull(exchangeRates).getContent();
        return new ArrayList<>(accounts);
    }

    public void updateAccount(Account account) {
        restTemplate.put(accountURI(account), account);
    }

    public void createTransaction(Account account, TransactionSpecification transaction) {
        restTemplate.postForEntity(accountURI(account) + "/transactions", transaction, TransactionSpecification.class);
    }

    public ExchangeRate getExchangeRate(String fromCurrency, String toCurrency) {
        UriBuilder builder = UriComponentsBuilder.fromUri(endpoint);
        builder.path("exchange_rate");
        builder.queryParam("fromCurrency", fromCurrency);
        builder.queryParam("toCurrency", toCurrency);
        Traverson traverson = new Traverson(builder.build(), MediaTypes.HAL_JSON);
        CollectionModel<ExchangeRate> exchangeRates = traverson.follow()
                .toObject(new TypeReferences.CollectionModelType<ExchangeRate>() {});
        Collection<ExchangeRate> rates = Objects.requireNonNull(exchangeRates).getContent();
        if (rates.size() != 1) throw new IllegalArgumentException("Unknown currencies");
        return rates.iterator().next();
    }

    public Collection<TransactionSpecification> getTransactionsFor(Account account) {
        URI baseUri = endpoint
                .resolve("accounts/")
                .resolve(account.getAccountNumber().toString());
        Traverson traverson = new Traverson(baseUri, MediaTypes.HAL_JSON);
        CollectionModel<TransactionSpecification> ts = traverson
                .follow("$._links.transactions.href")
                .toObject(new TypeReferences.CollectionModelType<TransactionSpecification>() {});
        assert ts != null;
        return ts.getContent();
    }
}
