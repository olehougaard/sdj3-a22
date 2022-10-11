package dk.via.bank;

import dk.via.bank.dto.Account;
import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.Customer;
import dk.via.bank.dto.Money;
import dk.via.bank.dto.transaction.AbstractTransaction;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

public interface Branch {
	Customer createCustomer(String cpr, String name, String address);
	Customer getCustomer(String cpr);
	Account createAccount(Customer customer, String currency);
	Account getAccount(Customer customer, AccountNumber accountNumber);
	void cancelAccount(Account account);
	Collection<Account> getAccounts(Customer customer);
	void execute(AbstractTransaction t);
	Money exchange(Money amount, String targetCurrency);
	List<AbstractTransaction> getTransactionsFor(Account account);
}
