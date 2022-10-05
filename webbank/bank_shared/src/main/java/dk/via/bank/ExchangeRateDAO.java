package dk.via.bank;

import dk.via.bank.dto.ExchangeRate;

import java.util.List;

public interface ExchangeRateDAO {
    List<ExchangeRate> getExchangeRates();

    List<ExchangeRate> getExchangeRatesTo(String toCurrency);

    List<ExchangeRate> getExchangeRatesFrom(String fromCurrency);

    ExchangeRate getExchangeRate(String fromCurrency, String toCurrency);
}
