package dk.via.bank;

import dk.via.bank.dto.Account;
import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.parameters.AccountSpecification;

import java.util.Collection;
import java.util.List;

public interface AccountDAO {
    Account createAccount(AccountSpecification specification);

    List<Account> readAccountsFor(String cpr);

    Account readAccount(AccountNumber accountNumber);

    Account getAccount(AccountNumber accountNumber);

    void updateAccount(AccountNumber accountNumber, Account account);

    void deleteAccount(AccountNumber accountNumber);
}
