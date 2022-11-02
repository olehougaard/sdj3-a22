package dk.via.bank.dao;

import dk.via.bank.ExchangeRateDAO;
import dk.via.bank.dao.exception.NotFound;
import dk.via.bank.dto.ExchangeRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Scope("singleton")
public class ExchangeRateDAOImpl implements ExchangeRateDAO {
	private final ExchangeRateMapper mapper;
	private final DatabaseHelper<ExchangeRate> helper;

	public ExchangeRateDAOImpl(@Autowired DatabaseHelper<ExchangeRate> helper) {
		mapper = new ExchangeRateMapper();
		this.helper = helper;
	}

	private static class ExchangeRateMapper implements DataMapper<ExchangeRate> {
		@Override
		public ExchangeRate create(ResultSet rs) throws SQLException {
			return new ExchangeRate(rs.getString("from_currency"), rs.getString("to_currency"), rs.getBigDecimal("rate"));
		}
	}

	@Override
	public List<ExchangeRate> getExchangeRates() {
		return helper.map(mapper, "SELECT * FROM Exchange_rates");
	}

	@Override
	public List<ExchangeRate> getExchangeRatesTo(String toCurrency) {
		return helper.map(mapper, "SELECT * FROM Exchange_rates WHERE to_currency = ?", toCurrency);
	}

	@Override
	public List<ExchangeRate> getExchangeRatesFrom(String fromCurrency) {
		return helper.map(mapper, "SELECT * FROM Exchange_rates WHERE from_currency = ?", fromCurrency);
	}

	@Override
	public ExchangeRate getExchangeRate(String fromCurrency, String toCurrency) {
		ExchangeRate rate = helper.mapSingle(mapper, "SELECT * FROM Exchange_rates WHERE from_currency = ? AND to_currency = ?", fromCurrency, toCurrency);
		if (rate == null) {
			throw new NotFound();
		}
		return rate;
	}
}
