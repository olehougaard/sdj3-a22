package dk.via.bank.dto.transaction;

import dk.via.bank.dto.Account;
import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.Money;

public abstract class AbstractTransaction implements Transaction {
	private int id;
	private Money amount;
	private Account account;
	private String text;

	public AbstractTransaction(int id, Money amount, Account account, String text) {
		if (account == null) {
			throw new NullPointerException();
		}
		this.amount = amount;
		this.account = account;
		this.text = text;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Money getAmount() {
		return amount;
	}

	public Account getAccount() {
		return account;
	}

	public boolean includes(AccountNumber accountNumber) {
		return accountNumber.equals(account.getAccountNumber());
	}
	
	@Override
	public String getText() {
		return text;
	}

	// JAX-WS
	public AbstractTransaction() {}

	public void setId(int id) {
		this.id = id;
	}

	public void setAmount(Money amount) {
		this.amount = amount;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public void setText(String text) {
		this.text = text;
	}
}