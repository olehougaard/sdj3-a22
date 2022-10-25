package dk.via.bank.dto.transaction;

import dk.via.bank.dto.Account;
import dk.via.bank.dto.Money;

public class WithdrawTransaction extends AbstractTransaction {
	public WithdrawTransaction(int id, Money amount, Account account) {
		this(id, amount, account, "Withdrew " + amount);
	}

	public WithdrawTransaction(int id, Money amount, Account account, String text) {
		super(id, amount, account, text);
	}


	@Override
	public void accept(TransactionVisitor visitor) {
		visitor.visit(this);
	}

	// JAX-WS

	public WithdrawTransaction() {
	}
}
