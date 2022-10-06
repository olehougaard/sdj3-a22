package dk.via.bank.dao;

import dk.via.bank.CustomerDAO;
import dk.via.bank.dao.exception.Conflict;
import dk.via.bank.dao.exception.NotFound;
import dk.via.bank.dto.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class CustomerDAOImpl implements CustomerDAO {
	private final DatabaseHelper<Customer> helper;

	public CustomerDAOImpl(@Autowired DatabaseHelper<Customer> helper) {
		this.helper = helper;
	}
	
	private static class CustomerMapper implements DataMapper<Customer> {
		@Override
		public Customer create(ResultSet rs) throws SQLException {
			String cpr = rs.getString("cpr");
			String name = rs.getString("name");
			String address = rs.getString("address");
			return new Customer(cpr, name, address);
		}
	}

	@Override
	public Customer createCustomer(Customer customer) {
		helper.executeUpdate("INSERT INTO Customer VALUES (?, ?, ?)", customer.getCpr(), customer.getName(), customer.getAddress());
		return customer;
	}

	@Override
	public Customer readCustomer(String cpr) {
		CustomerMapper mapper = new CustomerMapper();
		Customer customer = helper.mapSingle(mapper, "SELECT * FROM Customer WHERE cpr = ?;", cpr);
		if (customer == null) {
			throw new NotFound();
		}
		return customer;
	}

	@Override
	public void updateCustomer(String cpr, Customer customer) {
		if (customer.getCpr() != null && !customer.getCpr().isEmpty() && !customer.getCpr().equals(cpr)) {
			throw new Conflict();
		}
		helper.executeUpdate("UPDATE Customer set name = ?, address = ? WHERE cpr = ?", customer.getName(), customer.getAddress(), cpr);
	}

	@Override
	public void deleteCustomer(@PathVariable("cpr") String cpr) {
		helper.executeUpdate("DELETE FROM Customer WHERE cpr = ?", cpr);
	}
}
