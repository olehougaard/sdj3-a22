package dk.via.bank.service;

import dk.via.bank.ExchangeRateDAO;
import dk.via.bank.dto.ExchangeRate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/exchange_rate")
public class ExchangeRateController {
	private final ExchangeRateDAO dao;

	public ExchangeRateController(@Autowired ExchangeRateDAO dao) {
		this.dao = dao;
	}

	private EntityModel<ExchangeRate> createEntity(ExchangeRate rate) {
		return EntityModel.of(rate,
				linkTo(methodOn(getClass()).getRates(rate.getFromCurrency(), rate.getToCurrency())).withSelfRel());
	}

	private CollectionModel<EntityModel<ExchangeRate>> createCollection(List<ExchangeRate> accounts) {
		List<EntityModel<ExchangeRate>> rateModels = accounts.stream().map(this::createEntity).collect(Collectors.toList());
		return CollectionModel.of(rateModels, linkTo(methodOn(getClass()).getRates(null, null)).withSelfRel());
	}

	@GetMapping
	public CollectionModel<EntityModel<ExchangeRate>> getRates(@RequestParam(value = "fromCurrency", required = false) String fromCurrency, @RequestParam(value = "toCurrency", required = false) String toCurrency) {
		return createCollection(getExchangeRateList(fromCurrency, toCurrency));
	}

	private List<ExchangeRate> getExchangeRateList(String fromCurrency, String toCurrency) {
		if (fromCurrency != null && toCurrency != null)
			return Collections.singletonList(dao.getExchangeRate(fromCurrency, toCurrency));
		else if (fromCurrency != null)
			return dao.getExchangeRatesFrom(fromCurrency);
		else if (toCurrency != null)
			return dao.getExchangeRatesTo(toCurrency);
		else
			return dao.getExchangeRates();
	}
}
