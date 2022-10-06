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

public interface Branch extends Remote {
	Customer createCustomer(String cpr, String name, String address) throws RemoteException;
	Customer getCustomer(String cpr) throws RemoteException;
	Account createAccount(Customer customer, String currency) throws RemoteException;
	Account getAccount(Customer customer, AccountNumber accountNumber) throws RemoteException;
	void cancelAccount(Account account) throws RemoteException;
	Collection<Account> getAccounts(Customer customer) throws RemoteException;
	void execute(AbstractTransaction t) throws RemoteException;
	Money exchange(Money amount, String targetCurrency) throws RemoteException;
	List<AbstractTransaction> getTransactionsFor(Account account) throws RemoteException;
}
