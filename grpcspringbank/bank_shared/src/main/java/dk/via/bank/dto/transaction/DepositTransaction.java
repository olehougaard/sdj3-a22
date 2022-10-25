package dk.via.bank.dto.transaction;

import dk.via.bank.dto.Account;
import dk.via.bank.dto.Money;

public class DepositTransaction extends AbstractTransaction {

	public DepositTransaction(int id, Money amount, Account account) {
		this(id, amount, account, "Deposited " + amount);
	}
	
	public DepositTransaction(int id, Money amount, Account account, String text) {
		super(id, amount, account, text);
	}

	@Override
	public void accept(TransactionVisitor visitor) {
		visitor.visit(this);
	}

	// JAX-WS

	public DepositTransaction() {
	}
}
