package dk.via.bank;

import dk.via.bank.dto.Account;
import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.parameters.AccountSpecification;

import java.util.Collection;
import java.util.List;

public interface AccountDAO {
    Account createAccount(String cpr, AccountSpecification specification);

    List<Account> readAccountsFor(String cpr);

    Account readAccount(String cpr, AccountNumber accountNumber);

    Account getAccount(AccountNumber accountNumber);

    void updateAccount(String cpr, AccountNumber accountNumber, Account account);

    void deleteAccount(String cpr, AccountNumber accountNumber);
}
