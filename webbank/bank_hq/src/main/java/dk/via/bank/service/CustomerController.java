package dk.via.bank.service;

import dk.via.bank.CustomerDAO;
import dk.via.bank.dto.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/customers")
public class CustomerController {
	private final CustomerDAO dao;

	public CustomerController(@Autowired CustomerDAO dao) {
		this.dao = dao;
	}

	private EntityModel<Customer> createEntity(Customer customer) {
		String cpr = customer.getCpr();
		return EntityModel.of(customer,
				linkTo(methodOn(getClass()).readCustomer(cpr)).withSelfRel(),
				linkTo(methodOn(AccountController.class).readAccountsFor(cpr)).withRel("accounts"));
	}


	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EntityModel<Customer> createCustomer(@RequestBody Customer customer) {
		return createEntity(dao.createCustomer(customer));
	}

	@GetMapping("/{cpr}")
	public EntityModel<Customer> readCustomer(@PathVariable("cpr") String cpr) {
		return createEntity(dao.readCustomer(cpr));
	}

	@PutMapping("/{cpr}")
	public ResponseEntity<EntityModel<Customer>> updateCustomer(@PathVariable("cpr") String cpr, @RequestBody Customer customer) {
		if (dao.readCustomer(cpr) == null) {
			dao.createCustomer(customer);
			return ResponseEntity.status(HttpStatus.CREATED).body(createEntity(customer));
		} else {
			dao.updateCustomer(cpr, customer);
			return ResponseEntity.status(HttpStatus.OK).body(createEntity(customer));
		}
	}

	@DeleteMapping("/{cpr}")
	public void deleteCustomer(@PathVariable("cpr") String cpr) {
		dao.deleteCustomer(cpr);
	}
}
