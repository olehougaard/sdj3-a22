package dk.via.bank;

import dk.via.bank.dto.Account;
import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.Customer;
import dk.via.bank.dto.Money;
import dk.via.bank.dto.transaction.AbstractTransaction;

import java.util.Collection;
import java.util.List;

public class BranchClient implements Branch {
    public BranchClient(String host, int port) {

    }

    @Override
    public Customer createCustomer(String cpr, String name, String address) {
        return null;
    }

    @Override
    public Customer getCustomer(String cpr) {
        return null;
    }

    @Override
    public Account createAccount(Customer customer, String currency) {
        return null;
    }

    @Override
    public Account getAccount(Customer customer, AccountNumber accountNumber) {
        return null;
    }

    @Override
    public void cancelAccount(Account account) {

    }

    @Override
    public Collection<Account> getAccounts(Customer customer) {
        return null;
    }

    @Override
    public void execute(AbstractTransaction t) {

    }

    @Override
    public Money exchange(Money amount, String targetCurrency) {
        return null;
    }

    @Override
    public List<AbstractTransaction> getTransactionsFor(Account account) {
        return null;
    }
}
