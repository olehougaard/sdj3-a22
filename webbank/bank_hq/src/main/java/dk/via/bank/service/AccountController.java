package dk.via.bank.service;

import dk.via.bank.AccountDAO;
import dk.via.bank.dto.Account;
import dk.via.bank.dto.AccountNumber;
import dk.via.bank.dto.parameters.AccountSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/customers/{cpr}/accounts")
public class AccountController {
	private final AccountDAO dao;

	public AccountController(@Autowired AccountDAO dao) {
		this.dao = dao;
	}

	private EntityModel<Account> createEntity(Account account) {
		String accountString = account.getAccountNumber().toString();
		String cpr = account.getCustomerCpr();
		return EntityModel.of(account,
			linkTo(methodOn(getClass()).readAccount(cpr, accountString)).withSelfRel(),
			linkTo(methodOn(TransactionController.class).readTransactionsFor(cpr, accountString)).withRel("transactions"));
	}

	private CollectionModel<EntityModel<Account>> createCollection(String cpr, List<Account> accounts) {
		List<EntityModel<Account>> accountModels = accounts.stream().map(this::createEntity).collect(Collectors.toList());
		return CollectionModel.of(accountModels, linkTo(methodOn(getClass()).readAccountsFor(cpr)).withSelfRel());
	}

	@PostMapping
	public EntityModel<Account> createAccount(@PathVariable("cpr") String cpr, @RequestBody AccountSpecification specification) {
		return createEntity(dao.createAccount(cpr, specification));
	}

	@GetMapping
    public CollectionModel<EntityModel<Account>> readAccountsFor(@PathVariable("cpr") String cpr) {
		return createCollection(cpr, dao.readAccountsFor(cpr));
	}

	@GetMapping("/{accountNumber}")
    public EntityModel<Account> readAccount(@PathVariable("cpr") String cpr, @PathVariable("accountNumber") String accountString) {
		return createEntity(dao.readAccount(cpr, AccountNumber.fromString(accountString)));
	}

	@PutMapping("/{accountNumber}")
    public void updateAccount(@PathVariable("cpr") String cpr, @PathVariable("accountNumber") String accountString, @RequestBody Account account) {
    	dao.updateAccount(cpr, AccountNumber.fromString(accountString), account);
	}

	@DeleteMapping("/{accountNumber}")
    public void deleteAccount(@PathVariable("cpr") String cpr, @PathVariable("accountNumber") String accountString) {
		AccountNumber accountNumber = AccountNumber.fromString(accountString);
    	dao.deleteAccount(cpr, accountNumber);
	}
}
