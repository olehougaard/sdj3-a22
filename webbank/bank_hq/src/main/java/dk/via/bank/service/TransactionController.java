package dk.via.bank.service;

import dk.via.bank.TransactionDAO;
import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.parameters.TransactionSpecification;
import dk.via.bank.dto.transaction.AbstractTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/customers/{cpr}/accounts/{accountNumber}/transactions")
public class TransactionController {
	private final TransactionDAO dao;

	public TransactionController(@Autowired TransactionDAO dao) {
		this.dao = dao;
	}
	private EntityModel<TransactionSpecification> createEntity(String cpr, String accountString, AbstractTransaction transaction) {
		return EntityModel.of(TransactionSpecification.from(transaction),
				linkTo(methodOn(getClass()).readTransaction(cpr, accountString, transaction.getId())).withSelfRel());
	}

	private CollectionModel<EntityModel<TransactionSpecification>> createCollection(String cpr, String accountString, List<AbstractTransaction> transactions) {
		List<EntityModel<TransactionSpecification>> accountModels = transactions.stream().map(t -> createEntity(cpr, t.getAccount().toString(), t)).collect(Collectors.toList());
		return CollectionModel.of(accountModels, linkTo(methodOn(getClass()).readTransactionsFor(cpr, accountString)).withSelfRel());
	}


	@PostMapping
	public EntityModel<TransactionSpecification> createTransaction(@PathVariable("cpr") String cpr, @PathVariable("accountNumber") String accountString, @RequestBody TransactionSpecification transactionSpec) {
		return createEntity(cpr, accountString, dao.createTransaction(cpr, AccountNumber.fromString(accountString), transactionSpec));
	}

	@GetMapping("/{id}")
	public EntityModel<TransactionSpecification> readTransaction(@PathVariable("cpr") String cpr, @PathVariable("accountNumber") String accountString, @PathVariable("id") int transactionId) {
		return createEntity(cpr, accountString, dao.readTransaction(cpr, AccountNumber.fromString(accountString), transactionId));
	}

	@GetMapping
	public CollectionModel<EntityModel<TransactionSpecification>> readTransactionsFor(@PathVariable("cpr") String cpr, @PathVariable("accountNumber") String accountString) {
		return createCollection(cpr, accountString, dao.readTransactionsFor(cpr, AccountNumber.fromString(accountString)));
	}
}
