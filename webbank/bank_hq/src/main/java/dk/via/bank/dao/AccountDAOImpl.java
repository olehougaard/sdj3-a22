package dk.via.bank.dao;

import dk.via.bank.AccountDAO;
import dk.via.bank.dao.exception.BadRequest;
import dk.via.bank.dao.exception.Conflict;
import dk.via.bank.dao.exception.MethodNotAllowed;
import dk.via.bank.dao.exception.NotFound;
import dk.via.bank.dto.Account;
import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.Money;
import dk.via.bank.dto.parameters.AccountSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Scope("singleton")
public class AccountDAOImpl implements AccountDAO {
	private final DatabaseHelper<Account> helper;

	public AccountDAOImpl(@Autowired DatabaseHelper<Account> helper) {
		this.helper = helper;
	}

	public Account createAccount(String cpr, AccountSpecification specification) {
		int regNumber = specification.getRegNumber();
		String currency = specification.getCurrency();
		final List<Integer> keys = helper.executeUpdateWithGeneratedKeys("INSERT INTO Account(reg_number, customer, currency) VALUES (?, ?, ?)",
				regNumber, cpr, currency);
		return getAccount(new AccountNumber(regNumber, keys.get(0)));
	}
	
	public static class AccountMapper implements DataMapper<Account>{
		@Override
		public Account create(ResultSet rs) throws SQLException {
			AccountNumber accountNumber = new AccountNumber(rs.getInt("reg_number"), rs.getLong("account_number"));
			BigDecimal balance = rs.getBigDecimal("balance");
			String currency = rs.getString("currency");
			String customerCpr = rs.getString("customer");
			return new Account(accountNumber, new Money(balance, currency), customerCpr);
		}
		
	}

    public List<Account> readAccountsFor(String cpr) {
		return helper.map(new AccountMapper(), "SELECT * FROM Account WHERE customer = ? AND active", cpr) ;
	}

    public Account readAccount(String cpr, AccountNumber accountNumber) {
		Account account = getAccount(accountNumber);
		if (account == null) {
			throw new NotFound();
		}
		return account;
	}

	@Override
	public Account getAccount(AccountNumber accountNumber) {
		return helper.mapSingle(new AccountMapper(),
				"SELECT * FROM Account WHERE reg_number = ? AND account_number = ? AND active",
				accountNumber.getRegNumber(), accountNumber.getAccountNumber());
	}

    public void updateAccount(String cpr, AccountNumber accountNumber, Account account) {
		if (account.getAccountNumber() != null && !account.getAccountNumber().equals(accountNumber)) {
			throw new Conflict();
		}
		if (getAccount(accountNumber) == null) {
			throw new MethodNotAllowed();
		}
		if (!account.getSettledCurrency().equals(account.getBalance().getCurrency())) {
			throw new BadRequest();
		}
		helper.executeUpdate("UPDATE ACCOUNT SET balance = ?, currency = ? WHERE reg_number = ? AND account_number = ? AND active",
				account.getBalance().getAmount(), account.getSettledCurrency(), accountNumber.getRegNumber(), accountNumber.getAccountNumber());
	}

    public void deleteAccount(String cpr, AccountNumber accountNumber) {
		helper.executeUpdate(
				"UPDATE ACCOUNT SET active = FALSE WHERE reg_number = ? AND account_number = ? AND customer = ?",
				accountNumber.getRegNumber(), accountNumber.getAccountNumber(), cpr);
	}
}
