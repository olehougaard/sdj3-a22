package dk.via.bank.dao;

import dk.via.bank.AccountDAO;
import dk.via.bank.TransactionDAO;
import dk.via.bank.dao.exception.NotFound;
import dk.via.bank.dto.Account;
import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.Money;
import dk.via.bank.dto.parameters.TransactionSpecification;
import dk.via.bank.dto.transaction.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Scope("singleton")
public class TransactionDAOImpl implements TransactionDAO {
	private static final String DEPOSIT = "Deposit";
	private static final String TRANSFER = "Transfer";
	private static final String WITHDRAWAL = "Withdrawal";

	private final DatabaseHelper<AbstractTransaction> helper;
	private final AccountDAO accounts;

	public TransactionDAOImpl(@Autowired AccountDAO accounts, @Autowired DatabaseHelper<AbstractTransaction> helper) {
		this.accounts = accounts;
		this.helper = helper;
	}
	
	private class TransactionMapper implements DataMapper<AbstractTransaction> {
		private final AccountNumber accountNumber;

		public TransactionMapper(AccountNumber accountNumber) {
			this.accountNumber = accountNumber;
		}

		@Override
		public AbstractTransaction create(ResultSet rs) throws SQLException {
			int id = rs.getInt("transaction_id");
			Money amount = new Money(rs.getBigDecimal("amount"), rs.getString("currency"));
			String text = rs.getString("transaction_text");
			Account primary = readAccount(rs, "primary_reg_number", "primary_account_number");
			AbstractTransaction transaction = null;
			switch(rs.getString("transaction_type")) {
			case DEPOSIT:
				transaction = new DepositTransaction(id, amount, primary, text);
				break;
			case WITHDRAWAL:
				transaction = new WithdrawTransaction(id, amount, primary, text);
				break;
			case TRANSFER:
				Account secondaryAccount = readAccount(rs, "secondary_reg_number", "secondary_account_number");
				transaction = new TransferTransaction(id, amount, primary, secondaryAccount, text);
			default:
			}
			if (transaction != null && !transaction.includes(accountNumber)) return null;
			return transaction;
		}

		private Account readAccount(ResultSet rs, String regNumberAttr, String acctNumberAttr) throws SQLException {
			return accounts.getAccount(new AccountNumber(rs.getInt(regNumberAttr), rs.getInt(acctNumberAttr)));
		}
	}
	
	private class TransactionCreator implements TransactionVisitor {
		@Override
		public void visit(DepositTransaction transaction) {
			Money amount = transaction.getAmount();
			AccountNumber primaryAccount = transaction.getAccount().getAccountNumber();
			List<Integer> keys = helper.executeUpdateWithGeneratedKeys(
					"INSERT INTO Transaction(amount, currency, transaction_type, transaction_text, primary_reg_number, primary_account_number) VALUES (?, ?, ?, ?, ?, ?)",
					amount.getAmount(), amount.getCurrency(), DEPOSIT, transaction.getText(),
					primaryAccount.getRegNumber(), primaryAccount.getAccountNumber());
			transaction.setId(keys.get(0));
		}

		@Override
		public void visit(WithdrawTransaction transaction) {
			Money amount = transaction.getAmount();
			AccountNumber primaryAccount = transaction.getAccount().getAccountNumber();
			List<Integer> keys = helper.executeUpdateWithGeneratedKeys(
					"INSERT INTO Transaction(amount, currency, transaction_type, transaction_text, primary_reg_number, primary_account_number) VALUES (?, ?, ?, ?, ?, ?)",
					amount.getAmount(), amount.getCurrency(), WITHDRAWAL, transaction.getText(),
					primaryAccount.getRegNumber(), primaryAccount.getAccountNumber());
			transaction.setId(keys.get(0));
		}

		@Override
		public void visit(TransferTransaction transaction) {
			Money amount = transaction.getAmount();
			AccountNumber primaryAccount = transaction.getAccount().getAccountNumber();
			AccountNumber secondaryAccount = transaction.getRecipient().getAccountNumber();
			List<Integer> keys = helper.executeUpdateWithGeneratedKeys(
					"INSERT INTO Transaction(amount, currency, transaction_type, transaction_text, primary_reg_number, primary_account_number, secondary_reg_number, secondary_account_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
					amount.getAmount(), amount.getCurrency(), TRANSFER, transaction.getText(),
					primaryAccount.getRegNumber(), primaryAccount.getAccountNumber(),
					secondaryAccount.getRegNumber(), secondaryAccount.getAccountNumber());
			transaction.setId(keys.get(0));
		}
	}
	
	private final TransactionCreator creator = new TransactionCreator();

	@Override
	public AbstractTransaction createTransaction(String cpr, AccountNumber accountNumber, TransactionSpecification transactionSpec) {
		Account account = accounts.getAccount(accountNumber);
		if (account == null) {
			throw new NotFound();
		}
		AbstractTransaction transaction = transactionSpec.toTransaction(account);
		transaction.accept(creator);
		return transaction;
	}

	@Override
	public AbstractTransaction readTransaction(String cpr, AccountNumber accountNumber, int transactionId) {
		AbstractTransaction transaction = getTransaction(cpr, accountNumber, transactionId);
		if (transaction == null || !transaction.includes(accountNumber)) {
			throw new NotFound();
		}
		return transaction;
	}

	private AbstractTransaction getTransaction(String cpr, AccountNumber accountNumber, int transactionId) {
		return helper.mapSingle(new TransactionMapper(accountNumber), "SELECT * FROM Transaction WHERE transaction_id = ?", transactionId);
	}

	@Override
	public List<AbstractTransaction> readTransactionsFor(String cpr, AccountNumber accountNumber) {
		return helper.map(new TransactionMapper(accountNumber),
				"SELECT * FROM Transaction WHERE (primary_reg_number = ? AND primary_account_number = ?) OR (secondary_reg_number = ? AND secondary_account_number = ?)",
				accountNumber.getRegNumber(), accountNumber.getAccountNumber(),accountNumber.getRegNumber(), accountNumber.getAccountNumber());
	}
}
