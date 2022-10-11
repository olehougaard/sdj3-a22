package dk.via.bank;

import dk.via.bank.dto.*;
import dk.via.bank.dto.parameters.TransactionSpecification;
import dk.via.bank.dto.transaction.*;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class RemoteBranch implements Branch {
	private final int regNumber;
	private final HqClient hqClient;

	public RemoteBranch(int regNumber, HqClient hqClient) {
		this.regNumber = regNumber;
		this.hqClient = hqClient;
	}

	@Override
	public Customer createCustomer(String cpr, String name, String address) {
		return hqClient.createCustomer(cpr, name, address);
	}

	@Override
	public Customer getCustomer(String cpr) {
		return hqClient.getCustomer(cpr);
	}

	@Override
	public Account createAccount(Customer customer, String currency) {
		return hqClient.createAccount(customer, regNumber, currency);
	}

	@Override
	public Account getAccount(Customer customer, AccountNumber accountNumber) {
		return hqClient.getAccount(accountNumber);
	}
	
	@Override
	public void cancelAccount(Account account) {
		hqClient.cancelAccount(account);
	}

	@Override
	public Collection<Account> getAccounts(Customer customer) {
		return hqClient.getAccounts(customer);
	}

	private class BranchTransactionVisitor implements TransactionVisitor {
		@Override
		public void visit(DepositTransaction transaction) {
			Account account = transaction.getAccount();
			Money amount = transaction.getAmount();
			amount = translateToSettledCurrency(amount, account);
			account.deposit(amount);
			hqClient.updateAccount(account);
		}

		@Override
		public void visit(WithdrawTransaction transaction) {
			Account account = transaction.getAccount();
			Money amount = transaction.getAmount();
			amount = translateToSettledCurrency(amount, account);
			account.withdraw(amount);
			hqClient.updateAccount(account);

		}

		@Override
		public void visit(TransferTransaction transaction) {
			visit(transaction.getDepositTransaction());
			visit(transaction.getWithdrawTransaction());
		}
	}
	
	@Override
	public void execute(AbstractTransaction t)  {
		t.accept(new BranchTransactionVisitor());
		hqClient.createTransaction(t.getAccount(), TransactionSpecification.from(t));
	}

	private ExchangeRate getExchangeRate(String fromCurrency, String toCurrency) {
		return hqClient.getExchangeRate(fromCurrency, toCurrency);
	}

	private Money exchange(Money money, String fromCurrency, String toCurrency) {
			ExchangeRate rate = getExchangeRate(fromCurrency, toCurrency);
			return rate.exchange(money);
	}
	
	private Money translateToSettledCurrency(Money amount, Account account) {
		if (amount.getCurrency().equals(account.getSettledCurrency())) {
			return amount;
		} else {
			return exchange(amount, amount.getCurrency(), account.getSettledCurrency());
		}
	}

	@Override
	public Money exchange(Money amount, String targetCurrency)  {
		if (targetCurrency.equals(amount.getCurrency()))
			return amount;
		return exchange(amount, amount.getCurrency(), targetCurrency);
	}
	
	@Override
	public List<AbstractTransaction> getTransactionsFor(Account primaryAccount) {
		return hqClient.getTransactionsFor(primaryAccount)
				.stream()
				.map(spec -> spec.toTransaction(primaryAccount))
				.collect(toList());
	}
}
